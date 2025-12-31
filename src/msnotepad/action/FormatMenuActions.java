/*
 * Copyright (c) 2021 Mohit Saini, Under MIT License. Use is subject to license terms.
 * 
 */

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

            if (InitialValues.getWrapTheLine()) {
                area.setLineWrap(false);
                InitialValues.setWrapTheLine(false);
            } else {
                area.setLineWrap(true);
                InitialValues.setWrapTheLine(true);
            }
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
