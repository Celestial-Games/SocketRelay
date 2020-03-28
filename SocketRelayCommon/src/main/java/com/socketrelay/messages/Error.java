package com.socketrelay.messages;

import java.io.Serializable;

public class Error implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private String clientId;
	private String error;
	
	public Error() {
		super();
	}

	public Error(String error) {
		super();
		this.error = error;
	}

	public Error(String clientId, String error) {
		super();
		this.clientId = clientId;
		this.error = error;
	}

	public String getClientId() {
		return clientId;
	}
	
	public void setClientId(String clientId) {
		this.clientId = clientId;
	}
	
	public String getError() {
		return error;
	}
	
	public void setError(String error) {
		this.error = error;
	}
}
