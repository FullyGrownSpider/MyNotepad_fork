public class UndoAction {
    public int location;
    public StringBuilder text;
    public boolean deleted;

    public UndoAction(int location, String text, boolean deleted) {
        this.location = location;
        this.text = new StringBuilder(text);
        this.deleted = deleted;
    }

    @Override
    public String toString(){
        return text.toString();
    }
}
