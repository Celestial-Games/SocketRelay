package com.socketrelay.host;

import static com.socketrelay.host.Consts.HostConnectionThreadAttribute;
import static com.socketrelay.host.Consts.HostPortAttribute;

import java.io.Serializable;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.socketrelay.host.commands.Command;
import com.socketrelay.host.commands.CommandClientClose;
import com.socketrelay.host.commands.CommandClose;
import com.socketrelay.host.commands.CommandData;
import com.socketrelay.messages.ClientClose;
import com.socketrelay.messages.Close;
import com.socketrelay.messages.Configuration;
import com.socketrelay.messages.Data;

public class HostHandler extends IoHandlerAdapter{
	private static final Logger logger=LoggerFactory.getLogger(HostHandler.class);

	private static Map<Class<? extends Serializable>,Command<? extends Serializable>> commands=new HashMap<>();
	
	static {
		commands.put(ClientClose.class, new CommandClientClose());
		commands.put(Close.class, new CommandClose());
		commands.put(Data.class, new CommandData());
	}
	
	private Map<Integer,String> portIpMapping=new HashMap<Integer, String>(); 
	
	private SocketRelayConfig config;
	private Set<Integer> portsUsed=new HashSet<>();
	
	public HostHandler(SocketRelayConfig config, Set<Integer> portsUsed) {
		this.config=config;
		this.portsUsed=portsUsed;
	}
	
	@Override
	public void exceptionCaught(IoSession session, Throwable cause) throws Exception {
		logger.error(cause.getMessage(), cause);
		session.closeOnFlush();
	}

	@Override
	public void sessionIdle(IoSession session, IdleStatus status) throws Exception {
	}

	@Override
	public void sessionClosed(IoSession session) throws Exception {
		logger.debug("Session closed "+session.getId());
		portsUsed.remove(session.getAttribute(HostPortAttribute));
		
		ServerConnectionThread serverConnectionThread=(ServerConnectionThread)session.getAttribute(HostConnectionThreadAttribute);
		if (serverConnectionThread!=null){
			serverConnectionThread.close();
		}

		super.sessionClosed(session);
	}

	@Override
	public void sessionOpened(IoSession session) throws Exception {
		super.sessionOpened(session);
		logger.debug("Session opened "+session.getId());

		int port=-1;
		synchronized (portsUsed) {
			String ip=(((InetSocketAddress)session.getRemoteAddress()).getAddress()).toString().replace("/","");
			// TODO: This could be a lot better, cycle through range from start to finish looking for free port explore full range.
			for (int t=0;t<100;t++) {
				int portNum=(int)(Math.floor((Math.random()*(config.getClientHigh()-config.getClientLow()+1)+config.getClientLow())));
				// If this is the first port we exploring lets use the one last used for this IP
				if (t==0) {
					for (Entry<Integer, String> entry:portIpMapping.entrySet()) {
						if (ip.equals(entry.getValue())){
							portNum=entry.getKey();
							break;
						}
					}
				}
				if (!portsUsed.contains(portNum)) {
					port=portNum;
					portIpMapping.put(port, ip);
					break;
				}
			}
		}
		
		if (port==-1) {
			logger.error("Unable to accept new connection can not find a free port.");
			session.write(new Error("Unable to find a port to use here, server may be too busy."));
			session.closeOnFlush();
		} else {
			logger.debug("Opening port ["+port+"] for client connections.");
			session.setAttribute(HostPortAttribute, port);
			portsUsed.add(port);

			ServerConnectionThread serverConnectionThread=new ServerConnectionThread(session, new ServerSocket(port,50,InetAddress.getByName(config.getServerIp()))); 
			session.setAttribute(HostConnectionThreadAttribute, serverConnectionThread);
			serverConnectionThread.start();
			
			// Send back configuration message
			session.write(new Configuration(port));
		}
	}
	
	@Override
	public void messageReceived(IoSession session, Object objMessage) throws Exception {
		Command<? extends Serializable> command=commands.get(objMessage.getClass());
		
		if (command!=null) {
			logger.debug("Message Recieved: "+objMessage.getClass().getSimpleName());
			command.processCommandRaw(session, (Serializable)objMessage);
		} else {
			logger.error("No command found for message type '"+objMessage.getClass().getName()+"'");
			session.closeNow();
		}
	}

}
