package com.socketrelay.client;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.playfab.PlayFabClientAPI;
import com.playfab.PlayFabClientModels.GetPlayerCombinedInfoRequestParams;
import com.playfab.PlayFabClientModels.LoginResult;
import com.playfab.PlayFabClientModels.LoginWithEmailAddressRequest;
import com.playfab.PlayFabClientModels.RegisterPlayFabUserRequest;
import com.playfab.PlayFabClientModels.RegisterPlayFabUserResult;
import com.playfab.PlayFabClientModels.SendAccountRecoveryEmailRequest;
import com.playfab.PlayFabClientModels.SendAccountRecoveryEmailResult;
import com.playfab.PlayFabErrors.PlayFabResult;

public class LoginFrame extends JFrame {
	private static final long serialVersionUID = 1L;
	private static final Logger logger = LoggerFactory.getLogger(LoginFrame.class);

	private JPanel mainPanel;
	private CardLayout cardLayout;
	
	private JTextField loginEmail=new JTextField();
	private JPasswordField loginPassword=new JPasswordField();
	private JCheckBox stayLoggenIn=new JCheckBox("Stay logged in");

	private JTextField registerEmail=new JTextField();
	private JPasswordField registerPassword=new JPasswordField();
	private JPasswordField registerPasswordRepeat=new JPasswordField();
	private JTextField registerHandle=new JTextField();

	private JTextField recoveryEmail=new JTextField();
	
	public LoginFrame() {
		super("Socket Relay Login");

		setIconImages(Images.getIcons());

		build();
		setSize(400, 350);
		setResizable(false);
		setLocationRelativeTo(null);
		setVisible(true);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
	}

