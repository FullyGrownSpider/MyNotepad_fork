package qtnotes.gui.helper;

import qtnotes.gui.GUIHandler;
import qtnotes.spellcheck.Compression;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;

public class SpellCheckRemove extends ADialog{

    public SpellCheckRemove(JFrame frame, Font font) {
        super(frame, "", false);
        initializeDialog(font);
        setUndecorated(true);

        pack();
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(false);
        setVisible(true);
    }

    private void initializeDialog(Font font) {
        var jList = new JTextField();

        jList.setFont(font);
        this.add(new JScrollPane(jList));
        jList.setText("                                              ");
        jList.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                jList.setText("");
            }

            @Override
            public void focusLost(FocusEvent e) {
                dispose();
            }
        });
        KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);
        rootPane.registerKeyboardAction(x ->choose(jList.getText()), stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
    }

    public void choose(String remove){
        Compression.remove(remove);
        GUIHandler.recheckSpelling();
        dispose();
    }
}
