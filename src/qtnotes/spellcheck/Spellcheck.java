package qtnotes.spellcheck;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public final class Spellcheck {

    private static final int maxStepsDefault = 7;
    private static final ArrayList<String> nono = new ArrayList<>(List.of("bk","bq","bx","cb","cf","cg","cj","cp","cv","cw","cx","dx","fk","fq","fv","fx","fz","gq","gv","gx","hk","hv","hx","hz","iy","jb","jc","jd","jf","jg","jh","jk","jl","jm","jn","jp","jq","jr","js","jt","jv","jw","jx","jy","jz","kq","kv","kx","kz","lq","lx","mg","mj","mq","mx","mz","pq","pv","px","qb","qc","qd","qe","qf","qg","qh","qj","qk","ql","qm","qn","qo","qp","qr","qs","qt","qv","qw","qx","qy","qz","sx","sz","tq","tx","vb","vc","vd","vf","vg","vh","vj","vk","vm","vn","vp","vq","vt","vw","vx","vz","wq","wv","wx","wz","xb","xg","xj","xk","xv","xz","yq","yv","yz","zb","zc","zg","zh","zj","zn","zq","zr","zs","zx"));

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

        defaultMistakes(word, suggestionsList, false);
        suggestionsList = suggestionsList.stream().distinct().collect(Collectors.toList());
        int max = suggestionsList.size();
        for (int i = 0; i < max; i++) {
            defaultMistakes(suggestionsList.get(i), suggestionsList, true);
        }
        suggestionsList = suggestionsList.stream().filter(x -> {
            for (var letters : nono){
                if (x.contains(letters)) return false;
            }
            return true;
        }).distinct().collect(Collectors.toList());
        if (suggestionsList.isEmpty())
            suggestionsList.add(word);
        alphabetMe(suggestionsList);
        return new ArrayList<>(suggestionsList.stream().distinct().toList());
    }

    private static void alphabetMe(List<String> suggestionsList){
        int max = suggestionsList.size();
        for (int iii = 0; iii < max; iii++) {
            var word = suggestionsList.get(iii);
            int length = word.length();
            for (int i = 0; i< length + 1; i++){
                String removed;
                if (i == 0 || i == length){
                    removed = null;
                } else {
                    //remove a character from the word
                    removed = new StringBuilder(word).replace(i, i + 1, "").toString();
                    suggestionsList.add(removed);
                    //add things like 'qu' and 'ch' to the word
                    normalLetterCombos(suggestionsList, removed, i);
                }
                normalLetterCombos(suggestionsList, word, i);
                for (int ii = 0; ii < 26; ii++) {
                    if (ii == 16) continue;
                    char charBoy = (char)(((byte) 'a') + ii);
                    //add the char into the location
                    suggestionsList.add(new StringBuilder(word).insert(i, charBoy).toString());
                    if (ii < 22) { //skip v w x y z
                        //skip a e i o u
                        if (!(ii == 4 || ii == 8 || ii == 20 || ii == 14 || ii == 0)) {
                            //add two letters in the spot
                            letterDoubleAdd(word, i, ii, suggestionsList, 'a');
                            letterDoubleAdd(word, i, ii, suggestionsList, 'e');
                            letterDoubleAdd(word, i, ii, suggestionsList, 'i');
                            letterDoubleAdd(word, i, ii, suggestionsList, 'o');
                            letterDoubleAdd(word, i, ii, suggestionsList, 'u');
                            if (removed != null) {
                                letterDoubleAdd(removed, i, ii, suggestionsList, 'a');
                                letterDoubleAdd(removed, i, ii, suggestionsList, 'e');
                                letterDoubleAdd(removed, i, ii, suggestionsList, 'i');
                                letterDoubleAdd(removed, i, ii, suggestionsList, 'o');
                                letterDoubleAdd(removed, i, ii, suggestionsList, 'u');
                            }
                        }
                        if (removed != null) {
                            if (ii != 2 && ii != 9) // skip c j
                                for (int io = 0; io < length; io++) {
                                    //while a character is removed add the char into the location
                                    suggestionsList.add(new StringBuilder(removed).insert(io, charBoy).toString());
                                }
                        }
                    }
                }
            }
        }
    }

    private static void normalLetterCombos(List<String> suggestionsList, String removed, int i) {
        letterDoubleAddOneWay(removed, i, suggestionsList, "qu");
        letterDoubleAddOneWay(removed, i, suggestionsList, "ch");
        letterDoubleAddOneWay(removed, i, suggestionsList, "gh");
        letterDoubleAddOneWay(removed, i, suggestionsList, "th");
    }

    private static void letterDoubleAddOneWay(String word, int index, List<String> suggestionList, String letters){
        suggestionList.add(new StringBuilder(word).insert(index,letters).toString());
    }

    private static void letterDoubleAdd(String word, int index, int letter, List<String> suggestionList, char actualLetter){
        var text = "" + (char)(((byte) 'a') + letter) + actualLetter;
        var text2 = "" + actualLetter + (char)(((byte) 'a') + letter);
        suggestionList.add(new StringBuilder(word).insert(index,text).toString());
        suggestionList.add(new StringBuilder(word).insert(index,text2).toString());
    }

    private static void defaultMistakes(String word, List<String> suggestionsList, boolean advanced) {
        suggestionsList.addAll(suggest(word, "f", "v"));
        suggestionsList.addAll(suggest(word, "s", "c"));
        suggestionsList.addAll(suggest(word, "k", "c"));

        if (advanced) {
            suggestionsList.addAll(suggest(word, "e", "a"));
            suggestionsList.addAll(suggest(word, "i", "y"));
            suggestionsList.addAll(suggest(word, "i", "e"));
            suggestionsList.addAll(suggest(word, "ie", "y"));
        }
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
        if (suggestionsList.size() > maxStepsDefault)
            suggestionsList.clear();
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

            //add words that are near where it should be, but only those about the same length
            if (actualSugs.isEmpty())
                actualSugs.addAll(Compression.read(word).stream().filter((x) -> x.length() > word.length() - 4 && x.length() < word.length() + 4 && x.charAt(0) == word.charAt(0)).sorted().toList());

            actualSugs.sort((a, b) -> weirdCompare(word, a, b));

            return actualSugs.stream().distinct().toList();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static int weirdCompare(String word, String a, String b){
        //step 1 check the length (the closer it is to the word length the better, otherwise bigger is better
        int calc = (Math.abs(word.length() - a.length()) - Math.abs(word.length() - b.length())) * 10;
        //step 2 do the weird checking of letters that are also in there
        var wa = new StringBuilder(a);
        var wb = new StringBuilder(b);
        for (int i = 0; i < word.length(); i++) {
            var chari = "" + word.charAt(i);
            var indexB = wb.indexOf(chari);
            var indexA = wa.indexOf(chari);
            if (indexB != -1) {
                wb.deleteCharAt(indexB);
                if (indexA == -1) {
                    calc += 100;
                }
            } if (indexA != -1) {
                wa.deleteCharAt(indexA);
                if (indexB == -1) {
                    calc -= 100;
                }
            }
        }
        //step 3 check last and first letter
        calc += a.charAt(0) != word.charAt(0) ? 500 : -500;
        calc += b.charAt(0) != word.charAt(0) ? -500 : 500;
        calc += a.charAt(a.length()-1) != word.charAt(word.length()-1) ? 500 : -500;
        calc += b.charAt(b.length()-1) != word.charAt(word.length()-1) ? -500 : 500;
        return calc;
    }
}
