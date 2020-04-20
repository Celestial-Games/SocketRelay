package com.socketrelay.client;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.net.ConnectException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.LogManager;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.socketrelay.client.beans.Config;
import com.socketrelay.client.beans.Game;
import com.socketrelay.client.beans.Server;
import com.socketrelay.client.swing.TextAreaOutputStream;
import com.socketrelay.messages.Configuration;

public class SocketRelayClient extends JFrame implements ConnectionListener {
	private static final long serialVersionUID = 1L;
	
	private static final Logger logger = LoggerFactory.getLogger(SocketRelayClient.class);
	private static final Gson gson = new GsonBuilder().create();

	private Config config;
	private Map<String, Server> servers = new HashMap<>();
	private Map<String, Game> games = new HashMap<>();
	private List<String> serverNames = new ArrayList<>();
	private List<String> gameNames = new ArrayList<>();

	private JPanel mainPanel;
	private CardLayout cardLayout = new CardLayout();
	private JPanel cardPanel = new JPanel(cardLayout);

	private JComboBox<String> serverComboBox;
	private JComboBox<String> gameComboBox;

	private JLabel connectionLabel = new JLabel();
	private JLabel gameLabel = new JLabel();
	private JLabel clientsLabel = new JLabel();
	private JLabel connectionsLabel = new JLabel();

	private TrafficImage trafficImage;

	private ServerConnection serverConnection;
	private PlayerConnection playerConnection;

	public SocketRelayClient() throws FileNotFoundException, IOException {
		super("Socket Relay");

		setIconImages(Images.getIcons());

		loadDetails();

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		build();

		pack();

		setMinimumSize(new Dimension(getWidth(), getHeight()));

		setLocationRelativeTo(null);

		setVisible(true);
	}

	public Point getBottomRightCornerOffset(int xoffset, int yoffset) {
		Point location = new Point(mainPanel.getWidth() + xoffset, mainPanel.getHeight() + yoffset);
		SwingUtilities.convertPointToScreen(location, mainPanel);
		return location;
	}

	private void loadDetails() throws FileNotFoundException, IOException {
		File configFile = new File("config.json");
		if (configFile.exists()) {
			config = gson.fromJson(new FileReader(configFile), Config.class);
			for (Server server : config.getServers()) {
				if (!servers.containsKey(server.getName())) {
					servers.put(server.getName(), server);
					serverNames.add(server.getName());
				} else {
					logger.error("Config has a duplicated server '" + server.getName()
							+ "' ignoring all but the first one.");
				}
			}
			for (Game game : config.getGames()) {
				if (!games.containsKey(game.getName())) {
					games.put(game.getName(), game);
					gameNames.add(game.getName());
				} else {
					logger.error(
							"Config has a duplicated game '" + game.getName() + "' ignoring all but the first one.");
				}
			}
		} else {
			JOptionPane.showMessageDialog(null, "Unable to find config.json file.", "Unable to start",
					JOptionPane.ERROR_MESSAGE);
			System.exit(-1);
		}
	}

	private JPanel buildConnectPanel() {
		JPanel outerPanel = new JPanel(new BorderLayout());
		JPanel panel = new JPanel(new GridBagLayout());

		GridBagConstraints gbc = new GridBagConstraints();
		serverComboBox = new JComboBox<String>(serverNames.toArray(new String[serverNames.size()]));
		gbc.insets = new Insets(3, 15, 3, 15);
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.weightx = 1000;
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.fill = GridBagConstraints.BOTH;
		panel.add(new JLabel("Pick a server"), gbc);

		gbc.gridy++;
		panel.add(serverComboBox, gbc);

		gbc.gridy++;
		panel.add(new JLabel(), gbc);

		gbc.gridy++;
		panel.add(new JLabel("Pick a game"), gbc);

		gameComboBox = new JComboBox<String>(gameNames.toArray(new String[gameNames.size()]));
		gbc.gridy++;
		panel.add(gameComboBox, gbc);

		gbc.gridy++;
		panel.add(new JLabel(), gbc);

		JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		buttonsPanel.add(new JButton(new ConnectPlayerAction()));
		buttonsPanel.add(new JButton(new ConnectHostAction()));

		outerPanel.add(panel, BorderLayout.CENTER);
		outerPanel.add(buttonsPanel, BorderLayout.SOUTH);

		return outerPanel;
	}

