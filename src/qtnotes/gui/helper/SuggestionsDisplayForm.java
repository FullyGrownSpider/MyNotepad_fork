package qtnotes.gui.helper;

import qtnotes.actions.WordXY;
import qtnotes.gui.GUIHandler;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.ArrayList;

public class SuggestionsDisplayForm extends ADialog {

    static final String ignoreText = "'Ignore'", addToDict = " (Add to Dictionary)";
    boolean shouldLoop;
    WordXY wordXY;

    public SuggestionsDisplayForm(JFrame frame, Font font, ArrayList<String> suggestions, boolean shouldLoop, String word, WordXY wordXY) {
        super(frame, "", false);
        this.shouldLoop = shouldLoop;
        this.wordXY = wordXY;
        suggestions.add(0, word+addToDict);
        suggestions.add(1, ignoreText);
        initializeDialog(suggestions, font);
        setUndecorated(true);

        pack();
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(false);
        setVisible(true);
    }

    private void initializeDialog(ArrayList<String> suggestions, Font font) {
        JList<String> jList = new JList<>();

        jList.setFont(font);
        jList.setModel(new AbstractListModel<>() {

            @Override
            public int getSize() {
                return suggestions.size();
            }

            @Override
            public String getElementAt(int i) {
                return suggestions.get(i);
            }
        });
        this.add(new JScrollPane(jList));

        jList.setSelectedIndex(2);

        KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);
        KeyStroke stroke2 = KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0);
        rootPane.registerKeyboardAction(x ->choose(jList.getSelectedValue()), stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
        rootPane.registerKeyboardAction(x ->choose(jList.getSelectedValue()), stroke2, JComponent.WHEN_IN_FOCUSED_WINDOW);
    }
    
    public void choose(String replace){
        if (replace.equals(addToDict)){
            GUIHandler.addToDictionary();
        } else if (replace.equals(ignoreText)){
            GUIHandler.ignoreWord();
        }else
            GUIHandler.replaceMistake(replace, wordXY);
        if (shouldLoop){
            GUIHandler.doAnotherSpellcheck();
        }
        dispose();
    }
}
