package com.socketrelay.client;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.net.ConnectException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.logging.LogManager;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.UIManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.socketrelay.client.swing.TextAreaOutputStream;
import com.socketrelay.messages.Configuration;

public class SocketRelayClient extends JFrame implements ConnectionListener{
	private static final long serialVersionUID = 1L;
	
	private static final Logger logger=LoggerFactory.getLogger(SocketRelayClient.class); 

	private Properties serverProperties=new Properties();
	private Properties gamesProperties=new Properties();

	private CardLayout cardLayout=new CardLayout();
	private JPanel cardPanel=new JPanel(cardLayout);
	
	private JComboBox<String> serverComboBox;
	private JComboBox<String> gameComboBox;

	private JLabel connectionLabel=new JLabel();
	private JLabel gameLabel=new JLabel();
	private JLabel connectionsLabel=new JLabel();
	
	private String serverIp;
	private ServerConnection serverConnection;
	
	public SocketRelayClient() throws FileNotFoundException, IOException {
		super("Socket Relay");
		
		loadIcons();
		
		loadDetails();

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		build();
		
		pack();
		
		setLocationRelativeTo(null);

		setVisible(true);
	}
	
	private void loadIcons() throws IOException {
		List<Image> icons=new ArrayList<>();
		icons.add(ImageIO.read(ClassLoader.getSystemResourceAsStream("images/icon16.png")));
		icons.add(ImageIO.read(ClassLoader.getSystemResourceAsStream("images/icon24.png")));
		icons.add(ImageIO.read(ClassLoader.getSystemResourceAsStream("images/icon32.png")));
		icons.add(ImageIO.read(ClassLoader.getSystemResourceAsStream("images/icon48.png")));
		icons.add(ImageIO.read(ClassLoader.getSystemResourceAsStream("images/icon64.png")));
		icons.add(ImageIO.read(ClassLoader.getSystemResourceAsStream("images/icon128.png")));
		icons.add(ImageIO.read(ClassLoader.getSystemResourceAsStream("images/icon256.png")));
		setIconImages(icons);
	}
	
	private void loadDetails() throws FileNotFoundException, IOException {
		serverProperties.load(new FileInputStream(new File("servers.properties")));
		gamesProperties.load(new FileInputStream(new File("games.properties")));
	}
	
	private JPanel buildConnectPanel() {
		JPanel panel=new JPanel(new GridBagLayout());
		
		GridBagConstraints gbc=new GridBagConstraints();
		serverComboBox=new JComboBox<String>(Collections.list(serverProperties.keys()).toArray(new String[0]));
		gbc.insets=new Insets(3, 15, 3, 15);
		gbc.gridx=0;
		gbc.gridy=0;
		gbc.gridwidth=2;
		gbc.weightx=1000;
		gbc.anchor=GridBagConstraints.NORTHWEST;
		gbc.fill=GridBagConstraints.BOTH;
		panel.add(new JLabel("Pick a server"),gbc);

		gbc.gridy++;
		panel.add(serverComboBox,gbc);

		gbc.gridy++;
		panel.add(new JLabel(),gbc);
		
		gbc.gridy++;
		panel.add(new JLabel("Pick a game"),gbc);
		
		gameComboBox=new JComboBox<String>(Collections.list(gamesProperties.keys()).toArray(new String[0]));
		gbc.gridy++;
		panel.add(gameComboBox,gbc);
		
		gbc.gridy++;
		panel.add(new JLabel(),gbc);
		
		gbc.gridx=1;
		gbc.gridwidth=1;
		gbc.weightx=0;
		gbc.gridy++;
		panel.add(new JButton(new ConnectAction()),gbc);
		
		gbc.gridx=0;
		gbc.gridy++;
		gbc.weightx=1000;
		gbc.weighty=1000;
		panel.add(new JLabel(),gbc);
		
		return panel;
	}
	
	private JPanel buildConnectedPanel() {
		JPanel panel=new JPanel(new GridBagLayout());
		
		GridBagConstraints gbc=new GridBagConstraints();
		gbc.insets=new Insets(3, 15, 3, 15);
		gbc.gridx=0;
		gbc.gridy=0;
		gbc.gridwidth=2;
		gbc.weightx=1000;
		gbc.anchor=GridBagConstraints.NORTHWEST;
		gbc.fill=GridBagConstraints.BOTH;
		panel.add(new JLabel("Server Connection"),gbc);

		gbc.gridy++;
		panel.add(connectionLabel,gbc);

		gbc.gridy++;
		panel.add(new JLabel(),gbc);
		
		gbc.gridy++;
		panel.add(new JLabel("Game Type"),gbc);
		
		gbc.gridy++;
		panel.add(gameLabel,gbc);
		
		gbc.gridy++;
		panel.add(new JLabel(),gbc);

		gbc.gridy++;
		gbc.weightx=0;
		panel.add(connectionsLabel,gbc);

		gbc.gridx=1;
		gbc.gridwidth=1;
		panel.add(new JButton(new CloseAction()),gbc);
		
		gbc.gridx=0;
		gbc.gridy++;
		gbc.weightx=1000;
		gbc.weighty=1000;
		panel.add(new JLabel(),gbc);
		
		return panel;
	}

