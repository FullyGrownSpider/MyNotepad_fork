package qtnotes.spellcheck;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.BitSet;

public final class Compression {

    public static final int shiftMount = 96;
    public static final int charPerChar = 5;
    public static final int charPerCharCheck = 15;
    public static final int lineSize = 28;

    public static String path;

    public static void add(String value) {
        try {
            var searchValue = Compression.textToLetterCompress(value).toByteArray();
            var start = find(value);
            var pathUsed = Path.of(path);
            var text = Files.readAllBytes(pathUsed);

            if (start > -1)
                return;
            var firstHalf = Arrays.copyOfRange(text, 0, -start);
            var secondHalf = Arrays.copyOfRange(text, -start, text.length);

            Files.write(pathUsed, firstHalf);
            Files.write(pathUsed, searchValue, StandardOpenOption.APPEND);
            Files.write(pathUsed, secondHalf, StandardOpenOption.APPEND);
        } catch (IOException ignored) {

        }
    }

    public static void remove(String value) {
        try {
            var searchValue = Compression.textToLetterCompress(value).toByteArray();
            var start = find(value);
            var pathUsed = Path.of(path);
            var text = Files.readAllBytes(pathUsed);

            if (start < 0)
                return;

            var firstHalf = Arrays.copyOfRange(text, 0, start);
            var secondHalf = Arrays.copyOfRange(text, start + searchValue.length, text.length);

            Files.write(pathUsed, firstHalf);
            Files.write(pathUsed, secondHalf, StandardOpenOption.APPEND);
        } catch (IOException ignored) {

        }
    }

    public static int find(String value) throws IOException {
        var searchValue = Compression.textToLetterCompress(value);
        // open the file for reading
        RandomAccessFile raf = new RandomAccessFile(path, "r");
        var byteSizeLine = searchValue.toByteArray().length;
        int numberOfLines = Math.toIntExact(raf.length() / byteSizeLine);

        // perform the binary search...
        byte[] lineBuffer = new byte[byteSizeLine];
        int bottom = 0;
        int top = numberOfLines;
        int middle = 0;
        while (bottom <= top) {
            middle = (bottom + top) / 2;
            raf.seek((long) middle * byteSizeLine); // jump to this line in the file
            raf.read(lineBuffer); // read the line from the file
            var line = BitSet.valueOf(lineBuffer); // convert the line to a String

            int comparison = compare(line, searchValue);
//            System.out.println(Compression.textFromLetterCompress(line) + "->" + comparison);
            if (comparison == 0) {
                raf.close();
                return middle * byteSizeLine;
            } else if (comparison < 0) {
                // line comes before searchValue
                bottom = middle + 1;
            } else {
                // line comes after searchValue
                top = middle - 1;
            }
        }

        raf.close();
        return -middle * byteSizeLine;
    }

    public static BitSet textToLetterCompress(String line) {
        if (line.length() < 3 || line.length() > lineSize)
            return new BitSet();
        var myBits = new BitSet(lineSize);
        for (int i = 0; i < lineSize; i++) {
            if (i >= line.length()) {
                myBits.set(i * charPerChar, lineSize * charPerChar, true);
                break;
            }
            int next = line.charAt(i) - shiftMount;

            for (int ii = 0; ii < charPerChar; ii++) {
                myBits.set(i * charPerChar + charPerChar - 1 - ii, ((next >> (ii)) % 2) == 1);
            }

        }
        return myBits;
    }

    public static String textFromLetterCompress(BitSet in) {
        StringBuilder out = new StringBuilder();
        //get 4 bites
        for (int i = 0; i < lineSize; i++) {
            int calc = 0;
            for (int ii = 0; ii < charPerChar; ii++) {
                if (in.get(i * charPerChar + ii)) {
                    calc += 1 << (charPerChar - 1 - ii);
                }
            }
            if (calc == 31)
                return out.toString();
            out.append(Character.toString(calc + shiftMount));
        }
        return out.toString();
    }

    static int compare(BitSet b1, BitSet b2) {
        int N = b1.length();
        for (int i = 0; i < N; i++) {
            if (i % charPerChar == 0 && i > charPerCharCheck) {
                var b1T = b1.get(i, i + charPerChar);
                var b2T = b2.get(i, i + charPerChar);
                if (b1T.cardinality() == charPerChar){
                    return b2T.cardinality() == charPerChar ? 0 : -1;
                } else if (b2T.cardinality() == charPerChar){
                    return 1;
                }
            }
            if (b1.get(i)) {
                if (!b2.get(i)) {
                    return 1;
                }
            } else if (b2.get(i)) {
                return -1;
            }
        }
        return 0;
    }
}

