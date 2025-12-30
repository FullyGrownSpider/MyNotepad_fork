package quicktype;

import java.util.Arrays;

public class AddedWord {
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

    public static String what (String string){
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
    public String getS() {
        return getEm(data[2], "s");
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
}