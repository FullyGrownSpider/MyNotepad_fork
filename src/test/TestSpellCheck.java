package test;

import org.junit.Assert;
import org.junit.Test;
import qtnotes.spellcheck.Compression;
import qtnotes.spellcheck.Spellcheck;

import java.io.*;
import java.nio.file.*;
import java.util.*;

//this class is for
//rebuilding the files
//
public class TestSpellCheck {
    String fileLocation = "/home/a804/Documents/MyTyper/words.txt";
    String fileLocation3 = "/home/a804/Documents/MyTyper/words_bin.dat";
    String testWord = "beatiful";

    @Test
    public void removeCheck() throws IOException {
        Compression.path = fileLocation3;
        fixBinaryFile();
        var length = Files.readAllBytes(Path.of(fileLocation3)).length;
        Assert.assertTrue(Compression.find(testWord) != -1);
        Compression.remove(testWord);
        Assert.assertTrue(0 > Compression.find(testWord));
        Assert.assertEquals(length, Files.readAllBytes(Path.of(fileLocation3)).length + Compression.textToLetterCompress(testWord).toByteArray().length);
        fixBinaryFile();

    }

    @Test
    public void addCheck() throws IOException, InterruptedException {
        Compression.path = fileLocation3;
        fixBinaryFile();
        var length = Files.readAllBytes(Path.of(fileLocation3)).length;
        Assert.assertTrue(0 > Compression.find(testWord+testWord));
        Compression.add(testWord+testWord);
        Assert.assertEquals(length, Files.readAllBytes(Path.of(fileLocation3)).length - Compression.textToLetterCompress(testWord).toByteArray().length);
        Assert.assertTrue(Compression.find(testWord+testWord) != -1);
        Compression.remove(testWord+testWord);
        Assert.assertEquals(length, Files.readAllBytes(Path.of(fileLocation3)).length);
    }
    @Test
    public void searchTest() throws IOException {
//        var start = System.currentTimeMillis();
        Compression.path = fileLocation3;
        var found = Compression.find("strawberry");
//        System.out.println(System.currentTimeMillis() - start);
        Assert.assertTrue(found > -1);
    }

    @Test
    public void fixBinaryFile() throws IOException {
        Path path = Paths.get(fileLocation);
        Path outPath = Path.of(fileLocation3);
        List<String> lines = Files.readAllLines(path);
        lines.sort(String::compareTo);
        Files.writeString(path, String.join("\n", lines));
        var list = lines.stream().map((x) -> Compression.textToLetterCompress(x).toByteArray()).toList();

        Files.deleteIfExists(outPath);
        Files.createFile(outPath);
        for (var in : list){
            Files.write(outPath, in, StandardOpenOption.APPEND);
        }
    }

    @Test
    public void readTest() throws IOException {
        Compression.textToLetterCompress("aaa");

        BitSet readBits;
        try (DataInputStream in = new DataInputStream(new FileInputStream(fileLocation3))) {
            byte[] byteArray = new byte[15]; // Adjust size accordingly
            in.readFully(byteArray);
            readBits = BitSet.valueOf(byteArray);
        }
        var y = Compression.textFromLetterCompress(readBits);
        var z= 0;
    }

    @Test
    public void checkCompression() throws IOException {
//        assertEquals(testWord, Compression.textFromLetterCompress(Compression.padAndFilter(testWord)));
        var x = Compression.textToLetterCompress(testWord);
        Path path = Paths.get(fileLocation3);
        Files.write(path, x.toByteArray());
        var bytes = Files.readAllBytes(path);
        var y = 0;
    }

    @Test
    public void createSuggestions() throws InterruptedException {
        Compression.path = fileLocation3;
        var start = System.currentTimeMillis();

        var x = Spellcheck.optimizedSearch(testWord);
        System.out.println(System.currentTimeMillis() - start);


        var y = 0;
    }
}
