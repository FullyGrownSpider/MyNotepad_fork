package qtnotes.actions;

public class UndoAction {
    public int location;
    public StringBuilder text;
    public UndoActionType actionType;

    public UndoAction(int location, String text, UndoActionType deleted) {
        this.location = location;
        this.text = new StringBuilder(text);
        this.actionType = deleted;
    }

    @Override
    public String toString(){
        return location + "-" + text.toString();
    }

    public boolean isTab() {
        return location == -1;
    }
}