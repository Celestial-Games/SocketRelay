package com.socketrelay.client;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.socketrelay.client.beans.Game;
import com.socketrelay.client.beans.Server;
import com.socketrelay.messages.Configuration;

public class PlayerConnection extends Thread implements TrafficCounterSource {
	private static final Logger logger=LoggerFactory.getLogger(PlayerConnection.class);

	private Server server;
	private Game game;
	private Configuration configuration=null;

	private ServerSocket serverSocket; 
	private int instancePort;

	private List<ConnectionListener> cConnectionListeners=new ArrayList<>();
	
	private List<Connection> connections=new ArrayList<Connection>();
	private Map<String,TrafficCounter> trafficCounterMap=new HashMap<>();
	private TrafficCounter trafficCounter;

	public PlayerConnection(Server server,Game game, int instancePort){
		this.server=server;
		this.game=game;
		this.instancePort=instancePort;
		trafficCounter=new TrafficCounter("me");
		trafficCounterMap.put("me",trafficCounter);
	}

	public Map<String,TrafficCounter> getTrafficCounters(){
		return trafficCounterMap;
	}
	
	public void addConnectionListener(ConnectionListener connectionListener) {
		cConnectionListeners.remove(connectionListener);
		cConnectionListeners.add(connectionListener);
	}

	public void removeConnectionListener(ConnectionListener connectionListener) {
		cConnectionListeners.remove(connectionListener);
	}

	public void connect() throws UnknownHostException, IOException, ConnectException {
		serverSocket=new ServerSocket(game.getPort());
		start();
	}

	public Configuration getConfiguration() {
		return configuration;
	}

	public void run() {
		try {
			while (serverSocket.isBound() && !serverSocket.isClosed()) {
				Socket inCommingSocket=serverSocket.accept();
				Connection connection=new Connection(inCommingSocket);
				synchronized (connections) {
					connections.add(connection);	
				}
				sendClientsCount();
			}
		} catch (IOException e) {
			logger.error(e.getMessage(),e);
			close();
		}
	}

	private void sendClientsCount() {
		for (ConnectionListener configurationListener:cConnectionListeners) {
			configurationListener.clientConnectedChanged(1, connections.size());
		}
	}

	public void close() {
		logger.info("Connection to server closed.");
		try {
			for (ConnectionListener configurationListener:cConnectionListeners) {
				configurationListener.serverConnectedClosed();
			}
			for (Connection connection:connections) {
				connection.close();
			}
		} catch (Exception e) {
			logger.error(e.getMessage(),e);
		}
	}

	private class Connection extends Thread{

		private Socket outGoingSocket; 
		private Socket inCommingSocket;

		private InputStream inputInputStream;
		private OutputStream inputOutputStream;
		private InputStream outputInputStream;
		private OutputStream outputOutputStream;

		private Connection(Socket inCommingSocket) throws IOException{
			try {
				this.inCommingSocket=inCommingSocket;
				inputInputStream=inCommingSocket.getInputStream();
				inputOutputStream=inCommingSocket.getOutputStream();
				makeOutgoingConnection();
				setDaemon(true);
			} catch (IOException|IllegalArgumentException e) {
				logger.error(e.getMessage(),e);
				close();
				throw e;
			}
			start();
		}

		private void makeOutgoingConnection() throws UnknownHostException, IOException, IllegalArgumentException {
			outGoingSocket=new Socket(server.getIp(),instancePort);
			outputInputStream=outGoingSocket.getInputStream();
			outputOutputStream=outGoingSocket.getOutputStream();

			Thread outgoingThread=new Thread(new Runnable() {
				@Override
				public void run() {
					byte[] buffer=new byte[16*1024];
					try {
						while (inCommingSocket.isConnected() && outGoingSocket.isConnected()) {
							int r=outputInputStream.read(buffer);
							if (r>0) {
								inputOutputStream.write(buffer,0,r);
							} else if (r==-1) {
								break;
							}
							trafficCounter.addBytesCount(r);
						}
					} catch (IOException e) {
						logger.warn(e.getMessage(),e);
					}
					close();
				}
			});
			outgoingThread.setDaemon(true);
			outgoingThread.start();
		}

		private void close() {
			if (outGoingSocket!=null) {
				try {
					outGoingSocket.close();
				} catch (IOException e) {
					logger.warn(e.getMessage(),e);
				}
			}
			if (inCommingSocket!=null) {
				try {
					inCommingSocket.close();
				} catch (IOException e) {
					logger.warn(e.getMessage(),e);
				}
			}
			synchronized (connections) {
				connections.remove(this);	
			}
			sendClientsCount();
		}

		public void run() {
			byte[] buffer=new byte[16*1024];
			try {
				while (inCommingSocket.isConnected() && outGoingSocket.isConnected()) {
					int r=inputInputStream.read(buffer);
					if (r>0) {
						outputOutputStream.write(buffer,0,r);
					} else if (r==-1) {
						break;
					}
					trafficCounter.addBytesCount(r);
				}
			} catch (IOException e) {
				logger.warn(e.getMessage(),e);
			}
			close();
		}
	}
}
