package com.socketrelay.host;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.socketrelay.messages.Data;

public class ServerConnectionThread extends Thread {
	private static final Logger logger=LoggerFactory.getLogger(ServerConnectionThread.class);
	
	private ServerSocket serverSocket;
	private IoSession session;
	private Map<String,ClientConnectionThread> clientConnectionThreads=new HashMap<>();

	private int nextClientId=1;
	
	public ServerConnectionThread(IoSession session, ServerSocket serverSocket) throws IOException{
		this.serverSocket=serverSocket;
		this.session=session;
		setDaemon(true);
	}
	
	public void removeChild(ClientConnectionThread clientConnectionThread) {
		synchronized (clientConnectionThreads) {
			clientConnectionThreads.remove(clientConnectionThread.getClientId()+clientConnectionThread.getConnectionId());
		}
	}
	
	public void close() {
		List<ClientConnectionThread> clientsToClose=null;
		synchronized (clientConnectionThreads) {
			clientsToClose=new ArrayList<>(clientConnectionThreads.values());
		}
		for (ClientConnectionThread clientToClose:clientsToClose) {
			clientToClose.close();
		}
		session.closeNow();
	}

	public void closeClient(String clientId,int connectionId) {
		ClientConnectionThread clientConnectionThread=clientConnectionThreads.get(clientId+connectionId);
		if (clientConnectionThread!=null) {
			clientConnectionThread.close();
		}
	}
	
	public void writeToClient(Data data) {
		if (data.getData()!=null) {
			ClientConnectionThread clientConnectionThread=clientConnectionThreads.get(data.getClientId()+data.getConnectionId());
			synchronized (clientConnectionThread) {
				clientConnectionThread.deliver(data);
			}
		}
	}
	
	public void run() {
		try {
			while (!serverSocket.isClosed()) {
				Socket socket=serverSocket.accept();
				if (socket!=null){
					// New connection
					logger.info("New client socket connected from "+socket.getRemoteSocketAddress().toString());
					String clientId=(((InetSocketAddress)socket.getRemoteSocketAddress()).getAddress()).toString().replace("/","");
					int connectionId=nextClientId++;
					ClientConnectionThread clientConnectionThread=new ClientConnectionThread(this, clientId, connectionId, session, socket);
					synchronized (clientConnectionThreads) {
						clientConnectionThreads.put(clientId+connectionId,clientConnectionThread);
					}
					session.write(new Data(clientId, connectionId ,null));
					clientConnectionThread.start();
				}
			}
		} catch (Exception e) {
			logger.warn(e.getMessage());
			close();
		}
	}

	
}
