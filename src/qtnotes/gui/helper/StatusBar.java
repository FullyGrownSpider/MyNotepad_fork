package qtnotes.gui.helper;/*
 * Copyright (c) 2021 Mohit Saini, Under MIT License. Use is subject to license terms.
 * 
 */


import qtnotes.gui.GUIHandler;
import qtnotes.init.InitialValues;

import java.awt.Font;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.BorderLayout;
import java.awt.FlowLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;


/**
 * qtnotes.gui.helper.StatusBar class make basic statusBar for the MSNotepad, with the
 * help of the JPanel.
 */
public class StatusBar extends JPanel{
    private static StatusBarLabel hint;

    /**
     * qtnotes.gui.helper.StatusBar constructor help to do the initial work.
     */
	public StatusBar() {
        super();
        setOpaque(false);
        setPreferredSize(new Dimension(GUIHandler.getFrame().getWidth(), 23));
        initialiseStatusBar();
        setVisible(InitialValues.getShowStatusBar());
    }

    public void setHintText(String hintText) {
        hint.setText(hintText);
    }

    /**
     * initialiseStatusBar method help to do the initial work and setup
     * components.
     */
    private void initialiseStatusBar() {
        JPanel labelHolder = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        labelHolder.setOpaque(false);
        labelHolder.setBackground(new Color(0, 0, 0, 0));
		
        hint = new StatusBarLabel("", 340);

        hint.setForeground(Color.red);

        labelHolder.add(hint);

        setLayout(new BorderLayout());
		add(hint);
	}

    /**
     * StatusBarLabel inner class help to make label ready for to be
     * added on the statusBar panel.
     */
    private static class StatusBarLabel extends JLabel {
        Font font = new Font("", Font.PLAIN , 14);
        
        private StatusBarLabel(String name,int width) {
            super(name);
            setFont(font);
            setPreferredSize(new Dimension(width, 23));
            setAlignmentX(LEFT_ALIGNMENT);
        }
        @Override
        public void setText(String text){
            super.setText(" " + text);
        }
    }
}
