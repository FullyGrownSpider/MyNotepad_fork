/*
 * Copyright (c) 2021 Mohit Saini, Under MIT License. Use is subject to license terms.
 * 
 */


import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import javax.swing.AbstractAction;
import javax.swing.KeyStroke;
import javax.swing.JTextArea;

import static java.awt.event.InputEvent.CTRL_DOWN_MASK;
import static java.awt.event.InputEvent.SHIFT_DOWN_MASK;

public class EditMenuActions {
    
    public static class UndoEditAction extends AbstractAction {
        public UndoEditAction() {
            super();
            putValue(AbstractAction.NAME, "Undo");
            putValue(MNEMONIC_KEY, KeyEvent.VK_U);
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_Z, CTRL_DOWN_MASK));
        }
        @Override
        public void actionPerformed(ActionEvent e) {
            var undoAction = GUIHandler.getUndoAction();
            if (undoAction == null) return;
            GUIHandler.decreaseUndo();
            if (undoAction.deleted){
                GUIHandler.getEditorTextArea().insert(undoAction.text.toString(), undoAction.location);
            } else {
                GUIHandler.getEditorTextArea().replaceRange("", undoAction.location, undoAction.location + undoAction.text.length());
            }
        }
    }
    public static class RedoEditAction extends AbstractAction {
        public RedoEditAction() {
            super();
            putValue(AbstractAction.NAME, "Redo");
            putValue(MNEMONIC_KEY, KeyEvent.VK_U);
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_Y, CTRL_DOWN_MASK));
        }
        @Override
        public void actionPerformed(ActionEvent e) {
            GUIHandler.increaseUndo();
            var undoAction = GUIHandler.getRedoAction();
            if (undoAction == null) {
                GUIHandler.decreaseUndo();
                return;
            }
            if (undoAction.deleted){
                GUIHandler.getEditorTextArea().replaceRange("", undoAction.location, undoAction.location + undoAction.text.length());
            } else {
                GUIHandler.getEditorTextArea().insert(undoAction.text.toString(), undoAction.location);
            }
        }
    }

    public static class FindEditAction extends AbstractAction {
        public FindEditAction() {
            super();
            putValue(AbstractAction.NAME, "Find");
            putValue(MNEMONIC_KEY, KeyEvent.VK_F);
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_F, CTRL_DOWN_MASK));
        }
        @Override
        public void actionPerformed(ActionEvent e) {
            new FindAndReplaceDialog(GUIHandler.getFrame(), DialogType.FIND_ONLY, true);
        }
    }

    public static class ReplaceEditAction extends AbstractAction {
        public ReplaceEditAction() {
            super();
            putValue(AbstractAction.NAME, "Replace");
            putValue(MNEMONIC_KEY, KeyEvent.VK_R);
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_F, CTRL_DOWN_MASK | SHIFT_DOWN_MASK));
        }
        @Override
        public void actionPerformed(ActionEvent e) {
            new FindAndReplaceDialog(GUIHandler.getFrame(), DialogType.FIND_AND_REPLACE, true);
        }
    }

    public static class SelectAllEditAction extends AbstractAction {
        public SelectAllEditAction() {
            super();
            putValue(AbstractAction.NAME, "Select All");
            putValue(MNEMONIC_KEY, KeyEvent.VK_A);
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_A, CTRL_DOWN_MASK));
        }
        @Override
        public void actionPerformed(ActionEvent e) {
            JTextArea area = GUIHandler.getEditorTextArea();
            area.selectAll();
        }
    }
}
