package com.socketrelay.client;

import java.awt.Image;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Images {
	private static final Logger logger=LoggerFactory.getLogger(Images.class);

	private static List<Image> icons;
	
	public synchronized static final List<Image> getIcons() {
		if (icons==null) {
			try {
				loadIcons();
			} catch (IOException e) {
				logger.error(e.getMessage(),e);
			}
		}
		
		return icons;
	}
	
	private static void loadIcons() throws IOException {
		icons = new ArrayList<>();
		icons.add(ImageIO.read(ClassLoader.getSystemResourceAsStream("images/icon16.png")));
		icons.add(ImageIO.read(ClassLoader.getSystemResourceAsStream("images/icon24.png")));
		icons.add(ImageIO.read(ClassLoader.getSystemResourceAsStream("images/icon32.png")));
		icons.add(ImageIO.read(ClassLoader.getSystemResourceAsStream("images/icon48.png")));
		icons.add(ImageIO.read(ClassLoader.getSystemResourceAsStream("images/icon64.png")));
		icons.add(ImageIO.read(ClassLoader.getSystemResourceAsStream("images/icon128.png")));
		icons.add(ImageIO.read(ClassLoader.getSystemResourceAsStream("images/icon256.png")));
	}

}
