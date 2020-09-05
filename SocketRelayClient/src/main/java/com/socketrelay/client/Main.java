package com.socketrelay.client;

import java.awt.Color;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.logging.LogManager;

import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {
	private static final Logger logger=LoggerFactory.getLogger(Main.class);
	
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
			new LoginFrame(); 
			
			logger.info("Started Socket Relay Client");
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			JOptionPane.showMessageDialog(null, "Unable to start system.\n\n" + e.getMessage(), "Erorr Starting!",
					JOptionPane.ERROR_MESSAGE);
			System.exit(-1);
		}
	}

}
