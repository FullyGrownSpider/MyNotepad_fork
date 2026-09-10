package qtnotes.spellcheck;

import java.io.IOException;
import java.util.List;

public class SearchThread implements Runnable {
    private List<String> found;

    public SearchThread(List<String> toSearch){
        found = toSearch;
    }
    @Override
    public void run() {
        found = found.stream().filter((x) -> {
            try {
                return Compression.find(x) > -1;
            } catch (IOException e) {
                return false;
            }
        }).toList();
    }

    public List<String> getValue() {
        return found;
    }
}
