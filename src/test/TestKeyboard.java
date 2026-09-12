package test;

import org.junit.Assert;
import org.junit.Test;
import qtnotes.actions.Undoer;
import qtnotes.gui.GUIHandler;
import qtnotes.gui.helper.StrangeKeyAdapter;
import qtnotes.quicktype.Loading;
import qtnotes.spellcheck.Compression;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.Arrays;

@SuppressWarnings("OptionalGetWithoutIsPresent")
public class TestKeyboard {

    public GUIHandler gui;
    public String simpleTestText = "wwwwww\nwwwwww\nwwwwww";
    //words of 3 long and jumps of 4(bkh of ' ')
    public String longLineTestText = "www www www zt= zt[ zt. 'to bee oor not too Be' --- ...\nwww www";
    private static int counter= 0;
    private static StrangeKeyAdapter adapter;

    @Test
    public void fullTest(){
        Loading.path = "/home/a804/Documents/MyTyper/MyChords.txt";
        Compression.path = "/home/a804/Documents/MyTyper/words_bin.dat";

        gui = new GUIHandler();
        gui.handle();

        adapter = (StrangeKeyAdapter) Arrays.stream(GUIHandler.getEditorTextArea().getKeyListeners()).filter((listener) -> listener instanceof StrangeKeyAdapter).findFirst().get();
        leftRightUpDown();
        copyPaste();
        delete();
        //backspace
        //tab
        //letterWhileSelected
        //normalTyping(only undo-ing)
    }

    private void delete(){
        //remove first char
        doDeleteThing(0,simpleTestText,KeyEvent.VK_DELETE, false, false, simpleTestText.substring(1), 0, 1,0);
        //remove nothing because you are at the last char
        doDeleteThing(simpleTestText.length(),simpleTestText,KeyEvent.VK_DELETE, false, false, simpleTestText, simpleTestText.length(), simpleTestText.length(),simpleTestText.length());
        //remove last char
        doDeleteThing(simpleTestText.length()-1,simpleTestText,KeyEvent.VK_DELETE, false, false, simpleTestText.substring(0, simpleTestText.length()-2), simpleTestText.length()-1, simpleTestText.length(),simpleTestText.length());

        //delete first line
        doDeleteThing(0,simpleTestText,KeyEvent.VK_DELETE, false, true, "wwwwww\nwwwwww", 0,7,0);

        //delete last line
        doDeleteThing(14,simpleTestText,KeyEvent.VK_DELETE, false, true, "wwwwww\nwwwwww", 13,20,13);

        //delete no line
        doDeleteThing(0,"",KeyEvent.VK_DELETE, false, true, "", 0,0,0);

        //delete line but holding ctrl
        //delete first line
        doDeleteThing(0,simpleTestText,KeyEvent.VK_DELETE, true, true, "wwwwww\nwwwwww", 0,7,0);

        //delete last line
        doDeleteThing(14,simpleTestText,KeyEvent.VK_DELETE, true, true, "wwwwww\nwwwwww", 13,20,13);

        //delete no line
        doDeleteThing(0,"",KeyEvent.VK_DELETE, true, true, "", 0,0,0);

        //delete word
        doDeleteThing(0,simpleTestText,KeyEvent.VK_DELETE, true, false, "\nwwwwww\nwwwwww", 0,6,0);
        //delete last word
        doDeleteThing(14,simpleTestText,KeyEvent.VK_DELETE, true, false, "wwwwww\nwwwwww\n", 14,6,0);
        //delete no word
        doDeleteThing(20,simpleTestText,KeyEvent.VK_DELETE, true, false, simpleTestText, 20,20,20);

        //select
        GUIHandler.setEditorText(simpleTestText);
        pressButton((char) KeyEvent.VK_DELETE, true, false);
        GUIHandler.setEditorText(simpleTestText);
        pressButton((char) KeyEvent.VK_DELETE, false, false);
        GUIHandler.setEditorText(simpleTestText);
        pressButton((char) KeyEvent.VK_DELETE, false, true);
        //select at line end
        GUIHandler.setEditorText(simpleTestText);
        pressButton((char) KeyEvent.VK_DELETE, true, false);
        GUIHandler.setEditorText(simpleTestText);
        pressButton((char) KeyEvent.VK_DELETE, false, false);
        GUIHandler.setEditorText(simpleTestText);
        pressButton((char) KeyEvent.VK_DELETE, false, true);
    }

    private void doDeleteThing(int position, String setText, int keyChar, boolean ctrlDown, boolean shiftDown, String textAfter, int positionAfter, int posAfterUndo, int posAfterRedo) {
        adapter.undoer = new Undoer();
        GUIHandler.getEditorTextArea().setCaretPosition(position);
        GUIHandler.setEditorText(setText);
        pressButton((char) keyChar, ctrlDown, shiftDown);
        Assert.assertEquals(textAfter, GUIHandler.getEditorText());
        checkPosition(positionAfter);
        pressButton((char) KeyEvent.VK_Z, true, false);
        Assert.assertEquals(setText, GUIHandler.getEditorText());
        checkPosition(posAfterUndo);
        pressButton((char) KeyEvent.VK_Y, true, false);
        Assert.assertEquals(textAfter, GUIHandler.getEditorText());
        checkPosition(posAfterRedo);
    }

