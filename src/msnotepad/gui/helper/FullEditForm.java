import quicktype.AddedWord;
import quicktype.Loading;

import javax.swing.*;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;

public class FullEditForm {
    private final JFrame myFrame = new JFrame("typer inputEdit");
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
//        word.
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
            var found = myMap.stream().filter(xx -> xx.shortCut().equals(shortCut.getText())).findFirst();
            found.ifPresent(addedWord -> myMap.remove(addedWord));
            myMap.add(new AddedWord(Arrays.stream(fieldList).map(JTextComponent::getText).toList().toArray(new String[8])));
            textToThing();
            try {
                Loading.save((ArrayList<AddedWord>) myMap);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        southPanel.add(add);
        remove.setText("remove");
        remove.addActionListener(x -> {
            var found = myMap.stream().filter(xx -> xx.shortCut().equals(shortCut.getText())).findFirst();
            found.ifPresent(addedWord -> myMap.remove(addedWord));
            textToThing();
            try {
                Loading.save((ArrayList<AddedWord>) myMap);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        southPanel.add(remove);
    }

    private void textToThing() {
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
        myFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        myFrame.getContentPane().setBackground(Color.DARK_GRAY);
        myFrame.add(this.info, BorderLayout.CENTER);
    }
}

class HintTextField extends JTextField implements FocusListener {

    private final String hint;
    private boolean showingHint;

    public HintTextField(final String hint) {
        super(hint);
        this.hint = hint;
        this.showingHint = true;
        super.addFocusListener(this);
    }

    @Override
    public void focusGained(FocusEvent e) {
        if(this.getText().isEmpty()) {
            super.setText("");
            showingHint = false;
        }
    }
    @Override
    public void focusLost(FocusEvent e) {
        if(this.getText().isEmpty()) {
            super.setText(hint);
            showingHint = true;
        }
    }

    @Override
    public String getText() {
        return showingHint ? "" : super.getText();
    }
}