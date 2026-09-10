package qtnotes.gui;/*
 * Copyright (c) 2021 Mohit Saini, Under MIT License. Use is subject to license terms.
 *
 */

import qtnotes.actions.*;
import qtnotes.gui.helper.OptionPane;
import qtnotes.gui.helper.StatusBar;
import qtnotes.gui.helper.StrangeKeyAdapter;
import qtnotes.init.InitialValues;
import qtnotes.quicktype.AddedWord;
import qtnotes.quicktype.Quicktype;
import qtnotes.spellcheck.Spellcheck;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.Timer;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;

/**
 * qtnotes.gui.GUIHandler class handle the main gui functioning of the MSNotepad, this is the point of
 * distribution of work to other classes.
 */
public class GUIHandler {
    private static JFrame frame;
    private static JScrollPane editorScrollPane, editorScrollPaneOutArea;
    private static JPanel mainPanel;
    private static JTextArea editorTextArea;

    private static JTextPane qtOutArea;
    private static final Timer autoSave = new Timer();
    private static StatusBar statusBar;
    private static final AtomicBoolean isSaved = new AtomicBoolean(true);
    private static final AtomicBoolean isLoadingFile = new AtomicBoolean(false);
    private static final Quicktype quicktype = new Quicktype();

    private static JMenuItem saveAsFile;
    public static JCheckBoxMenuItem statusBarView;

    private static Style _defaultStyle;
    private static Style _incorrectStyle;
    private static Style _selectedStyle;
    private static final List<String> ignored = new ArrayList<>();
    private static final List<WordXY> incorrectItems = new ArrayList<>();
    private static final WordXY emptyPoint = new WordXY(-1, -1);
    
    public static byte saveCount = 0;

    public static void replaceMistake(String replace) {
        var wordXY = incorrectItems.remove(0);
        editorTextArea.select(wordXY.x, wordXY.x + wordXY.y);
        getEditorTextArea().replaceSelection(replace);
        postQTClean();
    }

