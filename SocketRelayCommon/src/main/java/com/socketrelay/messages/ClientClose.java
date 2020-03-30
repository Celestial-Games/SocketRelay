package com.socketrelay.messages;

import java.io.Serializable;

public class ClientClose implements Serializable{
	private static final long serialVersionUID = 1L;
	
	private String clientId;
	private int connectionId;
	
	public ClientClose() {
	}
	
	public ClientClose(String clientId, int connectionId) {
		super();
		this.clientId = clientId;
	}

	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	public int getConnectionId() {
		return connectionId;
	}

	public void setConnectionId(int connectionId) {
		this.connectionId = connectionId;
	}

}
