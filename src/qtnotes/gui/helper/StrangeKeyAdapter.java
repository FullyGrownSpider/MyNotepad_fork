package qtnotes.gui.helper;

import qtnotes.actions.UndoActionType;
import qtnotes.actions.Undoer;
import qtnotes.gui.GUIHandler;

import javax.swing.text.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

enum ACTION {
    NOTHING, FULL_QT, LINE_QT, POST_TEXT
}

public class StrangeKeyAdapter extends KeyAdapter {

    private static ACTION actionToDo = ACTION.NOTHING;
    public static final Pattern spaceOrEnter = Pattern.compile("[ \n]");
    public Undoer undoer = new Undoer();

    @Override
    public void keyReleased(KeyEvent e) {
        switch (actionToDo) {
            case FULL_QT -> GUIHandler.fullTextQT();
            case LINE_QT -> GUIHandler.fullLineQT();
            case POST_TEXT -> GUIHandler.postQTClean();
        }
        actionToDo = ACTION.NOTHING;
    }

    @Override
    public void keyPressed(KeyEvent e) {
        super.keyPressed(e);
        GUIHandler.saveCount = 2;
        int charCode = e.getKeyCode();
        if (ignoreKeys(charCode)) {
            return;
        }
        if (charCode == KeyEvent.VK_ENTER) {
            if (e.isShiftDown()) {
                removeModifiers(e);
            }
            undoer.addingTextUndo(GUIHandler.getCursorLocation(), "\n");
            actionToDo = ACTION.FULL_QT;
            return;
        }

        if (DoMovement(e, charCode)) {
            actionToDo = ACTION.POST_TEXT;
            return;
        }

        if (doDelete(e, charCode) || doBackspace(e, charCode)) {
            return;
        }

        if (doCuts(e, charCode)) return;

        if (undoRedo(e, charCode)) return;

        if (e.isControlDown() || e.isAltDown()) return;

        var selected = GUIHandler.getEditorTextArea().getSelectedText();

        if (charCode == KeyEvent.VK_TAB && (e.isShiftDown() || selected != null)) {
            var locationStart = GUIHandler.getCursorLocation();
            var locationEnd = GUIHandler.getEditorTextArea().getSelectionEnd();
            removeTab(e.isShiftDown());
            e.consume();
            if (e.isShiftDown()) {
                GUIHandler.getEditorTextArea().setSelectionStart(locationStart);
                GUIHandler.getEditorTextArea().setSelectionEnd(locationEnd);
            }
            return;
        }
        if (selected != null) {
            undoer.removeTextUndo(GUIHandler.getCursorLocation(), selected);
            undoer.addingTextUndo(GUIHandler.getCursorLocation(), "" + e.getKeyChar());
            actionToDo = ACTION.FULL_QT;
            return;
        }
        actionToDo = ACTION.LINE_QT;
        undoer.addingTextUndo(GUIHandler.getCursorLocation(), "" + e.getKeyChar());
    }

    private boolean undoRedo(KeyEvent e, int charCode) {
        if (!e.isControlDown()) return false;
        if (charCode == KeyEvent.VK_Y) {
            var thing = undoer.redoAnAction();
            if (thing == null) return false;
            if (thing.isTab()) {
                var ints = Undoer.getTabLines(thing);
                GUIHandler.getEditorTextArea().select(ints[0], ints[1]);
                removeTab(thing.actionType == UndoActionType.DELETE);
                actionToDo = ACTION.FULL_QT;
            } else {
                var text = thing.text.toString();
                if (thing.actionType != UndoActionType.ADD) {
                    GUIHandler.getEditorTextArea().replaceRange("", thing.location - 1, thing.location + text.length() - 1);
                } else {
                    GUIHandler.getEditorTextArea().insert(text, thing.location);
                }
                correctEdit(text);
            }
            e.consume();
            return true;
        }
        if (charCode == KeyEvent.VK_Z) {
            var thing = undoer.undoAnAction();
            if (thing == null) return false;
            if (thing.isTab()) {
                var ints = Undoer.getTabLines(thing);
                GUIHandler.getEditorTextArea().select(ints[0], ints[1]);
                removeTab(thing.actionType == UndoActionType.ADD);
                actionToDo = ACTION.FULL_QT;
            } else {
                var text = thing.text.toString();
                if (thing.actionType == UndoActionType.ADD) {
                    GUIHandler.getEditorTextArea().replaceRange("", thing.location, thing.location + text.length());
                } else {
                    GUIHandler.getEditorTextArea().insert(text, thing.location - 1);
                }
                correctEdit(text);
            }
            e.consume();
            return true;
        }
        return false;
    }

