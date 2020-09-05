package com.socketrelay.client;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JLabel;

public class SwingUtils {
	
	public static JButton createBlueButton(Action action) {
		JButton button=new JButton(action);
		button.setContentAreaFilled(false);
		button.setForeground(new Color(0x3287FF));
		button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		return button;
	}
	
	public static JLabel createBlueButtonRight(Action action) {
		JLabel label=new JLabel((String)action.getValue(Action.NAME));
		label.setForeground(new Color(0x3287FF));
		label.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		label.addMouseListener(new MouseListener() {
			
			@Override
			public void mouseReleased(MouseEvent e) {
			}
			
			@Override
			public void mousePressed(MouseEvent e) {
			}
			
			@Override
			public void mouseExited(MouseEvent e) {
				
			}
			
			@Override
			public void mouseEntered(MouseEvent e) {
			}
			
			@Override
			public void mouseClicked(MouseEvent e) {
				action.actionPerformed(new ActionEvent(label, e.getID(), "Clicked"));
			}
		});
		return label;
	}

}
