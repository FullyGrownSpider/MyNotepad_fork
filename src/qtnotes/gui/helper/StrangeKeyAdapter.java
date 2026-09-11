package qtnotes.gui.helper;

import qtnotes.actions.UndoAction;
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
import java.util.ArrayList;
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
            var selected = GUIHandler.getSelectedText();
            var location = GUIHandler.getCursorLocationOrSelectStart();
            if (selected != null){
                undoer.removeTextUndo(location, selected);
            }
            undoer.addingTextUndo(location, "\n");
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

        var selected = GUIHandler.getSelectedText();

        if (charCode == KeyEvent.VK_TAB && (e.isShiftDown() || selected != null)) {
            var locationStart = GUIHandler.getCursorLocationOrSelectStart();
            var locationEnd = GUIHandler.getEditorTextArea().getSelectionEnd();
            removeTab(e.isShiftDown(), true);
            e.consume();
            if (e.isShiftDown()) {
                GUIHandler.setSelectedOffset(locationStart,locationEnd);
            }
            return;
        }
        if (selected != null) {
            undoer.removeTextUndo(GUIHandler.getCursorLocationOrSelectStart(), selected);
            undoer.addingTextUndo(GUIHandler.getCursorLocationOrSelectStart(), "" + e.getKeyChar());
            actionToDo = ACTION.FULL_QT;
            return;
        }
        actionToDo = ACTION.LINE_QT;
        undoer.addingTextUndo(GUIHandler.getCursorLocationOrSelectStart(), "" + e.getKeyChar());
    }

    private boolean undoRedo(KeyEvent e, int charCode) {
        if (!e.isControlDown()) return false;
        if (charCode == KeyEvent.VK_Y) {
            var thing = undoer.redoAnAction();
            if (thing == null) return false;
            if (thing.isTab()) {
                doTabUndo(thing,true);
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
                doTabUndo(thing,false);
            } else {
                var text = thing.text.toString();
                if (thing.actionType == UndoActionType.ADD) {
                    GUIHandler.getEditorTextArea().replaceRange("", thing.location, thing.location + text.length());
                } else {
                    GUIHandler.getEditorTextArea().insert(text, Math.max(thing.location - 1,0));
                }
                correctEdit(text);
            }
            e.consume();
            return true;
        }
        return false;
    }

    private void doTabUndo(UndoAction thing, boolean shouldRemoveAdded) {
        var ints = Undoer.getTabLines(thing);
        try {
            GUIHandler.setSelectedOffset(
                    GUIHandler.getEditorTextArea().getLineStartOffset(Integer.parseInt(ints[0])),
                    GUIHandler.getEditorTextArea().getLineEndOffset(Integer.parseInt(ints[1])));
        } catch (BadLocationException ex) {
            throw new RuntimeException(ex);
        }
        removeTab(shouldRemoveAdded, false);
        for (int i = 2; i < ints.length; i++) {
            try {
                GUIHandler.setSelectedOffset(
                        GUIHandler.getEditorTextArea().getLineStartOffset(Integer.parseInt(ints[i])),
                        GUIHandler.getEditorTextArea().getLineEndOffset(Integer.parseInt(ints[i])));
                removeTab(!shouldRemoveAdded, false);
            } catch (BadLocationException ex) {
                throw new RuntimeException(ex);
            }
        }
        actionToDo = ACTION.FULL_QT;
        try {
            GUIHandler.setSelectedOffset(
                    GUIHandler.getEditorTextArea().getLineStartOffset(Integer.parseInt(ints[0])),
                    GUIHandler.getEditorTextArea().getLineEndOffset(Integer.parseInt(ints[1])));
        } catch (BadLocationException ex) {
            throw new RuntimeException(ex);
        }
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
        var selected = GUIHandler.getSelectedText();
        if (selected != null) {
            undoer.removeTextUndo(GUIHandler.getCursorLocationOrSelectStart(), selected);
            GUIHandler.getEditorTextArea().replaceRange("", GUIHandler.getEditorTextArea().getSelectionStart(), GUIHandler.getEditorTextArea().getSelectionEnd());
        }
        Clipboard cb = Toolkit.getDefaultToolkit().getSystemClipboard();
        try {
            String text;
            var trans = cb.getContents(null);
            if (trans.isDataFlavorSupported(DataFlavor.stringFlavor)) {
                text = (String) trans.getTransferData(DataFlavor.stringFlavor);
                undoer.addingTextUndo(GUIHandler.getCursorLocationOrSelectStart(), text);
            }

        } catch (IOException | UnsupportedFlavorException ex) {
            throw new RuntimeException(ex);
        }
    }

    void CopyIntoEdit(boolean cut) {
        var selected = GUIHandler.getSelectedText();
        if (selected == null) {
            int lineStart, lineEnd;
            try {
                int line = GUIHandler.getCurrentLine();
                lineStart = GUIHandler.getEditorTextArea().getLineStartOffset(line);
                lineEnd = GUIHandler.getEditorTextArea().getLineEndOffset(line);
            } catch (BadLocationException e) {
                throw new RuntimeException(e);
            }
            GUIHandler.setSelectedOffset(lineStart, lineEnd);
            selected = GUIHandler.getEditorTextArea().getSelectedText();
            if (selected == null || selected.isEmpty()) return;
        }
        if (cut) {
            undoer.removeTextUndo(GUIHandler.getCursorLocationOrSelectStart(), selected);
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
            GUIHandler.setSelectedOffset(from,next);
        } else {
            GUIHandler.setSelectedOffset(next, GUIHandler.getEditorTextArea().getSelectionEnd());
        }
    }

    private static void homeOrEnd(KeyEvent e, boolean home) {
        if (e.isControlDown()) {
            if (e.isShiftDown()) {
                GUIHandler.setSelectedOffset(
                        home ? 0: GUIHandler.getCursorLocationOrSelectStart(),
                        home ? GUIHandler.getEditorTextArea().getSelectionEnd() : GUIHandler.getEditorTextLength());
            } else {
                GUIHandler.getEditorTextArea().setCaretPosition(home ? 0: GUIHandler.getEditorTextLength());
            }
        } else {
            int line, linePoint;
            try {
                line = GUIHandler.getEditorTextArea().getLineOfOffset(home? GUIHandler.getCursorLocationOrSelectStart() : GUIHandler.getEditorTextArea().getSelectionEnd());
                if (home)
                    linePoint = GUIHandler.getEditorTextArea().getLineStartOffset(line);
                else {
                    linePoint = GUIHandler.endOfLine(line);
                }
            } catch (BadLocationException ex) {
                throw new RuntimeException(ex);
            }
            if (e.isShiftDown()) {
                GUIHandler.setSelectedOffset(home ? linePoint : GUIHandler.getCursorLocationOrSelectStart(),
                        home ? GUIHandler.getEditorTextArea().getSelectionEnd() : linePoint);
            } else {
                GUIHandler.getEditorTextArea().setCaretPosition(linePoint);
            }
        }
        e.consume();
    }

    private boolean doBackspace(KeyEvent e, int charCode) {
        if (charCode == KeyEvent.VK_BACK_SPACE) {
            var selectedText = GUIHandler.getSelectedText();
            if (selectedText != null) {
                undoer.removeTextUndo(GUIHandler.getCursorLocationOrSelectStart(), selectedText);
                GUIHandler.getEditorTextArea().replaceSelection("");
                correctEdit(selectedText);
                e.consume();
                return true;
            }
            if (!e.isControlDown()) {
                try {
                    var character = GUIHandler.getTextLength(GUIHandler.getCursorLocationOrSelectStart() - 1, 1);
                    correctEdit(character);
                    undoer.removeTextUndo(GUIHandler.getCursorLocationOrSelectStart(), character);
                } catch (BadLocationException ex) {
                    throw new RuntimeException(ex);
                }
                removeModifiers(e);
                return true;
            }
            int to = GUIHandler.getEditorTextArea().getCaretPosition();
            int from = leftClosestBorder(GUIHandler.getCursorLocationOrSelectStart());
            GUIHandler.setSelectedOffset(from, to);
            if (from != to) {
                try {
                    var text = GUIHandler.getTextOffset(from, to);
                    correctEdit(text);
                    undoer.removeTextUndo(from + 1, text);
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
            var selectedText = GUIHandler.getSelectedText();
            if (selectedText != null) {
                e.consume();
                if (e.isShiftDown()) {
                    deleteLines(undoer);
                    actionToDo = ACTION.FULL_QT;
                    return true;
                }
                GUIHandler.getEditorTextArea().replaceSelection("");
                undoer.removeTextUndo(GUIHandler.getCursorLocationOrSelectStart(), selectedText);
                correctEdit(selectedText);
                return true;
            }

            if (e.isShiftDown()) {
                var line = deleteLine();
                if (line.isEmpty()) return true;
                undoer.removeTextUndo(GUIHandler.getCursorLocationOrSelectStart(), line);
                actionToDo = ACTION.FULL_QT;
                e.consume();
                return true;
            }
            if (!e.isControlDown()) {
                try {
                    if (GUIHandler.getEditorTextLength() == GUIHandler.getCursorLocationOrSelectStart()) {
                        return true;
                    }
                    var character = GUIHandler.getTextLength(GUIHandler.getCursorLocationOrSelectStart(), 1);
                    correctEdit(character);
                    undoer.removeTextUndo(GUIHandler.getCursorLocationOrSelectStart() + 1, character);
                } catch (BadLocationException ex) {
                    throw new RuntimeException(ex);
                }
                removeModifiers(e);
                return true;
            }

            int to = rightClosestBorder(GUIHandler.getEditorTextArea().getSelectionEnd());
            if (to == GUIHandler.getEditorTextLength())
                to--;
            int from = GUIHandler.getEditorTextArea().getCaretPosition();
            GUIHandler.setSelectedOffset(from, to);
            if (from != to) {
                try {
                    var text = GUIHandler.getTextOffset(from, to);
                    correctEdit(text);
                    undoer.removeTextUndo(from + 1, text);
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
            next = (leftClosestBorder(GUIHandler.getCursorLocationOrSelectStart()));
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
        var caretPosition = Math.max(0, GUIHandler.getCurrentLine() - 1);
        try {
            return GUIHandler.getEditorTextArea().getLineStartOffset(caretPosition);
        } catch (BadLocationException e) {
            return 0;
        }
    }

    static int getNextEnterIndex() {
        var currentLine = GUIHandler.getCurrentLine()+1;
        try {
            return GUIHandler.endOfLine(currentLine);
        } catch (BadLocationException e) {
            return GUIHandler.getEditorTextLength();
        }
    }

    static int leftClosestBorder(int caretPosition) {
        try {
            Matcher matcher = spaceOrEnter.matcher(GUIHandler.getTextLength(0, caretPosition - 1).stripTrailing());
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
            var thingo = GUIHandler.getTextLength(caretPosition, GUIHandler.getEditorTextLength() - caretPosition);
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
        GUIHandler.setSelectedOffset(lineStart, lineEnd);
        var selected = GUIHandler.getSelectedText();
        if (selected == null) return "";

        GUIHandler.getEditorTextArea().replaceRange("", GUIHandler.getCursorLocationOrSelectStart(), GUIHandler.getSelectionEnd());
        return selected;
    }

    static void deleteLines(Undoer undoer) {
        int lineOffsetStart, lineOffsetEnd;
        try {
            var carrotStart = GUIHandler.getCursorLocationOrSelectStart();
            var carrotEnd = GUIHandler.getEditorTextArea().getSelectionEnd();
            var selected = GUIHandler.getSelectedText();
            GUIHandler.getEditorTextArea().setCaretPosition(carrotStart);
            int lineStartX = GUIHandler.getCurrentLine();
            GUIHandler.getEditorTextArea().setCaretPosition(carrotEnd);
            int lineEndX = GUIHandler.getCurrentLine();
            lineOffsetStart = GUIHandler.getEditorTextArea().getLineStartOffset(lineStartX);
            lineOffsetEnd = GUIHandler.getEditorTextArea().getLineEndOffset(lineEndX);
            undoer.removeTextUndo(lineOffsetStart, selected);
            GUIHandler.getEditorTextArea().replaceRange("", lineOffsetStart, lineOffsetEnd);
        } catch (BadLocationException e) {
            throw new RuntimeException(e);
        }
    }

    void removeTab(boolean shiftDown, boolean ableToUndo) {
        try {
            boolean anyTabs = !shiftDown;
            var lineNumQuickEnd = GUIHandler.getEditorTextArea().getLineOfOffset(GUIHandler.getEditorTextArea().getSelectionEnd()) - 1;
            var lineNumQuickStart = GUIHandler.getEditorTextArea().getLineOfOffset(GUIHandler.getEditorTextArea().getSelectionStart());
            ArrayList<String> skips = new ArrayList<>();
            for (int i = lineNumQuickStart; i <= lineNumQuickEnd; i++) {
                int index = GUIHandler.getEditorTextArea().getLineStartOffset(i);
                if (shiftDown) {
                    if (GUIHandler.getTextLength(index, 1).equals("\t")) {
                        GUIHandler.setEditorText(GUIHandler.getTextLength(0, index) +
                                GUIHandler.getTextLength(index + 1, GUIHandler.getEditorTextLength() - index - 1));
                        anyTabs = true;
                    } else {
                        skips.add("" + i);
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
            if (ableToUndo) {
                undoer.addTabLines(lineNumQuickStart, lineNumQuickEnd, shiftDown, skips.toArray(new String[0]));
            }
        } catch (BadLocationException ignored) {

        }
    }
}
