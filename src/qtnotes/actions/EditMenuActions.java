package qtnotes.actions;/*
 * Copyright (c) 2021 Mohit Saini, Under MIT License. Use is subject to license terms.
 *
 */


import qtnotes.gui.GUIHandler;
import qtnotes.gui.helper.DialogType;
import qtnotes.gui.helper.FullEditForm;
import qtnotes.gui.helper.SuggestionsDisplayForm;
import qtnotes.init.InitialValues;
import qtnotes.spellcheck.Spellcheck;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import javax.swing.AbstractAction;
import javax.swing.KeyStroke;

import static java.awt.event.InputEvent.CTRL_DOWN_MASK;

public class EditMenuActions {

    public static class OpenQuickTypeEditAction extends AbstractAction {
        FullEditForm f;
        public OpenQuickTypeEditAction() {
            super();
            putValue(AbstractAction.NAME, "Open Quicktype Edit");
            putValue(MNEMONIC_KEY, KeyEvent.VK_E);
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_E, CTRL_DOWN_MASK));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (f != null){
                f.myFrame.dispose();
            }
            f = new FullEditForm(GUIHandler.getWordList());
        }
    }

    public static class SuggestAction extends AbstractAction {
        public SuggestAction() {
            super();
            putValue(AbstractAction.NAME, "Suggest");
            putValue(MNEMONIC_KEY, KeyEvent.VK_L);
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_L, CTRL_DOWN_MASK));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            //TODO check the word that your cursor is on, if cursor is not on something either loop or choose the first
            var word = GUIHandler.getNextMistake();
            var list = Spellcheck.optimizedSearch(word);
            new SuggestionsDisplayForm(GUIHandler.getFrame(), InitialValues.getEditorFont(), list, GUIHandler.getShouldLoop(word), word);
        }
    }
}
