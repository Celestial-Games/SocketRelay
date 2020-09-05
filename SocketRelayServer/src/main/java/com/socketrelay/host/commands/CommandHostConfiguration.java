package com.socketrelay.host.commands;

import java.io.IOException;
import java.net.UnknownHostException;

import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.socketrelay.games.GameManager;
import com.socketrelay.games.GameSession;
import com.socketrelay.messages.GameStarted;
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
	
	private GameManager gameManager;

	public CommandHostConfiguration(GameManager gameManager){
		this.gameManager=gameManager;
	}
	
	@Override
	public void processCommand(IoSession session, HostConfiguration message) throws UnknownHostException, IOException {
		GameSession gameSession = gameManager.getOrCreateGameSession(message.getGame().getName(),message.getPassword());
		if (gameSession!=null) {
			session.write(new GameStarted());
		} else {
			// TODO: What action should we take here if the game is rejected (Might add server too busy for example)
			logger.warn("Unable to create game session");
		}
//		
//		
//		if (port==-1) {
//			logger.error("Unable to accept new connection can not find a free port.");
//			session.write(new Error("Unable to find a port to use here, server may be too busy."));
//			session.closeOnFlush();
//		} else {
//			logger.debug("Opening port ["+port+"] for client connections.");
//			session.setAttribute(HostPortAttribute, port);
//			portsUsed.add(port);
//
//			ServerConnectionThreadTcp serverConnectionThread=new ServerConnectionThreadTcp(session, new ServerSocket(port,50,InetAddress.getByName(config.getServerIp()))); 
//			session.setAttribute(HostConnectionThreadAttribute, serverConnectionThread);
//			serverConnectionThread.start();
//		}
	}
}