    /**
     * handle method is help to setup the major components and getting the frame ready to
     * make it visible on the user screen.
     */
    public void handle() {
        frame = new JFrame() {
            @Override
            public void setTitle(String fileName) {
                fileName = fileName + " - ";
                String unSavedMark = isSaved.get() ? "" : "*";
                super.setTitle(unSavedMark + fileName + "MyNotepad");
            }
        };
        InitialValues.readFromFile();
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                //store where you have location
                InitialValues.setFrameHeight(GUIHandler.getFrame().getHeight());
                InitialValues.setFrameWidth(GUIHandler.getFrame().getWidth());
                InitialValues.setFrameX(GUIHandler.getFrame().getX());
                InitialValues.setFrameY(GUIHandler.getFrame().getY());
                //file not saved
                if (GUIHandler.getNotSaved()) {

                    if (InitialValues.getFilePath() != null) {
                        FileMenuActions.saveFile();
                    } else {
                        int value = OptionPane.showOptionPane();
                        if (value == 1) {
                            GUIHandler.getSaveAsMenuItem().doClick();
                        }
                        if (value == 0) {
                            return;
                        }
                    }
                }
                InitialValues.writeToFile();
            }
        });
        frame.setSize(InitialValues.getFrameWidth(), InitialValues.getFrameHeight());

        frame.setLocation(InitialValues.getFrameX(), InitialValues.getFrameY());

        frame.setTitle(InitialValues.getFileName());
        mainPanel = (JPanel) frame.getContentPane();

        initialiseMenu();
        initialiseScrollPane();

        statusBar = new StatusBar();
        mainPanel.setLayout(new BorderLayout());
        var splitPanel =
                new JSplitPane(JSplitPane.VERTICAL_SPLIT, editorScrollPane, editorScrollPaneOutArea);
        splitPanel.setResizeWeight(.5);
        mainPanel.add(splitPanel, BorderLayout.CENTER);
        mainPanel.add(statusBar, BorderLayout.SOUTH);

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);

        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                saveCount--;
                if (saveCount > 0)//this should probably be an option?
                    return;
                if (saveCount < -2) {
                    saveCount = -5;
                    return;
                }
                if (InitialValues.getFilePath() != null && getNotSaved()) {
                    FileMenuActions.saveFile();
                }
            }
        };
        //try to save every two seconds
        autoSave.scheduleAtFixedRate(task, 0, 5000);

        findNSetPositionIndicator();
    }

    /**
     * initialiseScrollPane method is help to setup the text editor of the MSNotepad.
     */
    private void initialiseScrollPane() {

        StyleContext styleContext = new StyleContext();
        _defaultStyle = styleContext.getStyle(StyleContext.DEFAULT_STYLE);
        _selectedStyle = styleContext.addStyle("ConstantWidth", null);
        StyleConstants.setForeground(_selectedStyle, Color.lightGray);
        StyleConstants.setBold(_selectedStyle, true);
        _incorrectStyle = styleContext.addStyle("ConstantWidth", _selectedStyle);
        StyleConstants.setForeground(_incorrectStyle, new Color(0xf00000));

        editorTextArea = initTextArea();
        qtOutArea = initTextPane();
        editorTextArea.addKeyListener(new StrangeKeyAdapter());
        editorScrollPane = new JScrollPane(editorTextArea);
        editorScrollPaneOutArea = new JScrollPane(qtOutArea);
        editorScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        editorScrollPaneOutArea.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        editorScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        editorScrollPaneOutArea.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        editorScrollPane.setBorder(new LineBorder(Color.WHITE, 0));
        editorScrollPaneOutArea.setBorder(new LineBorder(Color.WHITE, 0));


        if (InitialValues.getFilePath() != null) {
            loadFileToEditor();
        }

        editorTextArea.addCaretListener(e -> findNSetPositionIndicator());
        editorTextArea.setSelectionStart(editorTextArea.getText().length());
    }

    private JTextPane initTextPane() {
        var textArea = new JTextPane() {
            @Override
            public void setFont(Font font) {
                String family = font.getFamily();
                int style = font.getStyle();
                int size = font.getSize();

                int originalPix = InitialValues.getEditorFont().getSize() + 5;
                int zoomPix = (InitialValues.getZoom() * originalPix) / 100 - originalPix;

                font = new Font(family, style, size + zoomPix + 5);
                super.setFont(font);
            }
        };
        textArea.setEditable(false);
        textArea.setBackground(Color.darkGray);
        textArea.setForeground(Color.lightGray);
        textArea.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent focusEvent) {

            }

            @Override
            public void focusLost(FocusEvent focusEvent) {
                saveCount = -5;
                if (InitialValues.getFilePath() != null && getNotSaved()) {
                    FileMenuActions.saveFile();
                }
            }
        });

        Border outside = new MatteBorder(1, 0, 0, 0, mainPanel.getBackground());
        Border inside = new MatteBorder(0, 4, 0, 0, textArea.getBackground());
        textArea.setBorder(new CompoundBorder(outside, inside));

        Font font = InitialValues.getEditorFont();
        textArea.setFont(font);

        return textArea;
    }

    private JTextArea initTextArea() {
        var textArea = new JTextArea() {
            @Override
            public void setFont(Font font) {
                String family = font.getFamily();
                int style = font.getStyle();
                int size = font.getSize();

                int originalPix = InitialValues.getEditorFont().getSize() + 5;
                int zoomPix = (InitialValues.getZoom() * originalPix) / 100 - originalPix;

                font = new Font(family, style, size + zoomPix + 5);
                super.setFont(font);
            }
        };
        textArea.setBackground(Color.darkGray);
        textArea.setForeground(Color.lightGray);
        textArea.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent focusEvent) {

            }

            @Override
            public void focusLost(FocusEvent focusEvent) {
                saveCount = -5;
                if (InitialValues.getFilePath() != null && getNotSaved()) {
                    FileMenuActions.saveFile();
                }
            }
        });

        Border outside = new MatteBorder(1, 0, 0, 0, mainPanel.getBackground());
        Border inside = new MatteBorder(0, 4, 0, 0, textArea.getBackground());
        textArea.setBorder(new CompoundBorder(outside, inside));

        Font font = InitialValues.getEditorFont();
        textArea.setFont(font);
        textArea.setLineWrap(InitialValues.getWrapTheLine());
        textArea.setTabSize(4);

        textArea.getDocument().addDocumentListener(new DocumentListener() {

            @Override
            public void removeUpdate(DocumentEvent e) {
                if (!isLoadingFile.get()) {
                    setIsSaved(false);
                }
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                if (!isLoadingFile.get()) {
                    setIsSaved(false);
                }
            }

            @Override
            public void changedUpdate(DocumentEvent arg0) {
            }
        });
        return textArea;
    }

    public static void postQTClean() {
        try {
            var word = getCurrentWord();

            spellCheck();
            clearBold(qtOutArea);

            spellPaint(qtOutArea);
            boldCursorText(word);
        } catch (BadLocationException ignored) {

        }
    }

    public static void fullTextQT() {
        try {
            qtOutArea.setText("");
            replaceOutSelection(0, 0, 0,editorTextArea.getText().length());

            postQTClean();
        } catch (BadLocationException ignored) {

        }
    }

    public static void fullLineQT() {
        try {
            int line = getCurrentLine();
            var offsetOut = getOutCurrentLine(line);
            var allTextAfterOffset = qtOutArea.getText(offsetOut, qtOutArea.getText().length() - offsetOut + 1);
            var lineEndOut = allTextAfterOffset.indexOf("\n") - 1;
            if (lineEndOut == -2) {
                lineEndOut = allTextAfterOffset.length() + offsetOut;
            }
            int lineEditorStart = editorTextArea.getLineStartOffset(line);
            int lineEditorEnd = editorTextArea.getLineEndOffset(line);
            replaceOutSelection(offsetOut, offsetOut + lineEndOut + 1, lineEditorStart, lineEditorEnd - lineEditorStart);

            postQTClean();
        } catch (Exception ignored) {
        }
    }

    private static void replaceOutSelection(int offsetOut, int lineEndOut, int offsetEditor, int lengthEditor) throws BadLocationException {
        qtOutArea.select(offsetOut, lineEndOut);
        qtOutArea.setEditable(true);
        qtOutArea.replaceSelection(isWhatWords(editorTextArea.getText(offsetEditor, lengthEditor)));
        qtOutArea.setEditable(false);
    }

    /// get current selected word in the editorQuickOutArea
    private static WordXY getCurrentWord() throws BadLocationException {
        String outText = qtOutArea.getText();
        if (!outText.contains(" "))
            return emptyPoint;

        //get all cursor data from editorTextArea
        int line = getCurrentLine();
        int lineEditorStart = editorTextArea.getLineStartOffset(line);
        int lineEditorEnd = editorTextArea.getLineEndOffset(line);

        int outCurrentLine = getOutCurrentLine(line);

        String lineOfTextEditor = editorTextArea.getText(lineEditorStart, lineEditorEnd - lineEditorStart);

        String lineBeforeWordEditor = lineOfTextEditor.substring(0, editorTextArea.getCaretPosition() - lineEditorStart);
        int wordStartEditor = lineBeforeWordEditor.lastIndexOf(' ') + 1;

        String lineQuicktypedBeforeWord = wordStartEditor == 0 ? "" : isWhatWords(lineBeforeWordEditor.substring(0, wordStartEditor));

        wordStartEditor += lineEditorStart;

        var matcher = StrangeKeyAdapter.spaceOrEnter.matcher(editorTextArea.getText(wordStartEditor, lineEditorEnd - wordStartEditor));
        int endingSpace;
        if (!matcher.find())
            endingSpace = outText.length() - (lineQuicktypedBeforeWord.length() + outCurrentLine);
        else {
            endingSpace = isWhatWords(editorTextArea.getText(wordStartEditor, matcher.end() - 1)).length();
        }
        return new WordXY(lineQuicktypedBeforeWord.length() + outCurrentLine, endingSpace);
    }

    private static int getOutCurrentLine(int line) {
        int outCurrentLine = 0;
        for (int i = 0; i < line; i++) {
            outCurrentLine = qtOutArea.getText().indexOf("\n", outCurrentLine) + 1;
        }
        return outCurrentLine;
    }

    private static void boldCursorText(WordXY currentPoint) {
        try {
            var input = GUIHandler.getEditorTextArea();

            int start = input.getSelectionStart();
            int end = input.getSelectionEnd();

            if (end != start) {
                return;
            }

            makeBold(qtOutArea, currentPoint.x, currentPoint.y);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static void clearBold(JTextPane outPut) {
        outPut.getStyledDocument().setCharacterAttributes(0, outPut.getText().length(), _defaultStyle, true);
    }

    private static void makeBold(JTextPane outPut, int startingSpace, int endingSpace) {
        outPut.getStyledDocument().setCharacterAttributes(startingSpace, endingSpace, _selectedStyle, true);
    }

    private static void spellCheck() {
        try {
            int currentLine = getCurrentLine();
            int outCurrentLineOffSet = getOutCurrentLine(currentLine);
            var allTextAfterOffset = new StringBuilder(qtOutArea.getText(outCurrentLineOffSet, qtOutArea.getText().length() - outCurrentLineOffSet));
            var wordsToCheck = allTextAfterOffset.toString().split("[ \t\n\"'.,?!]");
            Pattern pat = Pattern.compile("[^A-z]");
            var suggestible = new ArrayList<String>(8);
            var startOffset = editorTextArea.getLineStartOffset(currentLine);
            var editorText = editorTextArea.getText(editorTextArea.getLineStartOffset(currentLine), editorTextArea.getText().length() - startOffset);
            for (var word : wordsToCheck){
                if (word.length() < 3 || word.length() >= 30) continue;
                if (!pat.matcher(word).find() && !Spellcheck.isCorrectlySpelled(word) && editorText.contains(word)){
                    suggestible.add(word);
                }
            }
            incorrectItems.sort(WordXY::compareTo);
            var list = incorrectItems.stream().filter((wordXY -> wordXY.x < outCurrentLineOffSet)).toList();
            incorrectItems.clear();
            incorrectItems.addAll((list));
            String replaceNumbers = Long.MAX_VALUE + Long.toString(Long.MAX_VALUE);
            replaceNumbers = replaceNumbers.substring(0, 30);
            for (var badWord : suggestible){
                var index = allTextAfterOffset.indexOf(badWord);
                allTextAfterOffset.replace(index, badWord.length() + index, replaceNumbers.substring(0, badWord.length()));
                incorrectItems.add(new WordXY(index, badWord.length()));
            }

            //TODO
            // remove all checked words from this line
            // check all words in a line (except the one with the cursor in it

        } catch (BadLocationException e) {
            throw new RuntimeException(e);
        }
    }

    private static void addToIncorrect(WordXY what) {
        if (!incorrectItems.isEmpty()) {
            var last = incorrectItems.stream().filter((x) -> x.x == what.x).findFirst();
            if (last.orElse(emptyPoint).x == what.x) {
                incorrectItems.remove(last.orElse(emptyPoint));
            }
        }
        incorrectItems.add(what);
    }

    private static void spellPaint(JTextPane outPut) {
        for (var p : incorrectItems) {
            outPut.getStyledDocument().setCharacterAttributes(p.x, p.y, _incorrectStyle, true);
        }
    }

    private static String isWhatWords(String word) {
        return AddedWord.createText(word, quicktype.data, InitialValues.getReplaceQuote(), InitialValues.getException());
    }

    private static void setIncorrectQT(String oldText, String newText) {
        if (statusBar == null || !statusBar.isVisible()) return;

        var oldWords = Arrays.stream(oldText.split(" ")).distinct().toList();

        for (int i = Math.max(0, oldWords.size() - 2); i < oldWords.size(); i++) {
            if (oldWords.get(i).length() < 3) continue;
            if (newText.contains(oldWords.get(i))) {
                var wrong = AddedWord.exists(oldWords.get(i), quicktype.data);
                if (!wrong.isEmpty()) {
                    statusBar.setHintText(wrong);
                    return;
                }
            }
        }
        statusBar.setHintText("");
    }
    
    private static void findNSetPositionIndicator() {
        int lineNum = 1;
        int columnNum = 1;

        int caretPosition;
        try {
            caretPosition = editorTextArea.getCaretPosition();
            lineNum = editorTextArea.getLineOfOffset(caretPosition);
            columnNum = caretPosition - editorTextArea.getLineStartOffset(lineNum);

        } catch (BadLocationException ignored) {
        }
        if (statusBar != null)
            statusBar.setCaretPosition(lineNum, columnNum);
    }

    /**
     * loadFileToEditor method is help to load/reload the file,
     * whose name is saved in qtnotes.init.InitialValues class.
     */
    private void loadFileToEditor() {
        setIsLoadingFile(true);
        String path = InitialValues.getFilePath();
        File file = new File(path);
        StringBuilder fileText = new StringBuilder();
        try {
            Scanner fileReader = new Scanner(file);
            while (fileReader.hasNextLine()) {
                fileText.append(fileReader.nextLine()).append("\n");
            }
            fileReader.close();
            setIsSaved(true);
        } catch (IOException ex) {
            InitialValues.setFileName(InitialValues.NEW_FILE);
            InitialValues.setFilePath(null);
            InitialValues.writeToFile();
        }
        if (fileText.length() - 1 > -1)
            editorTextArea.setText(fileText.substring(0, fileText.length() - 1));
        try {
            editorTextArea.setCaretPosition(InitialValues.getCaretPosition());//TODO
        } catch (Exception e) {
            editorTextArea.setCaretPosition(0);
            InitialValues.setCaretPosition(0);
        }
        setIsLoadingFile(false);
        fullTextQT();
    }

    private JMenu makeMenu(String s) {
        var newMenu = new JMenu(s);
        newMenu.setForeground(Color.green);
        return newMenu;
    }

    private JMenuItem makeMenuItem(Action a) {
        var newMenu = new JMenuItem(a);
        newMenu.setForeground(Color.green);
        return newMenu;
    }

    /// init all values and create menus
    private void initialiseMenu() {
        var menuBar = new JMenuBar();
        menuBar.setBorderPainted(false);

        var fileMenu = makeMenu("File");
        var editMenu = makeMenu("Edit");
        var optionsMenu = makeMenu("Program");
        var viewMenu = makeMenu("View");

        menuBar.add(fileMenu);
        menuBar.add(editMenu);
        menuBar.add(optionsMenu);
        menuBar.add(viewMenu);
        frame.setJMenuBar(menuBar);
        JMenuItem newFile = makeMenuItem(new FileMenuActions.NewFileAction());
        JMenuItem newWindowFile = makeMenuItem(new FileMenuActions.NewWindowFileAction());
        JMenuItem openFile = makeMenuItem(new FileMenuActions.OpenFileAction());
        JMenuItem editQuicktype = makeMenuItem(new EditMenuActions.OpenQuickTypeEditAction());
        saveAsFile = makeMenuItem(new FileMenuActions.SaveAsFileAction());
        JMenuItem exportQuicktype = makeMenuItem(new FileMenuActions.exportQuickTypeAction());
        JMenuItem copyQuicktype = makeMenuItem(new FileMenuActions.copyQuickTypeAction());
        JMenuItem exitFile = makeMenuItem(new FileMenuActions.ExitFileAction());
        fileMenu.add(newFile);
        fileMenu.add(newWindowFile);
        fileMenu.addSeparator();
        fileMenu.add(openFile);
        fileMenu.add(saveAsFile);
        fileMenu.add(openFile);
        editMenu.add(editQuicktype);
        fileMenu.add(copyQuicktype);
        fileMenu.add(exportQuicktype);
        fileMenu.addSeparator();
        fileMenu.add(exitFile);

        var suggestEdit = makeMenuItem(new EditMenuActions.SuggestAction());
        editMenu.add(editQuicktype);
        editMenu.add(suggestEdit);

        JCheckBoxMenuItem replaceQuotes = makeCheckBoxMenuItem(new FormatMenuActions.shouldReplaceQuotes());
        replaceQuotes.setState(InitialValues.getReplaceQuote());
        JCheckBoxMenuItem doExceptions = makeCheckBoxMenuItem(new FormatMenuActions.shouldDoExceptions());
        doExceptions.setState(InitialValues.getException());
        JCheckBoxMenuItem wordWrapFormat = makeCheckBoxMenuItem(new FormatMenuActions.WordWrapFormatAction());
        wordWrapFormat.setState(InitialValues.getWrapTheLine());
        JMenuItem fontChangeFormat = makeMenuItem(new FormatMenuActions.FontChangeFormatAction());
        optionsMenu.add(replaceQuotes);
        optionsMenu.add(doExceptions);
        optionsMenu.add(wordWrapFormat);
        optionsMenu.add(fontChangeFormat);

        JMenu zoomView = makeMenu("Zoom");
        statusBarView = makeCheckBoxMenuItem(new ViewMenuActions.StatusBarViewAction());
        statusBarView.setState(InitialValues.getShowStatusBar());
        viewMenu.add(zoomView);
        viewMenu.add(statusBarView);

        JMenuItem zoomIn = makeMenuItem(new ViewMenuActions.ZoomInAction());
        JMenuItem zoomOut = makeMenuItem(new ViewMenuActions.ZoomOutAction());
        JMenuItem defaultZoom = makeMenuItem(new ViewMenuActions.DefaultZoomAction());
        zoomView.add(zoomIn);
        zoomView.add(zoomOut);
        zoomView.add(defaultZoom);
    }

    private JCheckBoxMenuItem makeCheckBoxMenuItem(Action a) {
        var newMenu = new JCheckBoxMenuItem(a);
        newMenu.setForeground(Color.green);
        return newMenu;
    }

    public static JFrame getFrame() {
        return frame;
    }

    public static JTextArea getEditorTextArea() {
        return editorTextArea;
    }

    public static String getFullQTExport() {
        fullTextQT();
        return qtOutArea.getText();
    }

    public static JPanel getStatusBar() {
        return statusBar;
    }

    public static JMenuItem getSaveAsMenuItem() {
        return saveAsFile;
    }

    public static boolean getNotSaved() {
        return !isSaved.get();
    }

    public static void setIsSaved(boolean value) {
        isSaved.set(value);
        updateFrameTitle();
    }

    public static void setReplaceQuotes() {
        fullTextQT();
    }

    public static void setIsLoadingFile(boolean value) {
        isLoadingFile.set(value);
    }

    public static void setZoomValue(int value) {
        InitialValues.setZoom(value);
        editorTextArea.setFont(InitialValues.getEditorFont());
        qtOutArea.setFont(InitialValues.getEditorFont());
    }

    public static void updateFrameTitle() {
        frame.setTitle(InitialValues.getFileName());
    }

    public static List<AddedWord> getWordList() {
        return quicktype.getWords();
    }

    public static Quicktype getQuicktype() {
        return quicktype;
    }

    public static int getCurrentLine() {
        try {
            return editorTextArea.getLineOfOffset(editorTextArea.getCaretPosition());
        } catch (BadLocationException e) {
            return -1;
        }
    }
    public static int getCursorLocation() {
        return editorTextArea.getSelectionStart();
    }
}