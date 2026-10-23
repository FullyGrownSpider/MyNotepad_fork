package qtnotes.spellcheck;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.*;

public final class Compression {

    public static final int shiftMount = 96;
    public static final int charPerChar = 5;
    public static final int byteInByteArray = 4;
    public static final int clusterSize = 6;
    public static final int maxSize = 6 * 8;

    public static String path;

    public static void add(String value) {
        try {
            var searchValue = Compression.textToLetterCompressOther(value);
            var start = find(value) * byteInByteArray;
            var pathUsed = Path.of(path);
            var text = Files.readAllBytes(pathUsed);

            if (start > -1)
                return;
            var firstHalf = Arrays.copyOfRange(text, 0, -start);
            var secondHalf = Arrays.copyOfRange(text, -start, text.length);

            Files.write(pathUsed, firstHalf);
            for (var x : searchValue)
                Files.write(pathUsed, x, StandardOpenOption.APPEND);
            Files.write(pathUsed, secondHalf, StandardOpenOption.APPEND);
        } catch (IOException ignored) {

        }
    }

    public static void remove(String value) {
        try {
            var searchValue = Compression.textToLetterCompressOther(value.toLowerCase(Locale.ROOT));
            var start = find(value) * byteInByteArray;
            var pathUsed = Path.of(path);
            var text = Files.readAllBytes(pathUsed);

            if (start < 0)
                return;

            var firstHalf = Arrays.copyOfRange(text, 0, start);
            var secondHalf = Arrays.copyOfRange(text, start + searchValue.size() * byteInByteArray, text.length);

            Files.write(pathUsed, firstHalf);
            Files.write(pathUsed, secondHalf, StandardOpenOption.APPEND);
        } catch (IOException ignored) {
        }
    }

    public static int find(ArrayList<byte[]> searchValue, int top, int bottom) throws IOException {
        return searchAlgo(searchValue, top, bottom);
    }

    private static int searchAlgo(ArrayList<byte[]> searchValue, int top, int bottom) throws IOException {
        RandomAccessFile raf = new RandomAccessFile(path, "r");
        ArrayList<byte[]> bytes = new ArrayList<>(8);
        for (int i = 0; i < 8; i++) {
            bytes.add(new byte[searchValue.get(0).length]);
        }
        if (top == -1)
            top = Math.toIntExact((raf.length() - 1) / byteInByteArray);
        var bytesOut = new ArrayList<byte[]>(byteInByteArray);
        int middle = bottom;
        while (bottom <= top) {
            middle = (bottom + top) / 2;
            raf.seek((long) middle * byteInByteArray); // jump to this line in the file
            raf.read(bytes.get(0)); // read the line from the file
            bytesOut.add(bytes.get(0));

            byte mountDown = 1;
            byte counter = 1;
            while (Compression.needPreviousCluster(bytesOut.get(0))) {
                var seek = middle * byteInByteArray - (byteInByteArray * mountDown);
                raf.seek(seek);
                raf.read(bytes.get(counter));
                bytesOut.add(0, bytes.get(counter));
                mountDown++;
                counter++;
            }
            byte mountUp = getMountUp(bytesOut, middle, raf, bytes, counter);
            int comparison = compare(bytesOut, searchValue);

            bytesOut.clear();

            if (comparison < 0) {
                // line comes before searchValue
                bottom = middle + mountUp;
                middle = bottom - 1;
            } else if (comparison == 0) {
                raf.close();
                return middle;
            } else {
                // line comes after searchValue
                top = middle - mountDown;
                middle = top + 1;
            }
        }

        raf.close();
        return -middle;
    }

    public static int find(String value) throws IOException {
        var searchValue = Compression.textToLetterCompressOther(value);
        return searchAlgo(searchValue, -1, 0);
    }

