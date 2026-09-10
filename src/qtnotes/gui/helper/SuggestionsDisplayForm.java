package qtnotes.gui.helper;

import qtnotes.gui.GUIHandler;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.ArrayList;

public class SuggestionsDisplayForm extends ADialog {

    /**
     * FindDialog constructor help to specify the parent component and mobality
     * of the FindDialog.
     *
     * @param frame    the parent component.
     */
    public SuggestionsDisplayForm(JFrame frame, Font font, ArrayList<String> suggestions) {
        super(frame, "", false);
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
        this.add(jList);

        jList.setSelectedIndex(0);
        KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);
        KeyStroke stroke2 = KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0);
        rootPane.registerKeyboardAction(x ->choose(jList.getSelectedValue()), stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
        rootPane.registerKeyboardAction(x ->choose(jList.getSelectedValue()), stroke2, JComponent.WHEN_IN_FOCUSED_WINDOW);
    }
    
    public void choose(String replace){
        GUIHandler.replaceMistake(replace);
        dispose();
    }

}
