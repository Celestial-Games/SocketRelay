package com.socketrelay.client;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Arrays;

import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.socketrelay.messages.ClientClose;
import com.socketrelay.messages.Data;

public class ClientConnectionUdp extends ClientConnection {
	private static final Logger logger=LoggerFactory.getLogger(ClientConnectionUdp.class);

	private DatagramSocket socket;

	public ClientConnectionUdp(IoSession session, String clientId, int connectionId, String host,int port, TrafficCounter trafficCounter, ServerConnection serverConnection) throws UnknownHostException, IOException {
		super(session, clientId, connectionId, host,port, trafficCounter, serverConnection);
		
		socket=new DatagramSocket(new InetSocketAddress(host, port));

		start();
	}

	public void run() {
		byte[] buffer = new byte[65508];
		try {
			DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
			while (socket.isConnected()) {
				packet.setLength(buffer.length);
				socket.receive(packet);
				
				if (packet.getLength()>0) {
					Data data=new Data(clientId, connectionId, Arrays.copyOf(buffer, packet.getLength()));
					serverConnection.addToTotalBytes(data);
					session.write(data);
					trafficCounter.addBytesCount(packet.getLength());
				}
			}
		} catch (Exception e) {
			logger.error(e.getMessage(),e);
		} finally {
			serverConnection.removeClientConnection(clientId, connectionId);
		}
		session.write(new ClientClose(clientId,connectionId));
	}

	public synchronized void send(Data data) throws IOException {
		if (data.getData()!=null) {
			socket.send(new DatagramPacket(data.getData(), data.getData().length));
			trafficCounter.addBytesCount(data.getData().length);
		}
	}

	public void close() {
		if (socket!=null) {
			socket.close();
		}
	}
}
