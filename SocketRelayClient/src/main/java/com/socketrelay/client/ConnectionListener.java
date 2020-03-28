package com.socketrelay.client;

import com.socketrelay.messages.Configuration;

public interface ConnectionListener {
	public void receiveConfiguration(Configuration configuration);
	public void clientConnectedChanged(int clients);
	public void serverConnectedClosed();
}