    private boolean doCuts(KeyEvent e, int charCode) {
        if (!e.isControlDown()) return false;
        if (charCode == KeyEvent.VK_V) {
            pasteIntoEdit();
            actionToDo = ACTION.FULL_QT;
            return true;
        } else if (charCode == KeyEvent.VK_X) {
            CopyIntoEdit(true);
            actionToDo = ACTION.FULL_QT;
            return true;
        } else if (charCode == KeyEvent.VK_C) {
            CopyIntoEdit(false);
            return true;
        }
        return false;
    }

    void pasteIntoEdit() {
        var selected = GUIHandler.getEditorTextArea().getSelectedText();
        if (selected != null) {
            undoer.removeTextUndo(GUIHandler.getCursorLocation(), selected);
            GUIHandler.getEditorTextArea().replaceRange("", GUIHandler.getEditorTextArea().getSelectionStart(), GUIHandler.getEditorTextArea().getSelectionEnd());
        }
        Clipboard cb = Toolkit.getDefaultToolkit().getSystemClipboard();
        try {
            String text;
            var trans = cb.getContents(null);
            if (trans.isDataFlavorSupported(DataFlavor.stringFlavor)) {
                text = (String) trans.getTransferData(DataFlavor.stringFlavor);
                undoer.addingTextUndo(GUIHandler.getCursorLocation(), text);
            }

        } catch (IOException | UnsupportedFlavorException ex) {
            throw new RuntimeException(ex);
        }
    }

    void CopyIntoEdit(boolean cut) {
        var selected = GUIHandler.getEditorTextArea().getSelectedText();
        if (selected == null) {
            int lineStart, lineEnd;
            try {
                int line = GUIHandler.getCurrentLine();
                lineStart = GUIHandler.getEditorTextArea().getLineStartOffset(line);
                lineEnd = GUIHandler.getEditorTextArea().getLineEndOffset(line);
            } catch (BadLocationException e) {
                throw new RuntimeException(e);
            }
            GUIHandler.getEditorTextArea().select(lineStart, lineEnd);
            selected = GUIHandler.getEditorTextArea().getSelectedText();
            if (selected == null || selected.isEmpty()) return;
        }
        if (cut) {
            undoer.removeTextUndo(GUIHandler.getCursorLocation(), selected);
            GUIHandler.getEditorTextArea().replaceRange("", GUIHandler.getEditorTextArea().getSelectionStart(), GUIHandler.getEditorTextArea().getSelectionEnd());
        }
        Clipboard cb = Toolkit.getDefaultToolkit().getSystemClipboard();

        StringSelection data = new StringSelection(selected);
        cb.setContents(data, null);
    }

    private static boolean DoMovement(KeyEvent e, int charCode) {
        if (KeyEvent.VK_PAGE_DOWN == charCode || KeyEvent.VK_PAGE_UP == charCode) return true;
        if (charCode == KeyEvent.VK_HOME) {
            homeOrEnd(e, true);
            return true;
        } else if (charCode == KeyEvent.VK_END) {
            homeOrEnd(e, false);
            return true;
        }
        //up down left right
        //ctrl
        int next = getNextCtrlMovement(charCode);
        if (next == -1) return false;
        if (e.isAltDown()){
            int from = GUIHandler.getEditorTextArea().getSelectionStart();
            if (charCode == KeyEvent.VK_RIGHT) {
                from = (rightClosestBorder(from));
                next = GUIHandler.getEditorTextArea().getSelectionEnd();
            } else {
                next = (leftClosestBorder(GUIHandler.getEditorTextArea().getSelectionEnd()));
            }
            if (from != next) {
                setSelection(from, next);
            }
            e.consume();
            return true;
        }
        if (!e.isControlDown()) {
            if (GUIHandler.getEditorTextArea().getSelectedText() != null) {
                if (KeyEvent.VK_LEFT == charCode) {
                    var location = GUIHandler.getEditorTextArea().getSelectionStart();
                    GUIHandler.getEditorTextArea().setSelectionEnd(location);
                    GUIHandler.getEditorTextArea().setSelectionStart(location);
                    return true;
                }
            }
            return true;
        }
        if (KeyEvent.VK_LEFT == charCode || KeyEvent.VK_RIGHT == charCode) {
            if (e.isShiftDown()) {
                int from = GUIHandler.getEditorTextArea().getSelectionStart();
                setSelection(from, next);
            } else {
                GUIHandler.getEditorTextArea().setCaretPosition(next);
            }
        } else {
            GUIHandler.getEditorTextArea().setCaretPosition(next);
        }
        e.consume();
        return true;
    }

