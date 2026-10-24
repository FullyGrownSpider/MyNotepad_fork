package qtnotes.spellcheck;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SearchThread implements Runnable {
    private List<ArrayList<byte[]>> found;

    public SearchThread(List<ArrayList<byte[]>> toSearch) {
        found = toSearch;
    }

    @Override
    public void run() {
        var ref = new Object() {
            int lastLocation;
        };
        try {
            ref.lastLocation = Math.abs(Compression.find(found.get(0), -1, 0)) - 7;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        found = found.stream().filter((x) -> {
            try {
                int location = Compression.find(x, ref.lastLocation + 15, ref.lastLocation - 15);

                if (location + ref.lastLocation > 10) {
                    int secondLocation = Compression.find(x, ref.lastLocation, ref.lastLocation - 31);
                    if (secondLocation + ref.lastLocation < 30) {
                        location = secondLocation;
                    } else {
                        location = Compression.find(x, ref.lastLocation, ref.lastLocation - 5000);
                    }
                }

                if (location > -1) {
                    ref.lastLocation = location;
                    return true;
                }
                ref.lastLocation = -location;
                return false;
            } catch (IOException e) {
                return false;
            }
        }).toList();
    }

    public List<ArrayList<byte[]>> getValue() {
        return found;
    }
}
