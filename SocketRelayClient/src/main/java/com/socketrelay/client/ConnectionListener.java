package com.socketrelay.client;

import com.socketrelay.client.beans.Server;
import com.socketrelay.messages.Configuration;

public interface ConnectionListener {
	public void receiveConfiguration(Server server, Configuration configuration);
	public void clientConnectedChanged(int clients);
	public void serverConnectedClosed();
}