    private static void setSelection(int from, int next) {
        if (from < next) {
            GUIHandler.getEditorTextArea().setSelectionEnd(next);
            GUIHandler.getEditorTextArea().setSelectionStart(from);
        } else {
            GUIHandler.getEditorTextArea().setSelectionEnd(GUIHandler.getEditorTextArea().getSelectionEnd());
            GUIHandler.getEditorTextArea().setSelectionStart(next);
        }
    }

    private static void homeOrEnd(KeyEvent e, boolean home) {
        if (e.isControlDown()) {
            if (e.isShiftDown()) {
                GUIHandler.getEditorTextArea().setSelectionStart(home? 0: GUIHandler.getEditorTextArea().getSelectionStart());
                GUIHandler.getEditorTextArea().setSelectionEnd(home ? GUIHandler.getEditorTextArea().getSelectionEnd() : GUIHandler.getEditorTextLength() - 1);
            } else {
                GUIHandler.getEditorTextArea().setCaretPosition(home ? 0: GUIHandler.getEditorTextLength() - 1);
            }
        } else {
            int line, linePoint;
            try {
                line = GUIHandler.getEditorTextArea().getLineOfOffset(GUIHandler.getEditorTextArea().getSelectionStart());
                if (home)
                linePoint = GUIHandler.getEditorTextArea().getLineStartOffset(line);
                else {
                    var totalLength = GUIHandler.getEditorTextLength();
                    var mount = GUIHandler.getEditorTextArea().getLineEndOffset(line);
                    if (mount != totalLength)
                        linePoint = mount-1;
                    else {
                        linePoint = totalLength;
                    }
                }
            } catch (BadLocationException ex) {
                throw new RuntimeException(ex);
            }
            if (e.isShiftDown()) {
                GUIHandler.getEditorTextArea().setSelectionStart(home ? linePoint : GUIHandler.getEditorTextArea().getSelectionStart());
                GUIHandler.getEditorTextArea().setSelectionEnd(home ? GUIHandler.getEditorTextArea().getSelectionEnd() : linePoint);
            } else {
                GUIHandler.getEditorTextArea().setCaretPosition(linePoint);
            }
        }
        e.consume();
    }

    private boolean doBackspace(KeyEvent e, int charCode) {
        if (charCode == KeyEvent.VK_BACK_SPACE) {
            var selectedText = GUIHandler.getEditorTextArea().getSelectedText();
            if (selectedText != null) {
                undoer.removeTextUndo(GUIHandler.getCursorLocation(), selectedText);
                GUIHandler.getEditorTextArea().replaceSelection("");
                correctEdit(selectedText);
                e.consume();
                return true;
            }
            if (!e.isControlDown()) {
                try {
                    var character = GUIHandler.getEditorTextArea().getText(GUIHandler.getCursorLocation() - 1, 1);
                    correctEdit(character);
                    undoer.removeTextUndo(GUIHandler.getCursorLocation(), character);
                } catch (BadLocationException ex) {
                    throw new RuntimeException(ex);
                }
                removeModifiers(e);
                return true;
            }
            int from = GUIHandler.getEditorTextArea().getCaretPosition();
            int to = leftClosestBorder(GUIHandler.getCursorLocation());
            GUIHandler.getEditorTextArea().setSelectionStart(to);
            GUIHandler.getEditorTextArea().setSelectionEnd(from);
            if (to != from) {
                try {
                    var text = GUIHandler.getEditorTextArea().getText(to, from - to);
                    correctEdit(text);
                    undoer.removeTextUndo(to + 1, text);
                } catch (BadLocationException ex) {
                    throw new RuntimeException(ex);
                }
                GUIHandler.getEditorTextArea().replaceSelection("");
            }
            e.consume();
            return true;
        }
        return false;
    }

    private static void correctEdit(String selectedText) {
        if (selectedText.contains("\n")) {
            actionToDo = ACTION.FULL_QT;
        } else {
            actionToDo = ACTION.LINE_QT;
        }
    }

