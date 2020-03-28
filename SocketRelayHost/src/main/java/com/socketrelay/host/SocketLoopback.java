package com.socketrelay.host;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SocketLoopback extends Thread {
	private static final Logger logger=LoggerFactory.getLogger(SocketLoopback.class);

	private ServerSocket serverSocket;
	private List<Socket> openSockets=new ArrayList<>();
	
	public SocketLoopback(int port) throws IOException {
		serverSocket=new ServerSocket(port);
		setDaemon(true);
		start();
	}

	public void close() {
		try {
			serverSocket.close();
		} catch (IOException e) {
			logger.warn(e.getMessage(),e);
		}
	}

	private void startClientSocket(final Socket socket) {
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				System.out.println("New Connection");
				openSockets.add(socket);
				try {
					InputStream inputStream=socket.getInputStream();
					OutputStream outputStream=socket.getOutputStream();
					
					byte[] buffer=new byte[1024];
					while (socket.isConnected()) {
						int len=inputStream.read(buffer);
						if (len>0) {
							outputStream.write(buffer, 0, len);
							//System.out.println(new String(buffer, 0, len));
						} else if (len==-1) {
							socket.close();
							break;
						}
					}
				} catch (Exception e) {
					logger.warn(e.getMessage(),e);
				} finally {
					openSockets.remove(socket);
					try {
						socket.close();
					} catch (IOException e) {
						logger.warn(e.getMessage(),e);
					}
				}
			}
		}).start();
		
	}
	
	public void run() {
		while (!serverSocket.isClosed()) {
			try {
				Socket socket=serverSocket.accept();
				startClientSocket(socket);
			} catch (IOException e) {
				logger.warn(e.getMessage(),e);
				close();
			}
		}
	}
}
