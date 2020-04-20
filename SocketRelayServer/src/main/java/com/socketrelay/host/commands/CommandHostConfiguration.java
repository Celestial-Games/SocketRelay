package com.socketrelay.host.commands;

import static com.socketrelay.host.Consts.HostConnectionThreadAttribute;
import static com.socketrelay.host.Consts.HostPortAttribute;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.socketrelay.host.ServerConnectionThreadTcp;
import com.socketrelay.host.SocketRelayConfig;
import com.socketrelay.messages.Configuration;
import com.socketrelay.messages.HostConfiguration;

/**
 * This is an unsatisfactory solution. 
 * 
 * Issues.
 * - Two hosts coming from the same network would be assumed to be the same host. 
 * - A host who's source IP changes would keep consuming different ports.
 * - Eventually all the available ports would be reserved.
 * - A port allocated to the same host right after a DC is not guaranteed to have  disconnected and binding may fail.
 * 
 * This will be fixed once we start to identify users and send invitations to players over Discord. This will remove 
 * the whole aspect of requiring a port since the players will query the server and be told what port to use. DC will
 * automatically restart on a new port forcing a full DC and RC process silently.
 * 
 * @author Travis
 *
 */
public class CommandHostConfiguration extends Command<HostConfiguration> {
	private static final Logger logger=LoggerFactory.getLogger(CommandHostConfiguration.class);
	
	private Map<Integer,String> portIpMapping=new HashMap<Integer, String>(); 
	private Set<Integer> portsUsed;
	private SocketRelayConfig config;
	
	public CommandHostConfiguration(SocketRelayConfig config, Set<Integer> portsUsed){
		this.portsUsed=portsUsed;
	}
	
	@Override
	public void processCommand(IoSession session, HostConfiguration message) throws UnknownHostException, IOException {
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
							if (!portsUsed.contains(portNum)) {
								port=portNum;
								portIpMapping.put(port, ip);
								break;
							}
							// Remove this for now.
							portsUsed.remove(portNum);
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

			ServerConnectionThreadTcp serverConnectionThread=new ServerConnectionThreadTcp(session, new ServerSocket(port,50,InetAddress.getByName(config.getServerIp()))); 
			session.setAttribute(HostConnectionThreadAttribute, serverConnectionThread);
			serverConnectionThread.start();
			
			// Send back configuration message
			session.write(new Configuration(port));
		}
	}
}
