package qtnotes.actions;

import java.awt.event.KeyEvent;
import java.util.ArrayList;

public class Undoer {

    ///Public for testing
    public int undoIndex = 0;
    private static final int MAX_LIST = 48;
    private static final int MAX_SIZE_UNDO = 10;
    private final ArrayList<UndoAction> undoActionList = new ArrayList<>(MAX_LIST);

    public Undoer() {
        //init the undo list
        for (int i = undoIndex; i < 48; i++) {
            undoActionList.add(null);
        }

    }

    ///Public for testing
    public UndoAction getUndoAction(int get){
        return undoActionList.get(get);
    }

    public void removeTextUndo(int location, String changedText){
        addToUndo(location, changedText, UndoActionType.DELETE);
    }

    public void addingTextUndo(int location, String changedText) {
        addToUndo(location, changedText, UndoActionType.ADD);
    }

    public static int[] getTabLines(UndoAction action) {
        var thing = action.text.toString().split("x");
        return new int[]{Integer.parseInt(thing[0]), Integer.parseInt(thing[1])};
    }

    public void addTabLines(int from, int to, boolean added) {
        if (added){
           addingTextUndo(-1, from +"x"+to);
        } else {
            removeTextUndo(-1, from +"x"+to);
        }
    }

    private void addToUndo(int location, String changedText, UndoActionType undoAction){
        if (undoIndex == -1) moveToNextIndex();
        var action = undoActionList.get(undoIndex);

        //already moved
        if (alreadyMoved(location, changedText, action, undoAction)) return;
        var spaceCheck = action.text.toString();
        var isOnlySplitters = spaceCheck.length() > 2 && isSplitter(spaceCheck.charAt(1)) && !isSplitter(changedText.charAt(0));
        // " "S
        if (isOnlySplitters){
            moveToNextIndex();
            undoActionList.set(undoIndex, new UndoAction(location, changedText, undoAction));
            return;
        }

        if (undoAction == UndoActionType.DELETE) {
            if (location == action.location -1) {
                action.text.insert(0, changedText);
            } else {
                action.text.append(changedText);
            }
            action.location = location;
        } else {
            action.text.append(changedText);
        }
        //its a single splitter char (S-TL)
        if (isSplitter(changedText.charAt(0))){
            //water(S)
            if (!isSplitter(spaceCheck.charAt(spaceCheck.length()-1))) {
                moveToNextIndex();
            }
        }
    }

    private boolean alreadyMoved(int location, String changedText, UndoAction action, UndoActionType undoAction) {
        //no action
        if (action == null){
            undoActionList.set(undoIndex, new UndoAction(location, changedText, undoAction));
            if (changedText.length() != 1){
                moveToNextIndex();
            }
            return true;
        }
        //the text is long
        //it's not the same spot
        //the action is not the same action type
        //word is too long
        else if (changedText.length() != 1
                || (UndoActionType.DELETE == undoAction ? !(location == action.location -1 || location == action.location) : location != action.location + action.text.length())
                || (action.actionType != undoAction)
                || (action.text.length() >= MAX_SIZE_UNDO)){
            moveToNextIndex();
            undoActionList.set(undoIndex, new UndoAction(location, changedText, undoAction));
            return true;
        }
        return false;

    }


    private static boolean isSplitter(int c) {
        return c == KeyEvent.VK_ENTER || c == KeyEvent.VK_SPACE || c == KeyEvent.VK_TAB || c == KeyEvent.VK_MINUS;
    }


    private void moveToNextIndex() {
        if (undoIndex == -1){
            undoIndex = 0;
            if (undoActionList.get(undoIndex) != null) clearUndoList();
            return;
        }
        if (undoActionList.get(undoIndex) == null) return;
        undoIndex++;
        if (MAX_LIST <= undoIndex){
            undoActionList.remove(0);
            undoActionList.add(null);
            undoIndex--;
        }
        else if (undoActionList.get(undoIndex) != null){
            clearUndoList();
        }
    }

    private void clearUndoList(){
        for (int i = undoIndex; i < undoActionList.size(); i++){
            undoActionList.set(i, null);
        }
    }

    public UndoAction undoAnAction(){
        if (undoIndex == -1) return null;
        var action = undoActionList.get(undoIndex);
        undoIndex--;
        if (action == null){
            return undoAnAction();
        }
        return action;
    }

    public UndoAction redoAnAction(){
        if (undoIndex >= MAX_LIST - 1) return null;
        if (undoIndex != -1 && undoActionList.get(undoIndex) == null) return null;
        undoIndex++;
        return undoActionList.get(undoIndex);
    }
}
