package com.socketrelay.client;

import java.io.IOException;
import java.net.ConnectException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.socketrelay.client.beans.Game;
import com.socketrelay.client.beans.Server;
import com.socketrelay.messages.Configuration;

public abstract class PlayerConnection extends Thread implements TrafficCounterSource {
	protected Server server;
	protected Game game;
	protected Configuration configuration=null;

	private List<ConnectionListener> cConnectionListeners=new ArrayList<>();
	
	private Map<String,TrafficCounter> trafficCounterMap=new HashMap<>();
	protected TrafficCounter trafficCounter;
	
	public PlayerConnection(Server server,Game game){
		this.server=server;
		this.game=game;
		trafficCounter=new TrafficCounter("me");
		trafficCounterMap.put("me",trafficCounter);
	}

	public Map<String,TrafficCounter> getTrafficCounters(){
		return trafficCounterMap;
	}

	public void addConnectionListener(ConnectionListener connectionListener) {
		cConnectionListeners.remove(connectionListener);
		cConnectionListeners.add(connectionListener);
	}

	public void removeConnectionListener(ConnectionListener connectionListener) {
		cConnectionListeners.remove(connectionListener);
	}

	public Configuration getConfiguration() {
		return configuration;
	}

	protected void sendClientsCount(int connectionsCount) {
		for (ConnectionListener configurationListener:cConnectionListeners) {
			configurationListener.clientConnectedChanged(1, connectionsCount);
		}
	}

	protected void sendGameConnected() {
		for (ConnectionListener configurationListener:cConnectionListeners) {
			configurationListener.gameConnected();
		}
	}

	abstract public long getTotalBytes();
	abstract public void connect() throws UnknownHostException, IOException, ConnectException;
	abstract public void close();

}
