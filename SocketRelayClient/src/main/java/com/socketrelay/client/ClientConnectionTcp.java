package com.socketrelay.client;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Arrays;

import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.socketrelay.messages.ClientClose;
import com.socketrelay.messages.Data;

public class ClientConnectionTcp extends ClientConnection {
	private static final Logger logger=LoggerFactory.getLogger(ClientConnectionTcp.class);

	private Socket socket;
	private InputStream input;
	private OutputStream output;

	public ClientConnectionTcp(IoSession session, String clientId, int connectionId, String host,int port, TrafficCounter trafficCounter, ServerConnection serverConnection) throws UnknownHostException, IOException {
		super(session, clientId, connectionId, host,port, trafficCounter, serverConnection);
		
		socket=new Socket(host, port);
		input=socket.getInputStream();
		output=socket.getOutputStream();

		start();
	}

	public void run() {
		byte[] buffer=new byte[16*1024];
		try {
			while (socket.isConnected()) {
				int len=input.read(buffer);
				if (len>0) {
					Data data=new Data(clientId, connectionId, Arrays.copyOf(buffer, len));
					serverConnection.addToTotalBytes(data);
					session.write(data);
					trafficCounter.addBytesCount(len);
				} else if (len==-1) {
					break;
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
			output.write(data.getData());
			trafficCounter.addBytesCount(data.getData().length);
		}
	}

	public void close() {
		if (socket!=null) {
			try {
				socket.close();
			} catch (IOException e) {
				logger.warn(e.getMessage(),e);
			}
		}
	}
}
