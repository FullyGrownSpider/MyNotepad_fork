public class UndoAction {
    public int location;
    public String text;
    public boolean deleted;

    public UndoAction(int location, String text, boolean deleted) {
        this.location = location;
        this.text = text;
        this.deleted = deleted;
    }
}
