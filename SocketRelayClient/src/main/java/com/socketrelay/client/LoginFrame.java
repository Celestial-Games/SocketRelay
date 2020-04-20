package com.socketrelay.client;

import java.awt.CardLayout;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class LoginFrame extends JDialog {
	private static final long serialVersionUID = 1L;

	private JPanel mainPanel;
	private CardLayout cardLayout;
	
	public LoginFrame() {
		super((JFrame)null, "Socket Relay Login");

		setIconImages(Images.getIcons());

		build();
		pack();
		setLocationByPlatform(true);
		setVisible(true);
	}
	
	private void build() {
		cardLayout=new CardLayout();
		mainPanel=new JPanel(cardLayout);
	}
}