    private void copyPaste(){
        GUIHandler.setEditorText(simpleTestText);

        GUIHandler.getEditorTextArea().select(7, 13);
        GUIHandler.getEditorTextArea().replaceSelection("xxxxxx");
        GUIHandler.getEditorTextArea().select(7, 13);

        //cut and undo
        pressButton((char) KeyEvent.VK_X, true, false);
        Assert.assertEquals("wwwwww\n\nwwwwww", GUIHandler.getEditorText());
        selectCheck(null);
        checkPosition(7);
        pressButton((char) KeyEvent.VK_Z, true, false);
        Assert.assertEquals("wwwwww\nxxxxxx\nwwwwww", GUIHandler.getEditorText());
        selectCheck("xxxxxx");
        pressButton((char) KeyEvent.VK_Y, true, false);
        Assert.assertEquals("wwwwww\n\nwwwwww", GUIHandler.getEditorText());
        checkPosition(7);

        pressButton((char) KeyEvent.VK_Z, true, false);

        //add and undo
        GUIHandler.getEditorTextArea().select(7, 13);
        GUIHandler.getEditorTextArea().replaceSelection("wwwwww");
        GUIHandler.getEditorTextArea().select(7, 13);

        pressButton((char) KeyEvent.VK_V, true, false);
        Assert.assertEquals("wwwwww\nxxxxxx\nwwwwww", GUIHandler.getEditorText());
        selectCheck("xxxxxx");
        checkPosition(13);

        pressButton((char) KeyEvent.VK_Z, true, false);
        Assert.assertEquals("wwwwww\n\nwwwwww", GUIHandler.getEditorText());
        checkPosition(7);

        pressButton((char) KeyEvent.VK_Z, true, false);
        Assert.assertEquals(simpleTestText, GUIHandler.getEditorText());
        selectCheck("wwwwww");
        checkPosition(13);

        pressButton((char) KeyEvent.VK_Y, true, false);
        Assert.assertEquals("wwwwww\n\nwwwwww", GUIHandler.getEditorText());
        checkPosition(7);
        pressButton((char) KeyEvent.VK_Y, true, false);
        Assert.assertEquals("wwwwww\nxxxxxx\nwwwwww", GUIHandler.getEditorText());
        selectCheck("xxxxxx");
        checkPosition(13);

        pressButton((char) KeyEvent.VK_Z, true, false);
        pressButton((char) KeyEvent.VK_Z, true, false);


        //cut and undo last line
        GUIHandler.setEditorText("wwwwww\nwwwwww\nxxxxxx");
        GUIHandler.getEditorTextArea().select(14, 20);

        pressButton((char) KeyEvent.VK_X, true, false);
        Assert.assertEquals("wwwwww\nwwwwww\n", GUIHandler.getEditorText());
        selectCheck(null);
        checkPosition(14);
        pressButton((char) KeyEvent.VK_Z, true, false);
        Assert.assertEquals("wwwwww\nwwwwww\nxxxxxx", GUIHandler.getEditorText());
        selectCheck("xxxxxx");
        pressButton((char) KeyEvent.VK_Y, true, false);
        Assert.assertEquals("wwwwww\nwwwwww\n", GUIHandler.getEditorText());
        checkPosition(14);

        pressButton((char) KeyEvent.VK_Z, true, false);
    }

    private void leftRightUpDown(){
        GUIHandler.setEditorText(simpleTestText);

        ctrlDown();

        ctrlUp();

        home();

        end();

        GUIHandler.setEditorText(longLineTestText);

        ctrlLeft();

        ctrlRight();
    }

    private void end() {
        GUIHandler.getEditorTextArea().setCaretPosition(10);
        pressButton((char) KeyEvent.VK_END, false, false);
        checkPosition(13);
        GUIHandler.getEditorTextArea().setCaretPosition(10);
        pressButton((char) KeyEvent.VK_END, true, false);
        checkPosition(20);
        selectCheck(null);

        GUIHandler.getEditorTextArea().setCaretPosition(10);
        pressButton((char) KeyEvent.VK_END, false, true);
        checkPosition(13);
        selectCheck("www");
        GUIHandler.getEditorTextArea().setCaretPosition(10);
        pressButton((char) KeyEvent.VK_END, true, true);
        selectCheck("www\nwwwwww");
    }

    private void home() {
        GUIHandler.getEditorTextArea().setCaretPosition(10);
        pressButton((char) KeyEvent.VK_HOME, false, false);
        checkPosition(7);
        GUIHandler.getEditorTextArea().setCaretPosition(10);
        pressButton((char) KeyEvent.VK_HOME, true, false);
        checkPosition(0);
        selectCheck(null);

        GUIHandler.getEditorTextArea().setCaretPosition(10);
        pressButton((char) KeyEvent.VK_HOME, false, true);
        checkPosition(7);
        selectCheck("www");
        GUIHandler.getEditorTextArea().setCaretPosition(10);
        pressButton((char) KeyEvent.VK_HOME, true, true);
        checkPosition(0);
        selectCheck("wwwwww\nwww");
    }