	private JPanel buildConnectedPanel() {
		JPanel outerPanel = new JPanel(new BorderLayout());
		JPanel panel = new JPanel(new GridBagLayout());

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(3, 15, 3, 15);
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.weightx = 1000;
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.fill = GridBagConstraints.BOTH;
		panel.add(new JLabel("Server Connection"), gbc);

		gbc.gridy++;
		panel.add(connectionLabel, gbc);

		gbc.gridy++;
		panel.add(new JLabel(), gbc);

		gbc.gridy++;
		panel.add(new JLabel("Game Type"), gbc);

		gbc.gridy++;
		panel.add(gameLabel, gbc);

		gbc.gridy++;
		panel.add(new JLabel(), gbc);

		Box labelsBox = Box.createHorizontalBox();
		labelsBox.add(clientsLabel);
		labelsBox.add(connectionsLabel);

		gbc.gridwidth = 1;
		gbc.gridy++;
		gbc.weighty = 0;
		gbc.weightx = 0;
		panel.add(labelsBox, gbc);

		JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		buttonsPanel.add(new JButton(new CloseAction()));

		outerPanel.add(panel, BorderLayout.CENTER);
		outerPanel.add(buttonsPanel, BorderLayout.SOUTH);

		return outerPanel;
	}

	private JPanel buildTopPanel() {
		cardPanel.add(buildConnectPanel(), "Connect");
		cardPanel.add(buildConnectedPanel(), "Connected");
		return cardPanel;
	}

	private JPanel buildTrafficPanel() {
		JPanel panel = new JPanel(new BorderLayout());

		trafficImage = new TrafficImage();
		panel.add(trafficImage);

		return panel;
	}

	private JPanel buildLogPanel() {
		JPanel panel = new JPanel(new BorderLayout());

		JTextArea textArea = new JTextArea();
		textArea.setEditable(false);

		PrintStream printStream = new PrintStream(new TextAreaOutputStream(textArea));
		System.setOut(printStream);
		System.setErr(printStream);

		JScrollPane jScrollPane = new JScrollPane(textArea);
		jScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		jScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

		panel.add(jScrollPane, BorderLayout.CENTER);

		return panel;
	}

	@Override
	public void receiveConfiguration(Server server, Configuration configuration) {
		connectionLabel.setText("<html><b>" + server.getIp() + ":" + configuration.getClientPort() + "</b></html>");
	}

	@Override
	public void gameConnected() {
		connectionLabel.setText("Game connected attempting to connect to server ...");
	}

	@Override
	public void clientConnectedChanged(int clients, int connections) {
		clientsLabel.setText("<html>Clients: <b>" + clients + "</b></html>");
		connectionsLabel.setText("<html>Connections: <b>" + connections + "</b></html>");
	}

	public void serverConnectedClosed() {
		connectServer();
		connectionLabel.setText("Connecting...");
		clientConnectedChanged(0, 0);
	}

	private void connectServer() {
		Server server = servers.get(serverComboBox.getModel().getSelectedItem().toString());
		Game game = games.get(gameComboBox.getSelectedItem().toString());
		serverConnection = new ServerConnection(server, game);
		serverConnection.addConnectionListener(SocketRelayClient.this);
		serverConnection.connect();
		gameLabel.setText("<html><b>" + gameComboBox.getSelectedItem() + "</b></html>");
		connectionLabel.setText("Connecting...");
		clientConnectedChanged(0, 0);
		cardLayout.show(cardPanel, "Connected");

		trafficImage.setTrafficCounterSource(serverConnection);
	}
	
	private JTabbedPane buildLowerPanel() {
		JTabbedPane tabs = new JTabbedPane(JTabbedPane.BOTTOM);

		tabs.addTab("Data", buildTrafficPanel());
		tabs.addTab("Logs", buildLogPanel());

		tabs.setMinimumSize(new Dimension(600, 400));
		tabs.setPreferredSize(new Dimension(600, 400));

		return tabs;
	}

	private void build() {
		mainPanel = new JPanel(new BorderLayout());
		mainPanel.add(buildTopPanel(), BorderLayout.NORTH);
		mainPanel.add(buildLowerPanel(), BorderLayout.CENTER);

		getContentPane().add(mainPanel);
	}

	class ConnectHostAction extends AbstractAction {
		private static final long serialVersionUID = 1L;

		public ConnectHostAction() {
			putValue(NAME, "Connect Host");
		}

		@Override
		public void actionPerformed(ActionEvent actionEvent) {
			connectServer();
		}
	}

