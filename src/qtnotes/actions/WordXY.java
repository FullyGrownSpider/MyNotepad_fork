package qtnotes.actions;

public class WordXY implements Comparable<WordXY>{

    public WordXY(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int x, y;
    @Override
    public int compareTo(WordXY o) {
        return x -o.x;
    }
}
