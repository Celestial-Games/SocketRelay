package com.socketrelay.client.swing;

import java.io.OutputStream;

import javax.swing.JTextArea;
import javax.swing.text.BadLocationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TextAreaOutputStream extends OutputStream {
	private static final Logger logger=LoggerFactory.getLogger(TextAreaOutputStream.class);
    private JTextArea textArea;
    
    private boolean running=true;
    
    private final static int SCROLL_BUFFER_SIZE=1000;
    
    public TextAreaOutputStream(JTextArea textArea) {
        this.textArea = textArea;
    }
     
    @Override
    public void write(int b) {
    	if (running) {
	        textArea.append(String.valueOf((char)b));
	        
	        int extraLines = textArea.getLineCount() - SCROLL_BUFFER_SIZE;
	        if(extraLines > 0) {
	            try {
	                int pos = textArea.getLineEndOffset(extraLines - 1);
	                textArea.replaceRange("",0,pos);
	            } catch (BadLocationException e) {
	            	running=false;
	            	logger.error(e.getMessage(),e);
	            }
	        }
	        textArea.setCaretPosition(textArea.getDocument().getLength());
    	}        
    }
}