	private JPanel buildRecovery() {
		JPanel center=new JPanel(new GridBagLayout());
		center.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
		GridBagConstraints gbc=new GridBagConstraints();
		gbc.fill=WIDTH;
		gbc.weightx=100;
		gbc.gridy++;
		center.add(new JLabel("Email"),gbc);
		gbc.gridy++;
		center.add(recoveryEmail,gbc);
		gbc.gridy++;
		gbc.weighty=100;
		center.add(new JLabel(""),gbc);
		gbc.gridy++;
		gbc.weighty=0;
		gbc.anchor=GridBagConstraints.WEST;
		
		center.add(SwingUtils.createBlueButton(new ShowLoginAction()),gbc);
		gbc.gridy++;
		center.add(SwingUtils.createBlueButton(new ShowRegisterAction()),gbc);
		
		JPanel buttons=new JPanel(new FlowLayout(FlowLayout.RIGHT));
		buttons.add(new JButton(new RecoveryAction()));
		
		JPanel topPanel=new JPanel(new BorderLayout());
		topPanel.add(new JLabel("<html><h2>Password Recovery</h2></html>"),BorderLayout.CENTER);
		topPanel.add(SwingUtils.createBlueButtonRight(new WhyDoWeNeedAnAccountAction()),BorderLayout.EAST);
		topPanel.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));

		JPanel panel=new JPanel(new BorderLayout());
		panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		panel.add(topPanel,BorderLayout.NORTH);
		panel.add(center,BorderLayout.CENTER);
		panel.add(buttons,BorderLayout.SOUTH);
		
		return panel;
	}
	
	private JPanel buildLogin() {
		JPanel center=new JPanel(new GridBagLayout());
		center.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
		GridBagConstraints gbc=new GridBagConstraints();
		gbc.fill=WIDTH;
		gbc.weightx=100;
		gbc.gridy++;
		center.add(new JLabel("Email"),gbc);
		gbc.gridy++;
		center.add(loginEmail,gbc);
		gbc.gridy++;
		center.add(new JLabel("Password"),gbc);
		gbc.gridy++;
		center.add(loginPassword,gbc);
		gbc.gridy++;
		stayLoggenIn.setEnabled(true);
		center.add(stayLoggenIn,gbc);
		gbc.gridy++;
		gbc.weighty=100;
		center.add(new JLabel(""),gbc);
		gbc.gridy++;
		gbc.weighty=0;
		gbc.anchor=GridBagConstraints.WEST;
		
		loginEmail.setText(UserConfig.getPlayFabAccountEmail());
		
		center.add(SwingUtils.createBlueButton(new ShowRecoveryAction()),gbc);
		gbc.gridy++;
		center.add(SwingUtils.createBlueButton(new ShowRegisterAction()),gbc);

		JPanel buttons=new JPanel(new FlowLayout(FlowLayout.RIGHT));
		buttons.add(new JButton(new LoginAction()));
		
		JPanel topPanel=new JPanel(new BorderLayout());
		topPanel.add(new JLabel("<html><h2>Login</h2></html>"),BorderLayout.CENTER);
		topPanel.add(SwingUtils.createBlueButtonRight(new WhyDoWeNeedAnAccountAction()),BorderLayout.EAST);
		topPanel.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
		
		JPanel panel=new JPanel(new BorderLayout());
		panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		panel.add(topPanel,BorderLayout.NORTH);
		panel.add(center,BorderLayout.CENTER);
		panel.add(buttons,BorderLayout.SOUTH);
		
		panel.addComponentListener(new ComponentListener() {
			
			@Override
			public void componentShown(ComponentEvent e) {
				if (loginEmail.getText()!=null && loginEmail.getText().length()>0) {
					loginPassword.requestFocus();
				}
			}
			
			@Override
			public void componentResized(ComponentEvent e) {
				if (loginEmail.getText()!=null && loginEmail.getText().length()>0) {
					loginPassword.requestFocus();
				}
			}
			
			@Override
			public void componentMoved(ComponentEvent e) {
			}
			
			@Override
			public void componentHidden(ComponentEvent e) {
			}
		});
		
		return panel;
	}


	private JPanel buildMessage(String message) {
		JPanel center=new JPanel(new BorderLayout());
		center.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
		center.add(new JLabel(message),BorderLayout.NORTH);
		return center;
	}
	
	private JPanel buildRegister() {
		JPanel center=new JPanel(new GridBagLayout());
		center.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
		GridBagConstraints gbc=new GridBagConstraints();
		gbc.fill=WIDTH;
		gbc.weightx=100;
		gbc.gridy++;
		center.add(new JLabel("Email"),gbc);
		gbc.gridy++;
		center.add(registerEmail,gbc);
		gbc.gridy++;
		center.add(new JLabel("Password"),gbc);
		gbc.gridy++;
		center.add(registerPassword,gbc);
		gbc.gridy++;
		center.add(new JLabel("Password Repeat"),gbc);
		gbc.gridy++;
		center.add(registerPasswordRepeat,gbc);
		gbc.gridy++;
		center.add(new JLabel("Your handle (This can be changed later)"),gbc);
		gbc.gridy++;
		center.add(registerHandle,gbc);
		gbc.gridy++;
		gbc.weighty=100;
		center.add(new JLabel(""),gbc);
		gbc.gridy++;
		gbc.weighty=0;
		gbc.anchor=GridBagConstraints.WEST;
		
		center.add(SwingUtils.createBlueButton(new ShowLoginAction()),gbc);
		
		JPanel buttons=new JPanel(new FlowLayout(FlowLayout.RIGHT));
		buttons.add(new JButton(new RegisterAction()));
		
		JPanel topPanel=new JPanel(new BorderLayout());
		topPanel.add(new JLabel("<html><h2>Register</h2></html>"),BorderLayout.CENTER);
		topPanel.add(SwingUtils.createBlueButtonRight(new WhyDoWeNeedAnAccountAction()),BorderLayout.EAST);
		topPanel.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));

		JPanel panel=new JPanel(new BorderLayout());
		panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		panel.add(topPanel,BorderLayout.NORTH);
		panel.add(center,BorderLayout.CENTER);
		panel.add(buttons,BorderLayout.SOUTH);
		
		return panel;
	}
	
	private void build() {
		cardLayout=new CardLayout();
		mainPanel=new JPanel(cardLayout);
		mainPanel.add(buildLogin(),"Login");
		mainPanel.add(buildRegister(),"Register");
		mainPanel.add(buildRecovery(),"Recovery");
		mainPanel.add(buildMessage("<html><h1>Registering</h1><p>We are busy registering your account online, please be patient.</p></html>"),"Registering");
		mainPanel.add(buildMessage("<html><h1>Logging in</h1><p>We are busy logging you in to the server, please be patient.</p></html>"),"Logging-in");
		mainPanel.add(buildMessage("<html><h1>Requesting password recovery</h1><p>We are busy requesting a password recovery, please be patient.</p></html>"),"RecoveryInProcess");
		cardLayout.show(mainPanel, "Login");
		
		getContentPane().add(mainPanel);
	}
	
	private void startSocketRelay() {
		try {
			SocketRelayClient socketRelayClient = new SocketRelayClient();
			Notifications.init(socketRelayClient);
		} catch (Exception e) {
			logger.error(e.getMessage(),e);
			JOptionPane.showMessageDialog(null, "Unable to start please check logs.\n"+e.getMessage(),"Failed to start",JOptionPane.ERROR_MESSAGE);
			System.exit(-1);
		}
	}

	class ShowLoginAction extends AbstractAction {
		private static final long serialVersionUID = 1L;

		ShowLoginAction(){
			putValue(NAME, "Have an account, click here to login.");
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			cardLayout.show(mainPanel, "Login");
		}
		
	}

	class ShowRegisterAction extends AbstractAction {
		private static final long serialVersionUID = 1L;

		ShowRegisterAction(){
			putValue(NAME, "Don't have an account, click here to register a new account.");
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			cardLayout.show(mainPanel, "Register");
		}
		
	}
	
	class ShowRecoveryAction extends AbstractAction {
		private static final long serialVersionUID = 1L;

		ShowRecoveryAction(){
			putValue(NAME, "Forgotten your password, click here.");
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			cardLayout.show(mainPanel, "Recovery");
		}
		
	}

	class WhyDoWeNeedAnAccountAction extends AbstractAction {
		private static final long serialVersionUID = 1L;

		WhyDoWeNeedAnAccountAction(){
			putValue(NAME, "Why do I need an account?");
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			JOptionPane.showMessageDialog(null, "<html><h2>Why accounts</h2><p>We need accounts in order to manage user access to games.<br />In order for a host to know who is connecting the accounts are critical.<br /><br />Having an account helps us keep your connections private and<br />prevents other people from injecting into your games.</p></html>", "Why do I need an account?",JOptionPane.OK_OPTION);
		}
	}

	class LoginAction extends AbstractAction {
		private static final long serialVersionUID = 1L;

		LoginAction(){
			putValue(NAME, "Login");
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			cardLayout.show(mainPanel, "Logging-in");
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					LoginWithEmailAddressRequest request=new LoginWithEmailAddressRequest();
					request.Email=loginEmail.getText();
					request.Password=new String(loginPassword.getPassword());
					request.TitleId="CF491";
					request.InfoRequestParameters = new GetPlayerCombinedInfoRequestParams();
					request.InfoRequestParameters.GetPlayerProfile = true;
					
					PlayFabResult<LoginResult> result = PlayFabClientAPI.LoginWithEmailAddress(request);

					if (result.Result!=null) {
						UserConfig.setPlayFabAccountEmail(loginEmail.getText());
						UserConfig.setHandle(result.Result.InfoResultPayload.PlayerProfile.DisplayName);
//						UserConfig.setSession(result.Result.SessionTicket);
						
						setVisible(false);

						startSocketRelay();
					} else {
						JOptionPane.showMessageDialog(LoginFrame.this, "Unable to log you in.\n"+result.Error.errorMessage,"Login Failed",JOptionPane.WARNING_MESSAGE);
						cardLayout.show(mainPanel, "Login");
					}
				}
			});
		}
	}

	class RecoveryAction extends AbstractAction {
		private static final long serialVersionUID = 1L;

		RecoveryAction(){
			putValue(NAME, "Request Password Recovery");
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			cardLayout.show(mainPanel, "RecoveryInProcess");
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					SendAccountRecoveryEmailRequest request=new SendAccountRecoveryEmailRequest();
					request.Email=recoveryEmail.getText();
					request.EmailTemplateId="7F3B1883EB2672F0";
					request.TitleId="CF491";
					
					PlayFabResult<SendAccountRecoveryEmailResult> result = PlayFabClientAPI.SendAccountRecoveryEmail(request);
		
					if (result.Result!=null) {
						JOptionPane.showMessageDialog(LoginFrame.this, "You should recieve an email soon with the steps to reset your password.","Password recovery requested.",JOptionPane.WARNING_MESSAGE);
						cardLayout.show(mainPanel, "Login");
					} else {
						JOptionPane.showMessageDialog(LoginFrame.this, "Unable to request password recovery.\n"+result.Error.errorMessage,"Request Failed",JOptionPane.WARNING_MESSAGE);
						cardLayout.show(mainPanel, "Recovery");
					}
				}
			});
		}
		
	}

	class RegisterAction extends AbstractAction {
		private static final long serialVersionUID = 1L;

		RegisterAction(){
			putValue(NAME, "Register");
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			cardLayout.show(mainPanel, "Registering");
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					RegisterPlayFabUserRequest request=new RegisterPlayFabUserRequest();
					request.RequireBothUsernameAndEmail=false;
					request.Email=registerEmail.getText();
					request.Password=new String(registerPassword.getPassword());
					request.TitleId="CF491";
					request.DisplayName=registerHandle.getText();
					
					PlayFabResult<RegisterPlayFabUserResult> result = PlayFabClientAPI.RegisterPlayFabUser(request);
		
					if (result.Result!=null) {
						JOptionPane.showMessageDialog(LoginFrame.this, "Your account has been registered.","Account Registered.",JOptionPane.WARNING_MESSAGE);
						cardLayout.show(mainPanel, "Login");
					} else {
						JOptionPane.showMessageDialog(LoginFrame.this, "Unable to request password recovery.\n"+result.Error.errorMessage,"Request Failed",JOptionPane.WARNING_MESSAGE);
						cardLayout.show(mainPanel, "Register");
					}
				}
			});
		}
	}
}