    private static byte getMountUp(ArrayList<byte[]> bytesOut, int middle, RandomAccessFile raf, ArrayList<byte[]> bytes, byte counter) throws IOException {
        byte mountUp = 1;
        while (Compression.needNextCluster(bytesOut.get(bytesOut.size() - 1))) {
            var seek = middle * byteInByteArray + (byteInByteArray * mountUp);
            raf.seek(seek);
            raf.read(bytes.get(counter));
            bytesOut.add(bytes.get(counter));
            mountUp++;
            counter++;
        }
        return mountUp;
    }

    public static ArrayList<byte[]> textToLetterCompressOther(String line) {
        //should have been done before but we programming defensively now
        var list = new ArrayList<byte[]>(4);
        var myBits = new BitSet(31);
        byte counter = 0;
        byte letters = 0;
        byte totalCounter = 0;
        var listArray = line.toCharArray();
        for (int i = 0; i < listArray.length; i++) {
            totalCounter++;
            int calc = listArray[i];
            if (i != listArray.length - 1) {
                var tempCalc = indexAf(listArray, i);
                if (tempCalc != 0) {
                    calc = tempCalc;
                    i++;
                    totalCounter++;
                }
            }
            byte next = (byte) (calc - shiftMount);
            for (byte ii = 0; ii < charPerChar; ii++) {
                myBits.set(counter, ((next >> (ii)) % 2) == 1);
                counter++;
            }
            if (letters == clusterSize - 1) {
                myBits.set(30, !list.isEmpty());
                myBits.set(31, totalCounter < line.length());
                list.add(myBits.toByteArray());
                myBits = new BitSet(31);
                letters = counter = 0;
            } else
                letters++;
        }
        if (!myBits.isEmpty()) {
            myBits.set(30, !list.isEmpty());
            myBits.set(31, false);
            var listo = new byte[byteInByteArray];
            var tryList = myBits.toByteArray();
            listo[0] = tryList[0];
            listo[1] = tryList.length > 1 ? tryList[1] : 0;
            listo[2] = tryList.length > 2 ? tryList[2] : 0;
            listo[3] = tryList.length > 3 ? tryList[3] : 0;
            list.add(listo);
        }
        return list;
    }

    private static int indexAf(char[] listArray, int i) {
        return switch ("" + listArray[i] + listArray[i + 1]) {
            case "th" -> 122;
            case "he" -> 123;
            case "in" -> 124;
            case "ten" -> 125;
            case "nt" -> 126;
            case "re" -> 127;
            default -> 0;
        };
    }

    private static String indexAf(int i) {
        return switch (i) {
            case 122 -> "th";
            case 123 -> "he";
            case 124 -> "in";
            case 125 -> "ten";
            case 126 -> "nt";
            case 127 -> "re";
            default -> Character.toString(i);
        };
    }

    //without first bit
    public static String textFromLetterCompressCluster(ArrayList<byte[]> fullWord) {
        StringBuilder out = new StringBuilder();
        for (var inB : fullWord) {
            var in = BitSet.valueOf(inB);
            //get 4 bites
            for (int i = 0; i < clusterSize; i++) {
                int calc = 0;
                for (int ii = 0; ii < charPerChar; ii++) {
                    if (in.get((i * charPerChar + ii))) {
                        calc += 1 << (ii);
                    }
                }
                if (calc == 0)
                    continue;
                out.append(indexAf(calc + shiftMount));
            }
        }
        return out.toString();
    }

    public static boolean needNextCluster(byte[] in) {
        return in[3] < 0;
    }

    public static boolean needPreviousCluster(byte[] in) {
        return in[3] > 63 || (in[3] > -65 && in[3] < 0);
    }

    public static int compare(List<byte[]> line, List<byte[]> search) {
        for (byte i = 0; i < line.size() * byteInByteArray; i++) {
            if (line.get(i / byteInByteArray)[i % byteInByteArray] != search.get(i / byteInByteArray)[i % byteInByteArray])
                return line.get(i / byteInByteArray)[i % byteInByteArray] - search.get(i / byteInByteArray)[i % byteInByteArray];
        }
        return 0;
    }
}