    private boolean doDelete(KeyEvent e, int charCode) {
        if (charCode == KeyEvent.VK_DELETE) {
            var selectedText = GUIHandler.getEditorTextArea().getSelectedText();
            if (selectedText != null) {
                e.consume();
                if (e.isShiftDown()) {
                    deleteLines(undoer);
                    return true;
                }
                GUIHandler.getEditorTextArea().replaceSelection("");
                undoer.removeTextUndo(GUIHandler.getCursorLocation(), selectedText);
                correctEdit(selectedText);
                return true;
            }

            if (e.isShiftDown()) {
                var line = deleteLine();
                if (line.isEmpty()) return true;
                undoer.removeTextUndo(GUIHandler.getCursorLocation(), line);
                actionToDo = ACTION.FULL_QT;
                e.consume();
                return true;
            }
            if (!e.isControlDown()) {
                try {
                    if (GUIHandler.getEditorTextLength() == GUIHandler.getCursorLocation()) {
                        return true;
                    }
                    var character = GUIHandler.getEditorTextArea().getText(GUIHandler.getCursorLocation(), 1);
                    correctEdit(character);
                    undoer.removeTextUndo(GUIHandler.getCursorLocation() + 1, character);
                } catch (BadLocationException ex) {
                    throw new RuntimeException(ex);
                }
                removeModifiers(e);
                return true;
            }

            int from = rightClosestBorder(GUIHandler.getEditorTextArea().getSelectionEnd());
            if (from == GUIHandler.getEditorTextLength())
                from--;
            int to = GUIHandler.getEditorTextArea().getCaretPosition();
            GUIHandler.getEditorTextArea().setSelectionStart(to);
            GUIHandler.getEditorTextArea().setSelectionEnd(from);
            if (to != from) {
                try {
                    var text = GUIHandler.getEditorTextArea().getText(to, from - to);
                    correctEdit(text);
                    undoer.removeTextUndo(to + 1, text);
                } catch (BadLocationException ex) {
                    throw new RuntimeException(ex);
                }
                GUIHandler.getEditorTextArea().replaceSelection("");
            }
            e.consume();
            return true;
        }
        return false;
    }

    private static void removeModifiers(KeyEvent e) {
        e.setModifiers(0);
    }

    private static int getNextCtrlMovement(int charCode) {
        int next = -1;
        if (charCode == KeyEvent.VK_LEFT) {
            next = (leftClosestBorder(GUIHandler.getCursorLocation()));
        } else if (charCode == KeyEvent.VK_RIGHT) {
            next = (rightClosestBorder(GUIHandler.getEditorTextArea().getSelectionEnd()));
        } else if (charCode == KeyEvent.VK_UP) {
            next = (getPreviousEnterIndex());
        } else if (charCode == KeyEvent.VK_DOWN) {
            next = (getNextEnterIndex());
        }
        return next;
    }

    static int getPreviousEnterIndex() {
        var caretPosition = GUIHandler.getEditorTextArea().getSelectionStart() - 1;
        int lineStart;
        try {
            int line;
            if (caretPosition == -1) {
                line = GUIHandler.getEditorTextArea().getLineOfOffset(GUIHandler.getEditorTextLength() - 1);
            } else {
                line = GUIHandler.getCurrentLine();
            }
            lineStart = GUIHandler.getEditorTextArea().getLineStartOffset(line);
            return lineStart;
        } catch (BadLocationException e) {
            return 0;
        }
    }

    static int getNextEnterIndex() {
        var caretPosition = GUIHandler.getEditorTextArea().getSelectionStart();
        try {
            Matcher matcher = spaceOrEnter.matcher(
                    GUIHandler.getEditorTextArea().getText(caretPosition, GUIHandler.getEditorTextLength() - caretPosition).stripLeading());
            int lastIndex = -1;
            while (matcher.find()) {
                lastIndex = matcher.start();
            }
            return lastIndex + 1;
        } catch (BadLocationException e) {
            return GUIHandler.getEditorTextLength();
        }
    }

    static int leftClosestBorder(int caretPosition) {
        try {
            Matcher matcher = spaceOrEnter.matcher(GUIHandler.getEditorTextArea().getText(0, caretPosition - 1).stripTrailing());
            int lastIndex = -1;
            while (matcher.find()) {
                lastIndex = matcher.start();
            }
            return lastIndex + 1;
        } catch (BadLocationException e) {
            return 0;
        }
    }

