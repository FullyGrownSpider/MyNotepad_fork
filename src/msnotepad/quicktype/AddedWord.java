package quicktype;

import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;
public class AddedWord implements Comparable<AddedWord>{
    public String[] data;

    public AddedWord(String[] data) {
        data[0] = sortString(data[0]);

        if (data.length < 8){
            this.data = new String[8];
            for (int i = 0; i < 8; i++) {
                this.data[i] = index(i, data);
            }
        } else {
            this.data = data;
        }
    }
    private static String index(int nu, String[] things) {
        if (nu >= things.length){
            return "-";
        }
        return things[nu];
    }

    public boolean amI(String value){
        return value.equals(data[1]) ||
               value.equals(data[3]) ||
               value.equals(data[4]) ||
               value.equals(data[5]) ||
               value.equals(data[6]);
    }

    @Override
    public String toString(){
        return what(data[0]) + what(data[1]) +
                what(data[2]) +
                what(data[3]) +
                what(data[4]) +
                what(data[5]) +
                what(data[6]) +
                what(data[7]);
    }

    public String export() {
        for (int i = 1; i < data.length; i++ ) {
            if (data[i].isBlank()){
                data[i] = "-";
            }
        }
        var out = String.join(" ", data);
        return out.replaceFirst("( -)*$", "");
    }

    public static String what(String string){
        if (string.equals("-")){
            return "";
        } else {
            return " '" + string + "'";
        }
    }

    public static String sortString(String inputString)
    {
        char[] tempArray = inputString.toCharArray();
        Arrays.sort(tempArray);
        return new String(tempArray);
    }

    public String shortCut() {
        return data[0];
    }

    public String getWord() {
        return data[1];
    }
    public String getS(String base) {
        if (!base.isEmpty()){
            return base + "s";
        }
        return getEm(data[2],"s");
    }
    public String getY() {
        return getEm(data[3], "y");
    }
    public String getING() {
        return getEm(data[4], "ing");
    }
    public String getED() {
        return getEm(data[5], "ed");
    }
    public String getER() {
        return getEm(data[6], "er");
    }

    private String getEm(String base, String add){
        if (isEmpty(base)){
            return data[1] + add;
        }
        return base;
    }

    private boolean isEmpty(String s){
        return s.isEmpty() || s.equals("-");
    }

    public String getNot(String chosen) {
        if (isEmpty(data[7])){
            return "not " + chosen;
        }
        if (data[7].endsWith("-")) {
            return data[7].replace("-", "") + chosen;
        }
        if (data[7].startsWith("-")) {
            return chosen + data[7].replace("-", "");
        }
        return data[7];
    }

    static boolean[] bools = {false, false, false, false, false, false};
    private static final String splita = "[ ?\\-\n\t{})(\"']";

    public static String exists(String word, Map<String, AddedWord> data){
        for (var item : data.values()){
            if (item.amI(word)){
                return item.shortCut();
            }
        }
        return "";
    }

    public static String createText(String text, Map<String, AddedWord> data) {
//she< [vh zt sz zer r bn so< unhappy for< [-cz a ln tn-.-] ()*&^%$#@!!:{}
        var split = Arrays.stream(text.split(splita)).toList();
        StringBuilder buf = new StringBuilder();
        for (String shortCut : split) {
            boolean hasDot = shortCut.endsWith(".");
            if (shortCut.contains("<") || shortCut.contains("ss")) continue;
            var unCap = shortCut.toLowerCase(Locale.ROOT);
            unCap = check("s", unCap, (byte) 0);
            unCap = check("y", unCap, (byte) 1);
            unCap = check("=", unCap, (byte) 2);
            unCap = check("[", unCap, (byte) 3);
            unCap = check(";", unCap, (byte) 4);
            unCap = check(",", unCap, (byte) 5);

            var item = data.get(AddedWord.sortString(unCap));//scr t= scribe
            String chosen;
            if (item == null) {
                if (!hasDot) {
                    continue;
                }
                unCap = unCap.substring(0, unCap.length()-1);
                item = data.get(AddedWord.sortString(unCap));
                if (item == null) continue;
                chosen = getString(item) + ".";
            } else {
                chosen = getString(item);
            }
            if (!shortCut.toLowerCase(Locale.ROOT).equals(shortCut)) {
                String s1 = chosen.substring(0, 1).toUpperCase();
                chosen = s1 + chosen.substring(1);
            }
            var matcher = Pattern.compile("(?:^|(?<="+splita+"))" + Pattern.quote(shortCut) + "(?:$|(?="+splita+"))", Pattern.MULTILINE).matcher(text);

            matcher.find();
            matcher.appendReplacement(buf, chosen.replace("\\", "\\\\"));
            var end = matcher.end();
            text = text.substring(end);
        }
        buf.append(text);
        return buf.toString().replaceAll("-(?!-)", "")
                        .replaceAll("--", "-")
                        .replaceAll("(?<!\\\\)<<", " ")
                        .replaceAll("(?<![\\\\<])<(?!<)", "")
                        .replaceAll("\\\\<", "<");
    }

    private static String getString(AddedWord item) {
        String chosen;
        if (bools[1]) {
            chosen = item.getY();
        } else if (bools[2]) {
            chosen = item.getING();
        } else if (bools[3]) {
            chosen = item.getED();
        } else if (bools[4]) {
            chosen = item.getER();
        } else if (!bools[0]){
            chosen = item.getWord();
        } else {
            chosen = "";
        }

        if (bools[5]) {
            if (chosen.isEmpty())
                chosen = item.getS(chosen);
            chosen = item.getNot(chosen);
        }
        if (bools[0]) {
            chosen = item.getS(chosen);
        }
        return chosen;
    }


    public static String check(String find, String text, byte index) {
        var newCut = text.replace(find, "");
        bools[index] = newCut.length() != text.length();
        return newCut;
    }

    @Override
    public boolean equals(Object o){
        return o instanceof AddedWord && (((AddedWord) o).shortCut()).equals(shortCut());
    }

    @Override
    public int compareTo(AddedWord o) {
        return o.shortCut().compareTo(shortCut());
    }
}