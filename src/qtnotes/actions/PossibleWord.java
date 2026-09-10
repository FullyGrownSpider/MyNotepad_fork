package qtnotes.actions;

public class PossibleWord {
    public String word;
    public boolean shouldLoop;
    public WordXY location;

    public PossibleWord(String word, boolean shouldLoop, WordXY location) {
        this.word = word;
        this.shouldLoop = shouldLoop;
        this.location = location;
    }
}
