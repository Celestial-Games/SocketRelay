package com.socketrelay.messages;

import java.io.Serializable;

public class ClientClose implements Serializable{
	private static final long serialVersionUID = 1L;
	
	private String clientId;
	
	public ClientClose() {
	}
	
	public ClientClose(String clientId) {
		super();
		this.clientId = clientId;
	}

	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

}
