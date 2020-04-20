package com.socketrelay.host;

import static com.socketrelay.host.Consts.HostConnectionThreadAttribute;
import static com.socketrelay.host.Consts.HostPortAttribute;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
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
import com.socketrelay.host.commands.CommandHostConfiguration;
import com.socketrelay.messages.ClientClose;
import com.socketrelay.messages.Close;
import com.socketrelay.messages.Data;
import com.socketrelay.messages.HostConfiguration;

public class HostHandler extends IoHandlerAdapter{
	private static final Logger logger=LoggerFactory.getLogger(HostHandler.class);

	private Map<Class<? extends Serializable>,Command<? extends Serializable>> commands=new HashMap<>();
	
	private Set<Integer> portsUsed=new HashSet<>();
	
	public HostHandler(SocketRelayHost socketRelayHost, SocketRelayConfig config, Set<Integer> portsUsed) {
		this.portsUsed=portsUsed;
		
		commands.put(ClientClose.class, new CommandClientClose());
		commands.put(Close.class, new CommandClose());
		commands.put(Data.class, new CommandData());
		commands.put(HostConfiguration.class, new CommandHostConfiguration(config,portsUsed));
	}
	
	@Override
	public void exceptionCaught(IoSession session, Throwable cause) throws Exception {
		logger.error(cause.getMessage(), cause);
		session.closeOnFlush();
	}

	@Override
	public void sessionIdle(IoSession session, IdleStatus status) throws Exception {
		logger.debug("Session idle "+session.getId());
	}

	@Override
	public void sessionClosed(IoSession session) throws Exception {
		logger.debug("Session closed "+session.getId());
		portsUsed.remove(session.getAttribute(HostPortAttribute));
		
		ServerConnectionThreadTcp serverConnectionThread=(ServerConnectionThreadTcp)session.getAttribute(HostConnectionThreadAttribute);
		if (serverConnectionThread!=null){
			serverConnectionThread.close();
		}

		super.sessionClosed(session);
	}

	@Override
	public void sessionOpened(IoSession session) throws Exception {
		super.sessionOpened(session);
		logger.debug("Session opened "+session.getId());
	}
	
	@Override
	public void messageReceived(IoSession session, Object objMessage) throws Exception {
		logger.debug("Session message recieved "+session.getId());
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