	class ConnectPlayerAction extends AbstractAction {
		private static final long serialVersionUID = 1L;

		public ConnectPlayerAction() {
			putValue(NAME, "Connect as Player");
		}

		@Override
		public void actionPerformed(ActionEvent actionEvent) {
			String portString = JOptionPane.showInputDialog(null, "Enter the port number to connect to.", "Port Number",
					JOptionPane.OK_CANCEL_OPTION);
			try {
				if (portString != null) {
					int port = Integer.parseInt(portString);
					Server server = servers.get(serverComboBox.getModel().getSelectedItem().toString());
					Game game = games.get(gameComboBox.getSelectedItem().toString());

					switch (game.getProtocol()) {
					case TCP:
						playerConnection = new PlayerConnectionTcp(server, game, port);
					case UDP:
						playerConnection = new PlayerConnectionUdp(server, game, port);
					}
					playerConnection.addConnectionListener(SocketRelayClient.this);
					try {
						playerConnection.connect();
						gameLabel.setText("<html><b>" + gameComboBox.getSelectedItem() + "</b></html>");
						connectionLabel.setText("Waiting for game to connect on port "+game.getPort()+"...");
						clientConnectedChanged(0, 0);
						cardLayout.show(cardPanel, "Connected");
						trafficImage.setTrafficCounterSource(playerConnection);

					} catch (ConnectException connectException) {
						JOptionPane
								.showMessageDialog(null,
										"Connection was refused port may be bound locally.\n\n" + server.getIp() + ":"
												+ server.getPort(),
										"Unable to listen locally", JOptionPane.WARNING_MESSAGE);
						playerConnection.close();
					} catch (IOException e) {
						logger.error(e.getMessage(), e);
						playerConnection.close();
					}
				}
			} catch (NumberFormatException e) {
				logger.warn(e.getMessage(), e);
				JOptionPane.showInputDialog(null,
						"The port number was not formatted correctly, it must just be an integer number.",
						"Port Number Error", JOptionPane.WARNING_MESSAGE);
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
			trafficImage.setTrafficCounterSource(null);
			if (serverConnection != null) {
				serverConnection.close();
				serverConnection = null;
			}
			if (playerConnection != null) {
				playerConnection.close();
				playerConnection = null;
			}
			cardLayout.show(cardPanel, "Connect");
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
			UIManager.put("control", new Color(0x1f2833));
			UIManager.put("info", new Color(128, 128, 128));
			UIManager.put("nimbusBase", new Color(18, 30, 49));
			UIManager.put("nimbusAlertYellow", new Color(248, 187, 0));
			UIManager.put("nimbusDisabledText", new Color(128, 128, 128));
			UIManager.put("nimbusFocus", new Color(115, 164, 209));
			UIManager.put("nimbusGreen", new Color(176, 179, 50));
			UIManager.put("nimbusInfoBlue", new Color(66, 139, 221));
			UIManager.put("nimbusLightBackground", new Color(18, 30, 49));
			UIManager.put("nimbusOrange", new Color(191, 98, 4));
			UIManager.put("nimbusRed", new Color(169, 46, 34));
			UIManager.put("nimbusSelectedText", new Color(255, 255, 255));
			UIManager.put("nimbusSelectionBackground", new Color(104, 93, 156));
			UIManager.put("text", new Color(230, 230, 230));
			for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
				if ("Nimbus".equals(info.getName())) {
					UIManager.setLookAndFeel(info.getClassName());
					break;
				}
			}
		} catch (Exception e) {
			try {
				UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			} catch (Exception e2) {

			}
		}

		try {
			File loggingConfigFile = new File("logging.properties");
			if (loggingConfigFile.exists()) {
				FileInputStream loggingConfigFileStream = new FileInputStream(loggingConfigFile);
				LogManager.getLogManager().readConfiguration(loggingConfigFileStream);
				loggingConfigFileStream.close();
			}
		} catch (IOException ex) {
			logger.error(ex.getMessage(), ex);
			System.exit(-1);
		}

		try {
			SocketRelayClient socketRelayClient = new SocketRelayClient();

			Notifications.init(socketRelayClient);

			logger.info("Started Socket Relay Client");
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
			JOptionPane.showMessageDialog(null, "Unable to start system.\n\n" + e.getMessage(), "Erorr Starting!",
					JOptionPane.ERROR_MESSAGE);
			System.exit(-1);
		}
	}

}
