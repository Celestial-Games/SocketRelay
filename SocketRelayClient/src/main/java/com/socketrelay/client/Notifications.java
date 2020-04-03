package com.socketrelay.client;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.Timer;
import javax.swing.border.BevelBorder;

public class Notifications {
	private static final List<Notification> notifications=new ArrayList<>();
	
	private static Timer timer;
	
	private static SocketRelayClient mainFrame;
	
	public static void init(SocketRelayClient main) {
		mainFrame=main;
		mainFrame.addComponentListener(new ComponentListener() {
			
			@Override
			public void componentShown(ComponentEvent e) {
			}
			
			@Override
			public void componentResized(ComponentEvent e) {
			}
			
			@Override
			public void componentMoved(ComponentEvent e) {
				if (notifications.size()>0) {
					synchronized (notifications) {
						for (Notification notification:notifications.toArray(new Notification[notifications.size()])) {
							notification.reposition();
						}
					}
				}
			}
			
			@Override
			public void componentHidden(ComponentEvent e) {
				if (notifications.size()>0) {
					synchronized (notifications) {
						for (Notification notification:notifications.toArray(new Notification[notifications.size()])) {
							notification.end();
						}
					}
				}
			}
		});
			
		timer=new Timer(1000, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (notifications.size()>0) {
					synchronized (notifications) {
						for (Notification notification:notifications.toArray(new Notification[notifications.size()])) {
							notification.testTime();
						}
					}
				}
			}
		});
		timer.setRepeats(true);
		timer.setDelay(100);
		timer.start();
	}
	
	public static void serverRejectedConnection(String serverName,String url,int port, String exceptionMessages) {
		JOptionPane.showMessageDialog(null,
				"<html><h2>Server connection failed.</h2><br />"+
				"Unable to connect to <i>"+serverName+"</i> <b>"+url+":"+port+"</b><br />"+
				exceptionMessages+"</html>" ,
				"Server Connection Failed!",JOptionPane.ERROR_MESSAGE);
	}

	public static void unableToBindLocalPort(int port) {
		JOptionPane.showMessageDialog(null,
				"<html><h2>Local socket bind failed.</h2><br />"+
				"We were unable to bind to the port <b>"+port+"</b> locally.</html>" ,
				"Local Bind Failed!",JOptionPane.ERROR_MESSAGE);
	}
	
	public static void clientJoined() {
		new Notification("<html>Player has connected from the Socket Relay.</html>");
	}
	
	public static void clientLeft() {
		new Notification("<html>Player has disconnected from the Socket Relay.</html>");
	}

	public static void connectedToserver() {
		new Notification("<html>Connection to the server established.</html>");
	}

	static class Notification {
		private String description;
		private long showTime;
		private JDialog dialog;
		
		private Notification(String description) {
			this.description=description;
			showTime=System.currentTimeMillis();
			build();
			synchronized (notifications) {
				notifications.add(this);
			}
			reposition();
			dialog.setVisible(true);
		}
		
		private void testTime() {
			if (showTime+2500<System.currentTimeMillis()) {
				end();
			} else {
				reposition();
			}
		}

		private void reposition() {
			int index=notifications.indexOf(this);
			System.out.println("index="+index);
			Point bottomRight=mainFrame.getBottomRightCornerOffset(-dialog.getWidth()-5,-((index+1)*(dialog.getHeight()+5)));
			dialog.setLocation(bottomRight);
		}
		
		private void end() {
			synchronized (notifications) {
				notifications.remove(this);
			}
			if (dialog!=null) {
				dialog.setVisible(false);
				dialog.dispose();
				dialog=null;
			}
		}
		
		private void build() {
			dialog=new JDialog(mainFrame);
			dialog.setUndecorated(true);
			
			JPanel panel=new JPanel(new BorderLayout());
			panel.setBorder(
					BorderFactory.createCompoundBorder(
							BorderFactory.createBevelBorder(BevelBorder.RAISED, dialog.getBackground().brighter(), dialog.getBackground().darker()),
							BorderFactory.createEmptyBorder(2, 5, 2, 5)
							)
					);
			
			JLabel label=new JLabel(description);
			label.setForeground(Color.yellow);

			panel.add(label,BorderLayout.CENTER);
			
			dialog.getContentPane().add(panel);
			dialog.setSize(200, 40);
		}
	}
}
