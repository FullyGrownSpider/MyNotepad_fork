package quicktype;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;

public class Loading {
    public static String path;
    public static ArrayList<AddedWord> getData() throws IOException {
        String file = Files.readString(Path.of(path));
        return new ArrayList<>(Arrays.stream(file.split("\n")).map((x) -> new AddedWord(x.trim().split(" "))).sorted().distinct().toList());
    }

    public static void save(ArrayList<AddedWord> wordList) throws IOException {
        var export = String.join("\n", wordList.stream().map(AddedWord::export).sorted().toList());
        Files.writeString(Path.of(path), export);
    }
}