    static int rightClosestBorder(int caretPosition) {
        try {
            var thingo = GUIHandler.getEditorTextArea().getText(caretPosition, GUIHandler.getEditorTextLength() - caretPosition);
            var bingo = thingo.stripLeading();
            var calc = thingo.length() - bingo.length();
            Matcher matcher = spaceOrEnter.matcher(bingo.stripLeading());
            int lastIndex = -1;
            if (matcher.find()) {
                lastIndex = matcher.start() + calc;
            }
            if (lastIndex == -1) {
                return GUIHandler.getEditorTextLength();
            }
            return caretPosition + lastIndex;
        } catch (BadLocationException e) {
            return GUIHandler.getEditorTextLength();
        }
    }

    static boolean ignoreKeys(int charTyped) {
        return charTyped == KeyEvent.VK_SHIFT || charTyped == KeyEvent.VK_CONTROL ||
                charTyped == KeyEvent.VK_ALT || charTyped == KeyEvent.VK_META ||
                charTyped == KeyEvent.VK_CAPS_LOCK || charTyped == KeyEvent.VK_ALT_GRAPH;
    }

    static String deleteLine() {
        int lineStart, lineEnd;
        try {
            int line = GUIHandler.getCurrentLine();
            lineStart = GUIHandler.getEditorTextArea().getLineStartOffset(line);
            lineEnd = GUIHandler.getEditorTextArea().getLineEndOffset(line);
        } catch (BadLocationException e) {
            throw new RuntimeException(e);
        }
        GUIHandler.getEditorTextArea().select(lineStart, lineEnd);
        var selected = GUIHandler.getEditorTextArea().getSelectedText();
        if (selected == null) return "";

        GUIHandler.getEditorTextArea().replaceRange("", GUIHandler.getEditorTextArea().getSelectionStart(), GUIHandler.getEditorTextArea().getSelectionEnd());
        return selected;
    }

    static void deleteLines(Undoer undoer) {
        int lineStart, lineEnd;
        try {
            int lineStartX = GUIHandler.getEditorTextArea().getSelectionStart();
            int lineEndX = GUIHandler.getEditorTextArea().getSelectionStart();
            lineStart = GUIHandler.getEditorTextArea().getLineStartOffset(lineStartX);
            lineEnd = GUIHandler.getEditorTextArea().getLineEndOffset(lineEndX);
        } catch (BadLocationException e) {
            throw new RuntimeException(e);
        }
        GUIHandler.getEditorTextArea().select(lineStart, lineEnd);
        var selected = GUIHandler.getEditorTextArea().getSelectedText();
        if (selected == null) return;

        undoer.removeTextUndo(lineStart, selected);
        GUIHandler.getEditorTextArea().replaceRange("", GUIHandler.getEditorTextArea().getSelectionStart(), GUIHandler.getEditorTextArea().getSelectionEnd());
    }

    void removeTab(boolean shiftDown) {
        try {
            boolean anyTabs = !shiftDown;
            var lineNumQuickEnd = GUIHandler.getEditorTextArea().getLineOfOffset(GUIHandler.getEditorTextArea().getSelectionEnd());
            var lineNumQuickStart = GUIHandler.getEditorTextArea().getLineOfOffset(GUIHandler.getEditorTextArea().getSelectionStart());
            for (int i = lineNumQuickStart; i <= lineNumQuickEnd; i++) {
                int index = GUIHandler.getEditorTextArea().getLineStartOffset(i);
                if (shiftDown) {
                    if (GUIHandler.getEditorTextArea().getText(index, 1).equals("\t")) {
                        GUIHandler.getEditorTextArea().setText(GUIHandler.getEditorTextArea().getText(0, index) +
                                GUIHandler.getEditorTextArea().getText(index + 1, GUIHandler.getEditorTextLength() - index - 1));
                        anyTabs = true;
                        //TODO what if you have 3 lines and the middle one has no tab
                    }
                } else {
                    GUIHandler.getEditorTextArea().insert("\t", index);
                }
            }
            if (!anyTabs) return;
            if (lineNumQuickEnd == lineNumQuickStart) {
                actionToDo = ACTION.LINE_QT;
            } else {
                actionToDo = ACTION.FULL_QT;
            }
            undoer.addTabLines(lineNumQuickStart, lineNumQuickEnd, shiftDown);
        } catch (BadLocationException ignored) {

        }
    }
}
