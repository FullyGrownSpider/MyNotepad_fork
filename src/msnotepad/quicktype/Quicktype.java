package quicktype;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Quicktype {

    public final Map<String, AddedWord> data;

    public Quicktype() {
        try {
            data = Loading.getData().stream().collect(Collectors.toMap(AddedWord::shortCut, a -> a));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public List<AddedWord> getWords(){
        return data.values().stream().toList();
    }

    public void addWord(AddedWord word){
        //replaces
        data.put(word.shortCut(), word);
        try {
            ArrayList<AddedWord> arrayList = new ArrayList<>(data.size());
            arrayList.addAll(data.values());
            Loading.save(arrayList);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void removeWord(String shortCut){
        data.remove(shortCut);
        try {
            ArrayList<AddedWord> arrayList = new ArrayList<>(data.size());
            arrayList.addAll(data.values());
            Loading.save(arrayList);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
