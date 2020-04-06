package com.socketrelay.client;

import java.io.IOException;
import java.net.UnknownHostException;

import org.apache.mina.core.session.IoSession;

import com.socketrelay.messages.Data;

public abstract class ClientConnection extends Thread {

	protected IoSession session;
	protected String clientId;
	protected int connectionId;
	protected TrafficCounter trafficCounter;
	protected ServerConnection serverConnection;

	public ClientConnection(IoSession session, String clientId, int connectionId, String host,int port, TrafficCounter trafficCounter, ServerConnection serverConnection) throws UnknownHostException, IOException {
		this.session=session;
		this.clientId=clientId;
		this.connectionId=connectionId;
		this.trafficCounter=trafficCounter;
		this.serverConnection=serverConnection;
		
		setDaemon(true);
	}
	
	public abstract void send(Data data) throws IOException;
	public abstract void close();
}
