package quicktype;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Quicktype {

    final Map<String, AddedWord> data;

    public Quicktype() {
        try {
            Loading.getData();
            data = Loading.getData().stream().collect(Collectors.toMap(AddedWord::shortCut, a -> a));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public List<AddedWord> getWords(){
        return data.values().stream().toList();
    }
}
