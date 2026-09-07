package qtnotes.spellcheck;

import java.io.IOException;
import java.util.*;

public final class Spellcheck {

    public static boolean isCorrectlySpelled(String word) {
        try {
            return Compression.find(word) > -1;
        } catch (IOException e) {
            return false;
        }
    }

    public static List<String> suggestions(String word){
        //TODO sort based on size (same letters length first)
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
        for (int i = 0; i < max; i++) {
            defaultMistakes(suggestionsList.get(i), suggestionsList);
        }

        alphabetMe(suggestionsList);
        return suggestionsList.stream().distinct().filter((x) -> x.length() < 30).toList();
    }

    private static void alphabetMe(List<String> suggestionsList){
        int max = suggestionsList.size();
        for (int iii = 0; iii < max; iii++) {
            var word = suggestionsList.get(iii);
            int length = word.length();
            for (int i = 1; i< length; i++){
                var removed = new StringBuilder(word).replace(i, i + 1, "");
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

            var actualSugs = new ArrayList<>(firstFinder.getValue());
            for (i = 0; i < threads.size(); i++) {
                threads.get(i).join();
                actualSugs.addAll(runs.get(i).getValue());
            }

            return actualSugs;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
