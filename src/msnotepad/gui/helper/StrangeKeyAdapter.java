import javax.swing.text.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class StrangeKeyAdapter extends KeyAdapter {

    private boolean needFullReset = false;

    @Override
    public void keyReleased(KeyEvent e) {
        if (needFullReset){
            GUIHandler.fullCompare();
            return;
        }
        if (e.getKeyCode() == KeyEvent.VK_ENTER){
            GUIHandler.enterCompare();
            return;
        }
        if (e.getKeyCode() == KeyEvent.VK_DELETE){
            GUIHandler.enterCompare();
            return;
        }
        GUIHandler.doCompare();
    }

    @Override
    public void keyPressed(KeyEvent e) {
        super.keyPressed(e);
        GUIHandler.count = 2;
        int charCode = e.getKeyCode();
        if (GUIHandler.ignoreKeys(charCode)) return;
        if (e.isAltDown()) return;
        if (charCode == KeyEvent.VK_ENTER){
            if (e.isShiftDown()) {
                removeModifiers(e);
            }
        }

        if (DoMovement(e, charCode)) return;

        if (doDelete(e, charCode)) return;
        if (doBackspace(e, charCode)) return;

        if (doCuts(e, charCode)) return;

        var selected = GUIHandler.getEditorTextArea().getSelectedText();

        if (charCode == KeyEvent.VK_TAB && (e.isShiftDown() || selected != null)) {
            GUIHandler.removeTab(e.isShiftDown());
            needFullReset = true;
            e.consume();
            return;
        }
        if (selected != null) {
            GUIHandler.addRemoveToUndo(selected);
            GUIHandler.getEditorTextArea().replaceRange("", GUIHandler.getEditorTextArea().getSelectionStart(), GUIHandler.getEditorTextArea().getSelectionEnd());
            if (charCode == KeyEvent.VK_BACK_SPACE || charCode == KeyEvent.VK_DELETE) {
                e.consume();
                return;
            }
            GUIHandler.addToUndo(e.getKeyChar());
            return;
        }
        GUIHandler.addToUndo(e.getKeyChar());
    }

    private boolean doCuts(KeyEvent e, int charCode) {
        if (!e.isControlDown()) return false;
        if (charCode == KeyEvent.VK_V) {
            GUIHandler.pasteIntoEdit();
            needFullReset = true;
            return true;
        }
        else if (charCode == KeyEvent.VK_X) {
            GUIHandler.CopyIntoEdit(true);
            needFullReset = true;
            return true;
        }
        else if (charCode == KeyEvent.VK_C) {
            GUIHandler.CopyIntoEdit(false);
            needFullReset = true;
            return true;
        } else return charCode == KeyEvent.VK_Z || charCode == KeyEvent.VK_Y;
    }

    private static boolean DoMovement(KeyEvent e, int charCode) {
        //arrow up and arrow down shift do  not work
        if (!e.isShiftDown() && !e.isControlDown()) {
            if (GUIHandler.getEditorTextArea().getSelectedText() != null){
                if (KeyEvent.VK_LEFT == charCode) {
                    var location = GUIHandler.getEditorTextArea().getSelectionStart();
                    GUIHandler.getEditorTextArea().setSelectionEnd(location);
                    GUIHandler.getEditorTextArea().setSelectionStart(location);
                    return true;
                }
            }
            return charCode >= 35 && charCode <= 40;
        }
        int next = getNext(charCode);
        if (next != -1) {
            if (e.isShiftDown()) {
                int from = GUIHandler.getEditorTextArea().getSelectionStart();
                if (from < next) {
                    GUIHandler.getEditorTextArea().setSelectionEnd(next);
                    GUIHandler.getEditorTextArea().setSelectionStart(from);
                } else {
                    GUIHandler.getEditorTextArea().setSelectionEnd(GUIHandler.getEditorTextArea().getSelectionEnd());
                    GUIHandler.getEditorTextArea().setSelectionStart(next);
                }

            } else {
                GUIHandler.getEditorTextArea().setCaretPosition(next);
            }
            e.consume();
            return true;
        }

        if (charCode == KeyEvent.VK_HOME){
            if (e.isControlDown()){
                if (e.isShiftDown()) {
                    GUIHandler.getEditorTextArea().setSelectionStart(0);
                    GUIHandler.getEditorTextArea().setSelectionEnd(GUIHandler.getEditorTextArea().getSelectionEnd());
                }
                else {
                    GUIHandler.getEditorTextArea().setCaretPosition(GUIHandler.getEditorTextArea().getText().length() - 1);
                }
                e.consume();
                return true;
            }
            int line, lineStart;
            try {
                line = GUIHandler.getEditorTextArea().getLineOfOffset(GUIHandler.getEditorTextArea().getSelectionStart());
                lineStart = GUIHandler.getEditorTextArea().getLineStartOffset(line);
            } catch (BadLocationException ex) {
                throw new RuntimeException(ex);
            }
            if (e.isShiftDown()) {
                GUIHandler.getEditorTextArea().setSelectionStart(lineStart);
                GUIHandler.getEditorTextArea().setSelectionEnd(GUIHandler.getEditorTextArea().getSelectionEnd());
            }
            else {
                GUIHandler.getEditorTextArea().setCaretPosition(lineStart);
            }
            e.consume();
            return true;
        } else if (charCode == KeyEvent.VK_END)
        {
            if (e.isControlDown()){
                if (e.isShiftDown()) {
                    GUIHandler.getEditorTextArea().setSelectionStart(GUIHandler.getEditorTextArea().getSelectionStart());
                    GUIHandler.getEditorTextArea().setSelectionEnd(GUIHandler.getEditorTextArea().getText().length() - 1);
                }
                else {
                    GUIHandler.getEditorTextArea().setCaretPosition(GUIHandler.getEditorTextArea().getText().length() - 1);
                }
                e.consume();
                return true;
            }
            int line, lineEnd;
            try {
                line = GUIHandler.getEditorTextArea().getLineOfOffset(GUIHandler.getEditorTextArea().getSelectionStart());
                lineEnd = GUIHandler.getEditorTextArea().getLineEndOffset(line);
            } catch (BadLocationException ex) {
                throw new RuntimeException(ex);
            }
            if (e.isShiftDown()) {
                GUIHandler.getEditorTextArea().setSelectionStart(GUIHandler.getEditorTextArea().getSelectionStart());
                GUIHandler.getEditorTextArea().setSelectionEnd(lineEnd);
            }
            else {
                GUIHandler.getEditorTextArea().setCaretPosition(lineEnd);
            }
            e.consume();
            return true;
        }
        return false;
    }

    private boolean doBackspace(KeyEvent e, int charCode) {
        if (charCode == KeyEvent.VK_BACK_SPACE){
            if (GUIHandler.getEditorTextArea().getSelectedText() != null){
                GUIHandler.addRemoveToUndo(GUIHandler.getEditorTextArea().getSelectedText());
                GUIHandler.getEditorTextArea().replaceSelection("");
                e.consume();
                needFullReset = true;
                return true;
            }
            if (!e.isControlDown()){
                removeModifiers(e);
                int location = GUIHandler.getEditorTextArea().getCaretPosition();
                if (location != 0) {
                    try {
                        GUIHandler.addRemoveToUndo(GUIHandler.getEditorTextArea().getText(location - 1, 1));
                    } catch (BadLocationException ex) {
                        throw new RuntimeException(ex);
                    }
                }
                needFullReset = true;
                return true;
            }
            int to = GUIHandler.getLastSpaceIndex();
            int from = GUIHandler.getEditorTextArea().getCaretPosition();
            GUIHandler.getEditorTextArea().setSelectionStart(to);
            GUIHandler.getEditorTextArea().setSelectionEnd(from);
            if (to != from) {
                GUIHandler.addRemoveToUndo(GUIHandler.getEditorTextArea().getSelectedText());
                GUIHandler.getEditorTextArea().replaceSelection("");
            }
            e.consume();
            needFullReset = true;
            return true;
        }
        return false;
    }

    private boolean doDelete(KeyEvent e, int charCode) {
        if (charCode == KeyEvent.VK_DELETE){
            if (e.isControlDown()) {
                if (GUIHandler.getEditorTextArea().getSelectedText() != null) {
                    GUIHandler.addRemoveToUndo(GUIHandler.getEditorTextArea().getSelectedText());
                    e.consume();
                    needFullReset = true;
                    return true;
                }
                int to = GUIHandler.getNextSpaceIndex();
                int from = GUIHandler.getEditorTextArea().getCaretPosition();
                if (to - from == 1) {
                    GUIHandler.getEditorTextArea().setCaretPosition(to);
                    to = GUIHandler.getNextSpaceIndex();
                }
                GUIHandler.getEditorTextArea().setSelectionStart(from);
                GUIHandler.getEditorTextArea().setSelectionEnd(to);
                if (to != from) {
                    GUIHandler.addRemoveToUndo(GUIHandler.getEditorTextArea().getSelectedText());
                    GUIHandler.getEditorTextArea().replaceSelection("");
                }
                e.consume();
                needFullReset = true;
                return true;
            }
            if (e.isShiftDown()) {
                GUIHandler.deleteLine();
                e.consume();
                needFullReset = true;
                return true;
            }
            var selected = GUIHandler.getEditorTextArea().getSelectedText();
            if (selected != null){
                GUIHandler.addRemoveToUndo(selected);
                GUIHandler.getEditorTextArea().replaceSelection("");
                e.consume();
                needFullReset = true;
                return true;
            }
            int location = GUIHandler.getEditorTextArea().getCaretPosition();
            try {
                if (location < GUIHandler.getEditorTextArea().getText().length()) {
                    GUIHandler.addRemoveToUndo(GUIHandler.getEditorTextArea().getText(location, 1));
                }
            } catch (BadLocationException ex) {
                throw new RuntimeException(ex);
            }
            needFullReset = true;
            return true;
        }
        return false;
    }

    private static void removeModifiers(KeyEvent e) {
        e.setModifiers(0);
    }

    private static int getNext(int charCode) {
        int next = -1;
        if (charCode == KeyEvent.VK_LEFT) {
            next = (GUIHandler.getLastSpaceIndex());
        }
        else if (charCode == KeyEvent.VK_RIGHT) {
            next = (GUIHandler.getNextSpaceIndex());
        }
        else if (charCode == KeyEvent.VK_UP){
            next = (GUIHandler.getPreviousEnterIndex());
        }
        else if (charCode == KeyEvent.VK_DOWN){
            next = ((GUIHandler.getNextEnterIndex()));
        }
        return next;
    }
}
