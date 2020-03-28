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

public class ClientConnection extends Thread {
	private static final Logger logger=LoggerFactory.getLogger(ClientConnection.class);

	private Socket socket;
	private InputStream input;
	private OutputStream output;
	private IoSession session;
	private String clientId;

	public ClientConnection(IoSession session, String clientId, String host,int port) throws UnknownHostException, IOException {
		this.session=session;
		this.clientId=clientId;

		socket=new Socket(host, port);
		input=socket.getInputStream();
		output=socket.getOutputStream();
		setDaemon(true);
		start();
	}

	public void run() {
		byte[] buffer=new byte[16*1024];
		try {
			while (socket.isConnected()) {
				int len=input.read(buffer);
				if (len>0) {
					session.write(new Data(clientId,Arrays.copyOf(buffer, len)));
				} else if (len==-1) {
					break;
				}
			}
		} catch (Exception e) {
			logger.error(e.getMessage(),e);
		}
		session.write(new ClientClose(clientId));
	}

	public synchronized void send(Data data) throws IOException {
		if (data.getData()!=null) {
			output.write(data.getData());
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
