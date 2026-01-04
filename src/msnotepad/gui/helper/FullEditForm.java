import quicktype.AddedWord;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;

public class FullEditForm {
    public final JFrame myFrame = new JFrame("typer inputEdit");
    private static final Font usedFont = new Font("Ariel", Font.BOLD, 14);
    private final JTextField
            shortCut = new HintTextField("Quicktype"),
            word = new HintTextField("Word"),
            wordS = new HintTextField("S"),
            wordY = new HintTextField("Y"),
            wordING = new HintTextField("Ing"),
            wordED = new HintTextField("Ed"),
            wordER = new HintTextField("Er"),
            wordNOT = new HintTextField("Not");
    private final JTextField[] fieldList = new JTextField[]{shortCut, word, wordS, wordY, wordING, wordED, wordER, wordNOT};
    private final JTextArea info = new JTextArea();
    private final JButton add = new JButton(), remove = new JButton();
    private final JPanel northPanel = new JPanel(), southPanel = new JPanel();
    List<AddedWord> myMap;

    public FullEditForm(List<AddedWord> myMap) {
        this.myMap = myMap;
        makeFrame();
        northPanelMake();
        southPanelMake();
        showDoubles();
        shortCutType();
        wordType();
        redo();
    }

    private void wordType() {
        word.addActionListener(evt -> {
            Optional<AddedWord> first = myMap.stream().filter(
                            x -> x.getWord().equals(
                                    word.getText().trim()))
                .findFirst();
            if (first.isPresent()) {
                for (int i = 0; i < fieldList.length; i++)
                    fieldList[i].setText(first.get().data[i]);
            } else {
                for (int i = 0; i < fieldList.length; i++) {
                    if (i == 1) continue;
                    fieldList[i].setText("");
                }
                showDoubles();
            }
        });
        word.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                if ((word.getText() + e.getKeyChar()).trim().isEmpty()) {
                    showDoubles();
                    return;
                }
                var list = myMap.stream().filter(
                                x -> x.getWord().contains(
                                        ((word.getText() + e.getKeyChar()).trim())))
                        .map(AddedWord::export)
                        .toList();
                info.setText(String.join("\n", list));
            }
        });
    }

    private void shortCutType() {
        shortCut.addActionListener(evt -> {
            Optional<AddedWord> first = myMap.stream().filter(
                            x -> x.shortCut().equals(
                                    AddedWord.sortString(shortCut.getText().trim())))
                    .findFirst();
            if (first.isPresent()) {
                for (int i = 0; i < fieldList.length; i++)
                    fieldList[i].setText(first.get().data[i]);
            } else {
                for (int i = 1; i < fieldList.length; i++) {
                    fieldList[i].setText("");
                }
                showDoubles();
            }
        });
        shortCut.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                if ((shortCut.getText() + e.getKeyChar()).trim().isEmpty()) {
                    showDoubles();
                    return;
                }
                var list = myMap.stream().filter(
                                x -> x.shortCut().contains(
                                        AddedWord.sortString((shortCut.getText() + e.getKeyChar()).trim())))
                        .map(AddedWord::export)
                        .toList();
                info.setText(String.join("\n", list));
            }
        });
    }

    private void showDoubles() {
        var stringList = new ArrayList<>(myMap.stream().map(AddedWord::shortCut).toList());
        var copyList = stringList.stream().distinct().toList();
        for (var item : copyList) {
            stringList.remove(item);
        }
        var info = String.join("\n", stringList);
        if (!info.isEmpty()) {
            info = "DOUBLES\n" + info;
        }
        this.info.setText(info);
        this.info.setFont(usedFont);
    }

    private void redo() {
        myFrame.repaint();
        myFrame.setVisible(true);
    }

    private void northPanelMake() {
        for (var item : fieldList) {
            item.setColumns(10);
            northPanel.add(item);
        }
    }

    private void southPanelMake() {
        add.setText("add/update");
        add.addActionListener(x -> {
            GUIHandler.getQuicktype().addWord(new AddedWord(Arrays.stream(fieldList).map(xo -> xo.getText().replaceAll(" ", "<<")).toList().toArray(new String[8])));
            textToThing();
        });
        southPanel.add(add);
        remove.setText("remove");
        remove.addActionListener(x -> {
            GUIHandler.getQuicktype().removeWord(shortCut.getText());
            textToThing();
        });
        southPanel.add(remove);
        add.setFocusable(false);remove.setFocusable(false);info.setFocusable(false);
    }

    private void textToThing() {
        myMap = GUIHandler.getQuicktype().getWords();
        var list = myMap.stream().filter(
                        xx -> xx.shortCut().contains(
                                AddedWord.sortString((shortCut.getText()).trim())))
                .map(AddedWord::export)
                .toList();
        info.setText(String.join("\n", list));
    }

    private void makeFrame() {
        myFrame.setSize(1230, 430);
        myFrame.setLayout(new BorderLayout());
        myFrame.add(northPanel, BorderLayout.NORTH);
        myFrame.add(southPanel, BorderLayout.SOUTH);
        myFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        myFrame.getContentPane().setBackground(Color.DARK_GRAY);
        myFrame.add(this.info, BorderLayout.CENTER);

        KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
        myFrame.getRootPane().registerKeyboardAction(x -> myFrame.dispose(), stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);

        KeyStroke strokeAdd = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, InputEvent.SHIFT_DOWN_MASK);
        myFrame.getRootPane().registerKeyboardAction(x -> {
            GUIHandler.getQuicktype().addWord(new AddedWord(Arrays.stream(fieldList).map(xo -> xo.getText().replaceAll(" ", "<<")).toList().toArray(new String[8])));
            textToThing();
            for (JTextField jTextField : fieldList) {
                jTextField.setText("");
            }
            fieldList[0].grabFocus();
            }, strokeAdd, JComponent.WHEN_IN_FOCUSED_WINDOW);
    }
}

class HintTextField extends JTextField implements FocusListener {

    private final String hint;

    public HintTextField(final String hint) {
        super(hint);
        this.hint = hint;
        super.addFocusListener(this);
    }

    @Override
    public void focusGained(FocusEvent e) {
        if(this.getText().isEmpty() || this.getText().equals("-") || this.getText().equals(hint)) {
            super.setText("");
        }
    }
    @Override
    public void focusLost(FocusEvent e) {
        if(this.getText().isEmpty() || this.getText().equals("-")) {
            super.setText(hint);
        }
    }
    @Override
    public String getText(){
        var what = super.getText();
        if (what.equals(hint))
            return "";
        return super.getText();
    }
}