package com.socketrelay.host;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Arrays;

import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.socketrelay.messages.ClientClose;
import com.socketrelay.messages.Data;

public class ClientConnectionThread extends Thread {
	private static final Logger logger=LoggerFactory.getLogger(ClientConnectionThread.class);
	
	private InputStream input;
	private OutputStream output;
	private Socket socket;
	private IoSession session;
	private String clientId;
	private int connectionId;
	private ServerConnectionThreadTcp parent;
	
	// TODO: using ServerConnectionThread as parent here is ugly. Needs a refactor.
	public ClientConnectionThread(ServerConnectionThreadTcp parent, String clientId, int connectionId, IoSession session, Socket socket) throws IOException{
		this.parent=parent;
		this.clientId=clientId;
		this.connectionId=connectionId;
		this.socket=socket;
		this.session=session;
		input=socket.getInputStream();
		output=socket.getOutputStream();
		setDaemon(true);
	}
	
	public String getClientId() {
		return clientId;
	}

	public int getConnectionId() {
		return connectionId;
	}

	public void close() {
		parent.removeChild(this);
		session.write(new ClientClose(clientId,connectionId));
		session.setAttribute(clientId,null);
		try {
			if (!socket.isClosed()) {
				socket.close();
			}
		} catch (IOException e) {
			logger.warn(e.getMessage(),e);
		}
	}
	
	public void deliver(Data data) {
		try {
			output.write(data.getData());
		} catch (IOException e) {
			logger.warn(e.getMessage());
			close();
		}
	}
	
	public void run() {
		byte[] buffer=new byte[16*1024];
		try {
			while (socket.isConnected()) {
				int r=input.read(buffer);
				if (r>0) {
					session.write(new Data(clientId,connectionId,Arrays.copyOf(buffer, r)));
				} else if (r==-1) {
					close();
					break;
				}
			}
		} catch (Exception e) {
			logger.warn(e.getMessage());
			close();
		}
	}
}
