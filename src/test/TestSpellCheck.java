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
    String fileLocation4 = "/home/a804/Documents/MyTyper/words_bin.dat";
    String testWord = "ccesser";

    @Test
    public void removeCheck() throws IOException {
        Compression.path = fileLocation4;
        fixNextBinaryFile();
        var length = Files.readAllBytes(Path.of(fileLocation4)).length;
        Assert.assertTrue(Compression.find(testWord) != -1);
        Compression.remove(testWord);
        Assert.assertTrue(0 > Compression.find(testWord));
        Assert.assertEquals(length, Files.readAllBytes(Path.of(fileLocation4)).length + Compression.textToLetterCompressOther(testWord).size()* 4L);
        fixNextBinaryFile();
    }

    @Test
    public void addCheck() throws IOException, InterruptedException {
        Compression.path = fileLocation4;
        fixNextBinaryFile();
        var length = Files.readAllBytes(Path.of(fileLocation4)).length;
        Assert.assertTrue(0 > Compression.find(testWord + testWord));
        Compression.add(testWord + testWord);
        Assert.assertTrue(Compression.find(testWord + testWord) > -1);
        Compression.remove(testWord + testWord);
        Assert.assertEquals(length, Files.readAllBytes(Path.of(fileLocation4)).length);
    }

    @Test
    public void searchTest() throws IOException {
//        var start = System.currentTimeMillis();
        Compression.path = fileLocation4;
        var found = Compression.find("probably");
//        System.out.println(System.currentTimeMillis() - start);
        Assert.assertTrue(found > -1);
    }

    @Test
    public void searchTest2() throws IOException {
//        var start = System.currentTimeMillis();
        Compression.path = fileLocation4;
        var found = Compression.find("accreted");
//        System.out.println(System.currentTimeMillis() - start);
        Assert.assertTrue(found > -1);
    }

    @Test
    public void fixNextBinaryFile() throws IOException {
        Path path = Paths.get(fileLocation);
        Path outPath = Path.of(fileLocation4);
        List<String> lines = Files.readAllLines(path);
        lines.sort(String::compareTo);
        lines = lines.stream().filter(x -> x.length() > 3).toList();
        Files.writeString(path, String.join("\n", lines));
        var list = lines.stream()
                .filter(e -> e.length() > 3 && e.length() < Compression.maxSize)
                .map(Compression::textToLetterCompressOther)
                .filter(Objects::nonNull)
                .sorted(Compression::compare).toList();

        Files.deleteIfExists(outPath);
        Files.createFile(outPath);
        for (var in : list) {
            for (var inIn : in)
                Files.write(outPath, inIn, StandardOpenOption.APPEND);
        }
    }

    @Test
    public void readTest() throws IOException {
        var x = Files.readAllBytes(Path.of(fileLocation4));
        for (int i = 0; i < 500; i += 4) {
            var list = new byte[4];
            list[0] = x[i];
            list[1] = x[1+ i];
            list[2] = x[2+ i];
            list[3] = x[3 + i];
            System.out.println(Compression.textFromLetterCompressCluster(list) + " " + Compression.needNextCluster(list) + " " + Compression.needPreviousCluster(list));
        }
    }

    @Test
    public void readTestTwo() throws IOException {
        var arst = new BitSet();
//        arst.set(31);
        arst.set(30);
        var arf = arst.toByteArray();
        var press = Compression.textToLetterCompressOther(testWord);
        var press1 = Compression.textToLetterCompressOther("bbbbbbbbbbbbb");
        var press2 = Compression.textToLetterCompressOther("zzzzzzzzzzzzz");
        var press3 = Compression.textToLetterCompressOther("aahing");

        var dePress = Compression.textFromLetterCompressCluster(press.get(0));
        var goLeft = Compression.needPreviousCluster(press.get(1));
        var goRight = Compression.needNextCluster(press.get(1));
        BitSet readBits;
        var z = 0;
    }

    @Test
    public void checkCompression() throws IOException {
        Compression.path = fileLocation4;
        Path outPath = Path.of(fileLocation);
        var lines = Files.readAllLines(outPath);
        List<String> fail = new ArrayList<>(5000);
        for (var word : lines) {
            if (word.length() < 4 || word.length() > 28 || Compression.find(word) > -1) continue;
            fail.add(word);
        }

        Assert.assertTrue(fail.isEmpty());
    }

    @Test
    public void createSuggestions() throws InterruptedException {
        Compression.path = fileLocation4;
        var start = System.currentTimeMillis();

        var x = Spellcheck.optimizedSearch(testWord);
        System.out.println(System.currentTimeMillis() - start);

        var y = 0;
    }
}
