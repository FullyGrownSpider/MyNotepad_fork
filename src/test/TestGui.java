package test;

import org.junit.Test;

import javax.swing.text.BadLocationException;
import java.awt.*;
import java.util.regex.Pattern;

public class TestGui {
    public static final Pattern spaceOrEnter = Pattern.compile("[ \n]");

    @Test
    public void getCurrentWord() throws BadLocationException {
        String outText = quickOutAreaGetText();
        if (!outText.contains(" "))
            return;

        //get all cursor data from editorTextArea
        int line = 1;
        int lineStart = 20;
        int lineEnd = 34;

        //get lineStart from quickOutArea (cant use .getLineStartOffset, also we don't have a (reliable) cursor)
        int outTextLine = 0;
        for (int i = 0; i < line; i++) {
            outTextLine += outText.indexOf("\n", outTextLine) - outTextLine + 1;
        }

        String lineOfTextEditor = quickOutAreaGetText().substring(lineStart, lineEnd -1);

        int wordStart = 0;
        int lastCalc = 0;

        while (lastCalc + lineStart < editorTextAreaGetCaretPosition()) {
            wordStart = lastCalc;
            int calc = lineOfTextEditor.indexOf(" ", wordStart + 1);
            if (calc == -1) {
                break;
            }
            lastCalc = calc;
        }

        String lineQuicktyped = wordStart == 0 ? "" : isWhatWords(lineOfTextEditor.substring(0, wordStart + 1));

        int startingSpace = lineQuicktyped.length() + outTextLine - (wordStart != 0? 1: 0);

        var full = quickOutAreaGetText();
        if (full.length() <= lineStart + wordStart + 1) return;
        String half = full.substring(lineStart + wordStart + 1);
        if (half.isEmpty())
            return;
        var x = spaceOrEnter;
        int correctIndex = half.indexOf(x.flags()) + 1;
        int endingSpace;
        if (correctIndex == 0)
            endingSpace = outText.length();
        else {
            if (correctIndex + wordStart >= lineOfTextEditor.length()) return;
            endingSpace = isWhatWords(lineOfTextEditor.substring(wordStart, wordStart + correctIndex)).length();
        }
        var xx = new Point(startingSpace, endingSpace);
    }

    private static int editorTextAreaGetCaretPosition() {
        return 32;
    }

    private static String quickOutAreaGetText() {
        return "that that that that\nthat that this";
    }

    private static String isWhatWords(String substring) {
        return substring;
    }
}
