package com.socketrelay.client;

import java.io.IOException;
import java.io.Serializable;
import java.net.InetSocketAddress;
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

public class ServerConnection extends Thread implements TrafficCounterSource {
	private static final Logger logger=LoggerFactory.getLogger(ServerConnection.class);

	private Server server;
	private Game game;
	private Configuration configuration=null;

	private NioSocketConnector connector=null;
	private ConnectFuture future=null;
	private IoSession session=null;

	private Map<String,Map<Integer,ClientConnection>> clientsMap=new HashMap<>();
	private Map<String,TrafficCounter> clientsTrafficCounterMap=new HashMap<>();
	private List<ConnectionListener> connectionListeners=new ArrayList<>();
	private long totalBytes=0;
	private boolean closed=false;

	public ServerConnection(Server server,Game game){
		this.server=server;
		this.game=game;
	}

	public void addConnectionListener(ConnectionListener connectionListener) {
		connectionListeners.remove(connectionListener);
		connectionListeners.add(connectionListener);
	}

	public void removeConnectionListener(ConnectionListener connectionListener) {
		connectionListeners.remove(connectionListener);
	}

	public long getTotalBytes() {
		return totalBytes;
	}

	public void addToTotalBytes(Data data) {
		if (data!=null && data.getData()!=null) {
			totalBytes+=data.getData().length;
		}
	}
	
	public Map<String,TrafficCounter> getTrafficCounters(){
		return clientsTrafficCounterMap;
	}

	public void connect() {
		closed = false;
		connector = new NioSocketConnector();
		connector.setConnectTimeoutMillis(20000);
		connector.getFilterChain().addLast("codes", new ProtocolCodecFilter(new ObjectSerializationCodecFactory()));
		
		connector.setHandler(new IoHandler() {
			
			@Override
			public void sessionOpened(IoSession session) throws Exception {
				Notifications.connectedToserver();
			}
			
			@Override
			public void sessionIdle(IoSession session, IdleStatus status) throws Exception {
			}
			
			@Override
			public void sessionCreated(IoSession session) throws Exception {
			}
			
			@Override
			public void sessionClosed(IoSession session) throws Exception {
				connectionToServerLost();
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
		connector.setConnectTimeoutMillis(500);
		connector.setConnectTimeoutCheckInterval(500);
		future = connector.connect(new InetSocketAddress(server.getIp(), server.getPort()));
		start();
	}

	public Configuration getConfiguration() {
		return configuration;
	}

	public void run() {
		sendClientsCount();
		future.awaitUninterruptibly();
		try {
			session=future.getSession();
			session.getCloseFuture().awaitUninterruptibly();
		} catch (Exception e) {
			Notifications.serverRejectedConnection(server.getName(), server.getIp(), server.getPort(), e.getMessage());
			logger.warn(e.getMessage(),e);
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
		for (ConnectionListener configurationListener:connectionListeners) {
			configurationListener.receiveConfiguration(server, message);
		}
	}
	
	public void removeClientConnection(String clientId, int connectionId) {
		Map<Integer,ClientConnection> connectionsMap=clientsMap.get(clientId);
		if (connectionsMap!=null) {
			synchronized (connectionsMap) {
				connectionsMap.remove(connectionId);
				if (connectionsMap.size()==0) {
					clientsMap.remove(clientId);
					clientsTrafficCounterMap.remove(clientId);
					Notifications.clientLeft();
				}
			}
		}
		sendClientsCount();
	}

	public void processMessage(Data message) {
		try {
			Map<Integer,ClientConnection> connectionsMap=clientsMap.get(message.getClientId());
			if (connectionsMap==null) {
				synchronized (clientsMap) {
					connectionsMap=clientsMap.get(message.getClientId());
					if (connectionsMap==null) {
						connectionsMap=new HashMap<Integer, ClientConnection>();
						clientsMap.put(message.getClientId(),connectionsMap);
						clientsTrafficCounterMap.put(message.getClientId(),new TrafficCounter(message.getClientId()));
						Notifications.clientJoined();
					}
				}
			}
			
			ClientConnection clientConnection=connectionsMap.get(message.getConnectionId());
			if (clientConnection==null) {
				clientConnection=new ClientConnectionTcp(session, message.getClientId(), message.getConnectionId(), "localhost", game.getPort(),clientsTrafficCounterMap.get(message.getClientId()),this);
				connectionsMap.put(message.getConnectionId(),clientConnection);
			}
			sendClientsCount();

			addToTotalBytes(message);
			
			clientConnection.send(message);
		} catch (IOException e) {
			logger.error(e.getMessage(),e);
			session.write(new ClientClose(message.getClientId(),message.getConnectionId()));
			removeClientConnection(message.getClientId(),message.getConnectionId());
		}
	}

	public void processMessage(Heartbeat message) {
		session.write(message);
	}

	private void sendClientsCount() {
		int clients=0;
		int connections=0;
		synchronized (clientsMap) {
			clients=clientsMap.size();
			for (Map<Integer,ClientConnection> clientConnectionMap:clientsMap.values()) {
				connections+=clientConnectionMap.size();
			}
		}
		for (ConnectionListener configurationListener:connectionListeners) {
			configurationListener.clientConnectedChanged(clients,connections);
		}
	}
	
	public void processMessage(ClientClose message) {
		synchronized (clientsMap) {
			removeClientConnection(message.getClientId(),message.getConnectionId());
		}
	}
	
	private void connectionToServerLost() {
		Notifications.disconnectedFromserver();
		for (Map<Integer,ClientConnection> clientConnections:clientsMap.values()) {
			for (ClientConnection clientConnection:clientConnections.values()) {
				clientConnection.close();
			}
		}
		if (!closed) {
			for (ConnectionListener configurationListener:connectionListeners) {
				configurationListener.serverConnectedClosed();
			}
			connect();
		}
	}
		

	public void close() {
		logger.info("Connection to server closed.");
		closed=true;
		try {
			if (session!=null && session.isConnected()) {
				session.closeNow();
			}
		} catch (Exception e) {
			logger.error(e.getMessage(),e);
		}
	}

}