    private void ctrlRight() {
        GUIHandler.getEditorTextArea().setCaretPosition(0);
        for (int i = 3; i <= 63; i+= 4) {
            pressButton((char) KeyEvent.VK_RIGHT, true, false);
            checkPosition(i);
        }
        checkPosition(63);
        pressButton((char) KeyEvent.VK_RIGHT, true, false);
        checkPosition(63);
        selectCheck(null);
        pressButton((char) KeyEvent.VK_RIGHT, true, true);
        checkPosition(63);
        selectCheck(null);
        GUIHandler.getEditorTextArea().setCaretPosition(0);

        for (int i = 3; i <= 63; i+= 4) {
            pressButton((char) KeyEvent.VK_RIGHT, true, true);
            checkPosition(i);
            selectCheck(longLineTestText.substring(0, i));
        }
        pressButton((char) KeyEvent.VK_RIGHT, true, true);
        checkPosition(63);
        selectCheck(longLineTestText);
        pressButton((char) KeyEvent.VK_RIGHT, true, false);
        checkPosition(63);
        selectCheck(null);
    }

    private void ctrlLeft() {
        GUIHandler.getEditorTextArea().setCaretPosition(63);
        for (int i = 60; i >= 0; i-= 4) {
            pressButton((char) KeyEvent.VK_LEFT, true, false);
            checkPosition(i);
        }
        pressButton((char) KeyEvent.VK_LEFT, true, false);
        checkPosition(0);
        selectCheck(null);
        pressButton((char) KeyEvent.VK_LEFT, true, true);
        checkPosition(0);
        selectCheck(null);
        GUIHandler.getEditorTextArea().setCaretPosition(63);

        for (int i = 60; i >= 0; i-= 4) {
            pressButton((char) KeyEvent.VK_LEFT, true, true);
            checkPosition(i);
            selectCheck(longLineTestText.substring(i));
        }
        pressButton((char) KeyEvent.VK_LEFT, true, true);
        checkPosition(0);
        selectCheck(longLineTestText);
        pressButton((char) KeyEvent.VK_LEFT, true, false);
        checkPosition(0);
        selectCheck(null);
    }

    private void ctrlUp() {
        GUIHandler.getEditorTextArea().setCaretPosition(20);
        pressButton((char)KeyEvent.VK_UP, true, false);
        checkPosition(7);
        pressButton((char)KeyEvent.VK_UP, true, false);
        checkPosition(0);
        pressButton((char)KeyEvent.VK_UP, true, false);
        checkPosition(0);
        selectCheck(null);

        GUIHandler.getEditorTextArea().setCaretPosition(20);

        pressButton((char)KeyEvent.VK_UP, true, true);
        checkPosition(7);
        selectCheck("wwwwww\nwwwwww");
        pressButton((char)KeyEvent.VK_UP, true, true);
        checkPosition(0);
        selectCheck(simpleTestText);
        pressButton((char)KeyEvent.VK_UP, true, true);
        checkPosition(0);
        selectCheck(simpleTestText);
    }

    private void ctrlDown() {
        GUIHandler.getEditorTextArea().setCaretPosition(0);

        pressButton((char)KeyEvent.VK_DOWN, true, false);
        checkPosition(13);
        pressButton((char)KeyEvent.VK_DOWN, true, false);
        checkPosition(20);
        pressButton((char)KeyEvent.VK_DOWN, true, false);
        checkPosition(20);
        selectCheck(null);

        GUIHandler.getEditorTextArea().setCaretPosition(0);

        pressButton((char)KeyEvent.VK_DOWN, true, true);
        checkPosition(13);
        selectCheck("wwwwww\nwwwwww");
        pressButton((char)KeyEvent.VK_DOWN, true, true);
        checkPosition(20);
        selectCheck(simpleTestText);
        pressButton((char)KeyEvent.VK_DOWN, true, true);
        checkPosition(20);
        selectCheck(simpleTestText);
    }

    private void selectCheck(String s) {
        Assert.assertEquals(s, GUIHandler.getEditorTextArea().getSelectedText());
    }

    private void checkPosition(int i) {
        var position = GUIHandler.getEditorTextArea().getCaretPosition();
        Assert.assertEquals(i, position);
    }

    private void pressButton(int key, boolean ctrlDown, boolean shiftDown){
        int amount = (ctrlDown ? KeyEvent.CTRL_DOWN_MASK : 0) +
                (shiftDown? KeyEvent.SHIFT_DOWN_MASK : 0);
        KeyStroke.getKeyStroke(key,amount);
        var event =new KeyEvent(
                GUIHandler.getEditorTextArea(),
                counter,
                0L,
                amount,
                key,
                (char)key);
        counter++;
        adapter.keyPressed(event);
        adapter.keyReleased(event);
    }
}
