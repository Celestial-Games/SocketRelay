package com.socketrelay.client;

import com.socketrelay.client.beans.Server;
import com.socketrelay.messages.GameStarted;

public interface ConnectionListener {
	public void clientConnectedChanged(int clients, int connections);
	public void serverConnectedClosed();
	public void gameConnected();
	public void receiveGameStarted(Server server, GameStarted gameStarted);
}
