package qtnotes.gui;

import qtnotes.gui.helper.ADialog;
import qtnotes.init.InitialValues;
import qtnotes.quicktype.AddedWord;
import qtnotes.quicktype.Quicktype;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.IOException;

public class OverlayMode extends ADialog {

    private final JTextField outTextField;
    private final JTextField textField;
    private static final Quicktype quicktype = new Quicktype();

    public OverlayMode(Frame owner, String title, boolean modal) {
        super(owner, title, modal);
        textField = createATextField();
        textField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                updateOutArea();
                if (e.getKeyCode() == KeyEvent.VK_ENTER){
                    ProcessBuilder builder = new ProcessBuilder("wl-copy", outTextField.getText());
                    try {
                        builder.start();
                    } catch (IOException ignored) {
                    }
                    System.exit(0);
                }
            }
        });
        InitialValues.readFromFile();
        outTextField = createATextField();
        outTextField.setFocusable(false);

        this.setLayout(new BorderLayout());
        this.add(textField, BorderLayout.NORTH);
        this.add(outTextField, BorderLayout.SOUTH);

        setUndecorated(true);

        pack();
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(false);
        setVisible(true);

        textField.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
            }

            @Override
            public void focusLost(FocusEvent e) {
                ProcessBuilder builder = new ProcessBuilder("wl-copy", outTextField.getText());
                try {
                    builder.start();
                } catch (IOException ignored) {
                }
                System.exit(0);
            }
        });
    }

    private void updateOutArea() {
        outTextField.setText(AddedWord.createText(textField.getText(), quicktype.data, true, true));
    }

    private static JTextField createATextField() {
        var theField = new JTextField();
        theField.setColumns(70);
        theField.setBackground(Color.darkGray);
        theField.setForeground(Color.white);
        theField.setCaretColor(Color.white);
        theField.setFont(InitialValues.getEditorFont());
        return theField;
    }
}
