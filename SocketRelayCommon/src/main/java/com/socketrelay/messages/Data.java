package com.socketrelay.messages;

import java.io.Serializable;

public class Data implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private String clientId;
	private int connectionId;
	private byte[] data;
	
	public Data() {
	}
	
	public Data(String clientId, int connectionId, byte[] data) {
		super();
		this.clientId = clientId;
		this.connectionId = connectionId;
		this.data = data;
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

	public byte[] getData() {
		return data;
	}

	public void setData(byte[] data) {
		this.data = data;
	}
}
