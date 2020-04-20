package com.socketrelay.client;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.BindException;
import java.net.ConnectException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.socketrelay.client.beans.Game;
import com.socketrelay.client.beans.Server;
import com.socketrelay.messages.Data;

public class PlayerConnectionUdp extends PlayerConnection implements TrafficCounterSource {
	private static final Logger logger=LoggerFactory.getLogger(PlayerConnectionUdp.class);

	private DatagramSocket serverSocket;
	private int instancePort;

	private List<ConnectionListener> cConnectionListeners=new ArrayList<>();
	
	private Connection connection=null;
	private long totalBytes=0;
	
	public PlayerConnectionUdp(Server server,Game game, int instancePort){
		super(server, game);
		this.instancePort=instancePort;
	}

	@Override
	public long getTotalBytes() {
		return totalBytes;
	}

	@Override
	public void connect() throws UnknownHostException, IOException, ConnectException {
		try {
			serverSocket=new DatagramSocket(game.getPort());
		} catch (BindException e) {
			Notifications.unableToBindLocalPort(game.getPort());
			throw e;
		}
		start();
	}

	@Override
	public void run() {
		byte[] buffer = new byte[65508];
		try {
			DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
			while (serverSocket.isBound() && !serverSocket.isClosed()) {
				packet.setLength(buffer.length);
				serverSocket.receive(packet);
				
				if (connection==null) {
					connection=new Connection();
				}
				
				if (packet.getLength()>0) {
					Data data=new Data(clientId, connectionId, Arrays.copyOf(buffer, packet.getLength()));
					serverConnection.addToTotalBytes(data);
					session.write(data);
					trafficCounter.addBytesCount(packet.getLength());
					totalBytes+=packet.getLength();

					int r=outputInputStream.read(buffer);
					if (r>0) {
						inputOutputStream.write(buffer,0,r);
					} else if (r==-1) {
						break;
					}
					trafficCounter.addBytesCount(r);
				
				}

				Socket inCommingSocket=serverSocket.accept();
				if (connections.size()==0) {
					sendGameConnected();
				}
				Connection connection=new Connection(inCommingSocket);
				synchronized (connections) {
					connections.add(connection);	
				}
				sendClientsCount(connections.size());
			}
		} catch (IOException e) {
			logger.error(e.getMessage(),e);
			close();
		} finally {
			if (serverSocket!=null && serverSocket.isBound()) {
				try {
					serverSocket.close();
				} catch (IOException e) {
					logger.error(e.getMessage(),e);
				}
			}
		}
	}

	@Override
	public void close() {
		logger.info("Connection to server closed.");
		try {
			if (serverSocket!=null && serverSocket.isBound()) {
				try {
					serverSocket.close();
				} catch (IOException e) {
					logger.error(e.getMessage(),e);
				}
			}
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

	private class Connection {

		private Socket outGoingSocket; 

		private InputStream outputInputStream;
		private OutputStream outputOutputStream;

		private Connection() throws IOException{
			try {
				makeOutgoingConnection();
				setDaemon(true);
			} catch (IOException|IllegalArgumentException e) {
				logger.error(e.getMessage(),e);
				close();
				throw e;
			}
		}

		private void makeOutgoingConnection() throws UnknownHostException, IOException, IllegalArgumentException {
			try {
				outGoingSocket=new Socket();
				outGoingSocket.connect(new InetSocketAddress(server.getIp(),instancePort),2000); 
				outputInputStream=outGoingSocket.getInputStream();
				outputOutputStream=outGoingSocket.getOutputStream();
				outGoingSocket.setSoTimeout(0);
				Thread outgoingThread=new Thread(new Runnable() {
					@Override
					public void run() {
						byte[] buffer=new byte[65508];
						try {
							while (serverSocket.isConnected() && outGoingSocket.isConnected()) {
								int r=outputInputStream.read(buffer);
								if (r>0) {
									
									inputOutputStream.write(buffer,0,r);
								} else if (r==-1) {
									break;
								}
								trafficCounter.addBytesCount(r);
								totalBytes+=r;
							}
						} catch (IOException e) {
							logger.warn(e.getMessage(),e);
						}
						close();
					}
				});
				outgoingThread.setDaemon(true);
				outgoingThread.start();
			} catch (IOException e) {
				Notifications.serverRejectedConnection(server.getName(), server.getIp(),instancePort, e.getMessage());
				throw e;
			}
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
			connection=null;	
			sendClientsCount(0);
		}
	}
}