	private JPanel buildTopPanel() {
		cardPanel.add(buildConnectPanel(),"Connect");
		cardPanel.add(buildConnectedPanel(),"Connected");
		return cardPanel;
	}
	
	private JPanel buildLogPanel() {
		JPanel panel=new JPanel(new BorderLayout());
		
		JTextArea textArea=new JTextArea();
		textArea.setEditable(false);

		PrintStream printStream = new PrintStream(new TextAreaOutputStream(textArea));
		System.setOut(printStream);
		System.setErr(printStream);
		
		JScrollPane jScrollPane=new JScrollPane(textArea);
		jScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		jScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		
		panel.add(jScrollPane,BorderLayout.CENTER);
		
		panel.setMinimumSize(new Dimension(600, 400));
		panel.setPreferredSize(new Dimension(600, 400));
		
		return panel;
	}

	@Override
	public void receiveConfiguration(Configuration configuration) {
		// TODO: Move this into a shared method
		String name=serverProperties.getProperty(serverIp);
		name=name.substring(0,name.indexOf(":"));
		connectionLabel.setText("<html><b>"+name+":"+configuration.getClientPort()+"</b></html>");
	}
	
	public void clientConnectedChanged(int clients) {
		connectionsLabel.setText("<html>Clients: <b>"+clients+"</b></html>");
	}
	
	public void serverConnectedClosed() {
		cardLayout.show(cardPanel, "Connect");
	}
	
	private void build(){
		JPanel mainPanel=new JPanel(new BorderLayout());
		mainPanel.add(buildTopPanel(),BorderLayout.NORTH);
		mainPanel.add(buildLogPanel(),BorderLayout.CENTER);
		
		getContentPane().add(mainPanel);
	}

	class ConnectAction extends AbstractAction {
		private static final long serialVersionUID = 1L;

		public ConnectAction() {
			putValue(NAME, "Connect");
		}
		
		@Override
		public void actionPerformed(ActionEvent actionEvent) {
			// TODO: Split out url from port here
			serverIp=serverComboBox.getModel().getSelectedItem().toString();
			serverConnection=new ServerConnection(serverProperties.getProperty(serverIp),gamesProperties.getProperty((String)gameComboBox.getSelectedItem()));
			serverConnection.addConnectionListener(SocketRelayClient.this);
			try {
				serverConnection.connect();
				gameLabel.setText("<html><b>"+gameComboBox.getSelectedItem()+"</b></html>");
				cardLayout.show(cardPanel, "Connected");
			} catch (ConnectException connectException) {
				JOptionPane.showMessageDialog(null, "Connection was refused to destination.\n\n"+serverProperties.getProperty(serverIp),"Unable to connect to server",JOptionPane.WARNING_MESSAGE);
				serverConnection.close();
			} catch (IOException e) {
				logger.error(e.getMessage(),e);
				serverConnection.close();
			}
		}
	}

	class CloseAction extends AbstractAction {
		private static final long serialVersionUID = 1L;

		public CloseAction() {
			putValue(NAME, "Close");
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			serverConnection.close();
		}
		
	}

	class CopyConnection extends AbstractAction {
		private static final long serialVersionUID = 1L;

		public CopyConnection() {
			putValue(NAME, "Copy URL");
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
		}
		
	}
	
	
	static public void main(String[] args) {
		try {
			File loggingConfigFile=new File("logging.properties");
		    if (loggingConfigFile.exists()) {
			    FileInputStream loggingConfigFileStream = new FileInputStream(loggingConfigFile);
		    	LogManager.getLogManager().readConfiguration(loggingConfigFileStream);
		    	loggingConfigFileStream.close();
		    }
		} catch (IOException ex) {
			logger.error(ex.getMessage(),ex);
		    System.exit(-1);
		}
		
		try {
			UIManager.setLookAndFeel( UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			
		}

		try {
			new SocketRelayClient();
			
			logger.info("Started Socket Relay Client");
		} catch (IOException e) {
			logger.error(e.getMessage(),e);
			JOptionPane.showMessageDialog(null, "Unable to start system.\n\n"+e.getMessage(),"Erorr Starting!",JOptionPane.ERROR_MESSAGE);
			System.exit(-1);
		}
	}

}
