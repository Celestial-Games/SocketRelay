package com.socketrelay.client;

import java.io.IOException;
import java.io.Serializable;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.service.IoHandler;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.FilterEvent;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.codec.serialization.ObjectSerializationCodecFactory;
import org.apache.mina.transport.socket.nio.NioSocketConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.socketrelay.client.beans.Game;
import com.socketrelay.client.beans.Server;
import com.socketrelay.messages.ClientClose;
import com.socketrelay.messages.Configuration;
import com.socketrelay.messages.Data;
import com.socketrelay.messages.Heartbeat;

public class ServerConnection extends Thread {
	private static final Logger logger=LoggerFactory.getLogger(ServerConnection.class);

	private Server server;
	private Game game;
	private Configuration configuration=null;

	private NioSocketConnector connector=null;
	private ConnectFuture future=null;
	private IoSession session=null;

	private Map<String,ClientConnection> clientConnections=new HashMap<>();
	private List<ConnectionListener> cConnectionListeners=new ArrayList<>();

	public ServerConnection(Server server,Game game){
		this.server=server;
		this.game=game;
	}

	public void addConnectionListener(ConnectionListener connectionListener) {
		cConnectionListeners.remove(connectionListener);
		cConnectionListeners.add(connectionListener);
	}

	public void removeConnectionListener(ConnectionListener connectionListener) {
		cConnectionListeners.remove(connectionListener);
	}

	public void connect() throws UnknownHostException, IOException, ConnectException {
		connector = new NioSocketConnector();
		connector.setConnectTimeoutMillis(20000);
		connector.getFilterChain().addLast("codes", new ProtocolCodecFilter(new ObjectSerializationCodecFactory()));
		
		connector.setHandler(new IoHandler() {
			
			@Override
			public void sessionOpened(IoSession session) throws Exception {
			}
			
			@Override
			public void sessionIdle(IoSession session, IdleStatus status) throws Exception {
			}
			
			@Override
			public void sessionCreated(IoSession session) throws Exception {
			}
			
			@Override
			public void sessionClosed(IoSession session) throws Exception {
				close();
			}
			
			@Override
			public void messageSent(IoSession session, Object message) throws Exception {
			}
			
			@Override
			public void messageReceived(IoSession session, Object message) throws Exception {
				processMessage((Serializable)message);
			}
			
			@Override
			public void inputClosed(IoSession session) throws Exception {
			}
			
			@Override
			public void exceptionCaught(IoSession session, Throwable cause) throws Exception {
			}
			
			@Override
			public void event(IoSession session, FilterEvent event) throws Exception {
			}
		});

		future = connector.connect(new InetSocketAddress(server.getIp(), server.getPort()));
		start();
	}

	public Configuration getConfiguration() {
		return configuration;
	}

	public void run() {
		sendClientsCount();

		NioSocketConnector connector = new NioSocketConnector();
		future.awaitUninterruptibly();
		try {
			session=future.getSession();
			session.getCloseFuture().awaitUninterruptibly();
		} catch (Exception e) {
			logger.warn(e.getMessage(),e);
			close();
		}
		connector.dispose();
	}

	/**
	 * This receives messages from the server to process.
	 * 
	 * @param message
	 */
	public void processMessage(Serializable message) {
		logger.info("Recieved "+message.getClass().getSimpleName());
		if (message instanceof Data) {
			processMessage((Data)message);
		} else if (message instanceof Heartbeat) {
			processMessage((Heartbeat)message);
		} else if (message instanceof Configuration) {
			processMessage((Configuration)message);
		} else if (message instanceof ClientClose) {
			processMessage((ClientClose)message);
		} else {
			logger.error("Unknown message type. '"+message.getClass().getName()+"'");
		}
	}

	public void processMessage(Configuration message) {
		configuration=message;
		for (ConnectionListener configurationListener:cConnectionListeners) {
			configurationListener.receiveConfiguration(message);
		}
	}

	public void processMessage(Data message) {
		try {
			if (!clientConnections.containsKey(message.getClientId())) {
				synchronized (clientConnections) {
					clientConnections.put(message.getClientId(),new ClientConnection(session, message.getClientId(), "localhost", game.getPort()));
					sendClientsCount();
				}
			}
			
			clientConnections.get(message.getClientId()).send(message);
		} catch (IOException e) {
			logger.error(e.getMessage(),e);
			session.write(new ClientClose(message.getClientId()));
			clientConnections.remove(message.getClientId()).close();
		}
	}

	public void processMessage(Heartbeat message) {
		session.write(message);
	}

	private void sendClientsCount() {
		for (ConnectionListener configurationListener:cConnectionListeners) {
			configurationListener.clientConnectedChanged(clientConnections.size());
		}
	}
	
	public void processMessage(ClientClose message) {
		synchronized (clientConnections) {
			clientConnections.remove(message.getClientId()).close();
			sendClientsCount();
		}
	}

	public void close() {
		logger.info("Connection to server closed.");
		try {
			for (ConnectionListener configurationListener:cConnectionListeners) {
				configurationListener.serverConnectedClosed();
			}
			for (ClientConnection clientConnection:clientConnections.values()) {
				clientConnection.close();
			}
			if (session!=null && session.isConnected()) {
				session.closeNow();
			}
		} catch (Exception e) {
			logger.error(e.getMessage(),e);
		}
	}

}
