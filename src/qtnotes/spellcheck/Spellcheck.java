package qtnotes.spellcheck;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public final class Spellcheck {

    public static boolean isIncorrectlySpelled(String word) {
        try {
            return Compression.find(word) <= -1;
        } catch (IOException e) {
            return true;
        }
    }

    private static ArrayList<String> suggestions(String word){
        //replaces f and v, s and z, and such
        List<String> suggestionsList = new ArrayList<>(66000);

        suggestionsList.add(word);
        if (word.endsWith("ll")){
            suggestionsList.add(word.substring(0,word.length()-1));
        } else if (word.endsWith("l")){
            suggestionsList.add(word += "l");
        }

        int max = suggestionsList.size();
        defaultMistakes(word, suggestionsList);
        suggestionsList = suggestionsList.stream().distinct().collect(Collectors.toList());
        for (int i = 0; i < max; i++) {
            defaultMistakes(suggestionsList.get(i), suggestionsList);
        }
        suggestionsList = suggestionsList.stream().distinct().collect(Collectors.toList());

        alphabetMe(suggestionsList);
        return new ArrayList<>(suggestionsList.stream().distinct().toList());
    }

    private static void alphabetMe(List<String> suggestionsList){
        int max = suggestionsList.size();
        for (int iii = 0; iii < max; iii++) {
            var word = suggestionsList.get(iii);
            int length = word.length();
            for (int i = 0; i< length + 1; i++){
                StringBuilder removed;
                if (i == 0 || i == length){
                    removed = new StringBuilder(word);
                } else
                    removed = new StringBuilder(word).replace(i, i + 1, "");
                suggestionsList.add(removed.toString());
                for (int ii = 0; ii < 25; ii++) {
                    suggestionsList.add(new StringBuilder(word).insert(i, Character.toChars(((int)'a') + ii)).toString());
                    suggestionsList.add(new StringBuilder(removed).insert(i, Character.toChars(((int)'a') + ii)).toString());
                }

                for (int ii = 1; ii < 25; ii++) {
                    if (ii == 4 || ii == 8|| ii == 20 || ii == 14 || ii == 17)
                        continue;
                    letterDoubleAdd(word, i, ii, suggestionsList, 'a');
                    letterDoubleAdd(word, i, ii, suggestionsList, 'e');
                    letterDoubleAdd(word, i, ii, suggestionsList, 'i');
                    letterDoubleAdd(word, i, ii, suggestionsList, 'o');
                    letterDoubleAdd(word, i, ii, suggestionsList, 'u');
                    if (ii != 7)
                        letterDoubleAdd(word, i, ii, suggestionsList, 'h');
                }

                letterDoubleAdd(word, i, 17, suggestionsList, 'u');
            }
        }
    }

    private static void letterDoubleAdd(String word, int index, int letter, List<String> list, char actualLetter){
        //pro"ba"bly
        var buf = new StringBuilder(word).insert(index,actualLetter).insert(index, Character.toChars(((int)'a') + letter));
        var temp = buf.toString();
        list.add(temp);
        if (index != word.length() -1) {
            //pro"ba"ly
            var otherTemp = new StringBuilder(temp).replace(index + 2, index + 3, "");
            list.add(otherTemp.toString());
            //pr"ba"ly
            list.add(otherTemp.replace(index, index + 1, "").toString());
        }
        //pr"ba"bly
        list.add(buf.replace(index, index+1, "").toString());
    }

    private static void defaultMistakes(String word, List<String> suggestionsList) {
        suggestionsList.addAll(suggest(word, "f", "v"));
        suggestionsList.addAll(suggest(word, "s", "c"));
        suggestionsList.addAll(suggest(word, "k", "c"));
        //common mistakes - e could be i or a
        suggestionsList.addAll(suggest(word, "e", "a"));
        suggestionsList.addAll(suggest(word, "e", "y"));
        suggestionsList.addAll(suggest(word, "i", "y"));
        suggestionsList.addAll(suggest(word, "i", "e"));
        suggestionsList.addAll(suggest(word, "ie", "y"));
        suggestionsList.addAll(suggest(word, "gh", "f"));
    }

    private static List<String> suggest(String base, String one, String two){
        List<String> suggestionsList = new ArrayList<>();

        var last = base;
        while (last.contains(one)){
            last = last.replaceFirst(one, two);
            suggestionsList.add(last);
        }
        while (last.contains(two)){
            last = last.replaceFirst(two, one);
            suggestionsList.add(last);
        }
        last = last.replaceFirst(one, two);
        last = last.replaceFirst(one, two);
        suggestionsList.add(last);
        last = last.replaceFirst(one, two);
        last = last.replaceFirst(two, one);
        last = last.replaceFirst(two, one);
        suggestionsList.add(last);
        return suggestionsList;
    }

    public static List<String> optimizedSearch(String word){
        try {
            List<String> testList = Spellcheck.suggestions(word);
            List<SearchThread> runs = new ArrayList<>();
            List<Thread> threads = new ArrayList<>();
            int half = testList.size() / 16;
            int i;
            for (i = half; i < testList.size(); i+= half) {
                var firstFinder = new SearchThread(testList.subList(i - half, i));
                var first = new Thread(firstFinder);
                threads.add(first);
                runs.add(firstFinder);
                first.start();

            }
            var firstFinder = new SearchThread(testList.subList(i-half, testList.size()));
            var first = new Thread(firstFinder);
            runs.add(firstFinder);
            first.start();

            first.join();
            var actualSugs = new ArrayList<>(firstFinder.getValue());

            for (i = 0; i < threads.size(); i++) {
                threads.get(i).join();
                actualSugs.addAll(runs.get(i).getValue());
            }

            actualSugs.sort((a, b) -> a.charAt(1) != b.charAt(1) ? word.charAt(1) == a.charAt(1) ? -1 : 1 : 0);

            actualSugs.sort((a, b) -> - a.length() + b.length() + (word.length() / 2));

            actualSugs.addAll(Compression.read(word).stream().filter((x) -> x.length() > word.length() - 4 && x.length() < word.length() + 4).sorted().toList());

            actualSugs.sort((a, b) -> weirdCompare(word, a, b));

            return actualSugs.stream().distinct().toList();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    private static int weirdCompare(String word, String a, String b){
        int calc = 0;
        for (int i = 0; i < word.length(); i++) {
            var chari = word.charAt(i);
            var lengthy = word.split(String.valueOf(chari)).length;
            var lengthyA = a.split(String.valueOf(chari)).length;
            lengthyA = Math.max(lengthy - lengthyA, 0);
            var lengthyB = b.split(String.valueOf(chari)).length;
            lengthyB = Math.max(lengthy - lengthyB, 0);
            calc += (lengthyA - lengthyB) * (word.length() - i);
        }
        return calc;
    }
}
