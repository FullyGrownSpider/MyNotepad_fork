package qtnotes.gui.helper;

import qtnotes.gui.GUIHandler;

import javax.swing.text.BadLocationException;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

public class QTMouse implements MouseListener {
    @Override
    public void mouseClicked(MouseEvent e) {
        if (e.isControlDown()) {
            Clipboard cb = Toolkit.getDefaultToolkit().getSystemClipboard();
            var export = new StringSelection(GUIHandler.getFullQTExport());
            cb.setContents(export, export);
            try {
                Thread.sleep(10);
            } catch (Exception ignored) {
            }
        }
        else
            try {
                GUIHandler.setLocationOfCursor();
            } catch (BadLocationException ignored) {
            }
    }

    @Override
    public void mousePressed(MouseEvent e) {

    }

    @Override
    public void mouseReleased(MouseEvent e) {
        Clipboard cb = Toolkit.getDefaultToolkit().getSystemClipboard();
        var export = new StringSelection(GUIHandler.getSelectedQTExport());
        cb.setContents(export, export);
        try {
            Thread.sleep(10);
        } catch (Exception ignored) {
        }
        GUIHandler.getEditorTextArea().grabFocus();
    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }
}
