package qtnotes.actions;/*
 * Copyright (c) 2021 Mohit Saini, Under MIT License. Use is subject to license terms.
 * 
 */

import qtnotes.gui.GUIHandler;
import qtnotes.gui.helper.FontDialog;
import qtnotes.init.InitialValues;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.JTextArea;

public class FormatMenuActions {
    
    public static class WordWrapFormatAction extends AbstractAction {
        public WordWrapFormatAction() {
            super();
            putValue(AbstractAction.NAME, "Word Wrap");
            putValue(MNEMONIC_KEY, KeyEvent.VK_W);
        }
        @Override
        public void actionPerformed(ActionEvent e) {
            JTextArea area = GUIHandler.getEditorTextArea();
            area.setLineWrap(!InitialValues.getWrapTheLine());
            InitialValues.setWrapTheLine(!InitialValues.getWrapTheLine());
        }
    }

    public static class shouldReplaceQuotes extends AbstractAction {
        public shouldReplaceQuotes() {
            super();
            putValue(AbstractAction.NAME, "Replace \" and ' with “” and ‘’");
            putValue(MNEMONIC_KEY, KeyEvent.VK_R);
        }
        @Override
        public void actionPerformed(ActionEvent e) {
            GUIHandler.setReplaceQuotes();
            InitialValues.setReplaceQuote(!InitialValues.getReplaceQuote());
        }
    }

    public static class shouldDoExceptions extends AbstractAction {
        public shouldDoExceptions() {
            super();
            putValue(AbstractAction.NAME, "Automatically change couldn't and wouldn't");
            putValue(MNEMONIC_KEY, KeyEvent.VK_X);
        }
        @Override
        public void actionPerformed(ActionEvent e) {
            InitialValues.setException(!InitialValues.getException());
        }
    }

    public static class FontChangeFormatAction extends AbstractAction {
        public FontChangeFormatAction() {
            super();
            putValue(AbstractAction.NAME, "Font...");
            putValue(MNEMONIC_KEY, KeyEvent.VK_F);
        }
        @Override
        public void actionPerformed(ActionEvent e) {
            FontDialog font = new FontDialog(GUIHandler.getFrame(), "Font", true);
            font.setVisible(true);
        }
    }
}
