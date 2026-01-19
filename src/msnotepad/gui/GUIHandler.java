/*
 * Copyright (c) 2021 Mohit Saini, Under MIT License. Use is subject to license terms.
 *
 */

import quicktype.AddedWord;
import quicktype.Quicktype;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.Timer;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;
//Wn ß gets bk home fm work ß hld qf mr zn enf tm t buy hm mr food.
//Q looked around it arst about already that



/**
 * GUIHandler class handle the main gui functioning of the MSNotepad, this is the point of
 * distribution of work to other classes.
 */
public class GUIHandler {
    private static JFrame frame;
    private static JScrollPane editorScrollPane, editorScrollPaneOutArea;
    private static JMenuBar menuBar;
    private static JPanel mainPanel;
    private static JTextArea editorTextArea;
    private static JTextArea editorQuickOutArea;
    private static final Timer autoSave = new Timer();
    private static StatusBar statusBar;
    private static final AtomicBoolean isSaved = new AtomicBoolean(true);
    private static final AtomicBoolean isLoadingFile = new AtomicBoolean(false);
    private static final Quicktype quicktype = new Quicktype();
    private static byte count = 0;
    private static int undoIndex = 0;
    private static final int MAX_LIST = 48;
    private static final ArrayList<UndoAction> undoActionList = new ArrayList<>(MAX_LIST);

    private static JMenu fileMenu, editMenu, formatMenu, viewMenu;
    private static JMenuItem saveAsFile;
    private static JMenuItem findEdit;
    private static JMenuItem replaceEdit;

    private static int zoomLevel = 100;
    public static JCheckBoxMenuItem statusBarView;

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

        frame.setTitle(InitialValues.getFileName());
        mainPanel = (JPanel) frame.getContentPane();

        initialiseMenuBar();
        frame.setJMenuBar(menuBar);
        initialiseScrollPane();

        statusBar = new StatusBar();
        mainPanel.setLayout(new BorderLayout());
        var splitPanel =
                new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, editorScrollPane, editorScrollPaneOutArea);
        splitPanel.setResizeWeight(.5);
        mainPanel.add(splitPanel, BorderLayout.CENTER);
        mainPanel.add(statusBar, BorderLayout.SOUTH);

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        undoInit();

        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                count--;
                if (count > 0)//this should probably be an option?
                    return;
                if (count < -2) {
                    count = -5;
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

    private void undoInit() {
        //init the undo list
        for (int i = undoIndex; i < 48; i++) {
            undoActionList.add(null);
        }
    }

    private JTextArea initTextArea() {
        var textArea = new JTextArea() {
            @Override
            public void setFont(Font font) {
                String family = font.getFamily();
                int style = font.getStyle();
                int size = font.getSize();

                int originalPix = InitialValues.getEditorFont().getSize() + 5;
                int zoomPix = (zoomLevel * originalPix) / 100 - originalPix;

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
                count = -5;
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
                doUpdateWork();
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                if (!isLoadingFile.get()) {
                    setIsSaved(false);
                }
                doUpdateWork();
            }

            @Override
            public void changedUpdate(DocumentEvent arg0) {
            }
        });
        return textArea;
    }

    /**
     * initialiseScrollPane method is help to setup the text editor of the MSNotepad.
     */
    private void initialiseScrollPane() {
        editorTextArea = initTextArea();
        editorQuickOutArea = initTextArea();
        editorTextArea.addKeyListener(new KeyAdapter() {
        private boolean needFullReset = false;
            public void keyReleased(KeyEvent e) {
                if (needFullReset){
                    fullCompare();
                    return;
                }
                if (e.getKeyCode() == KeyEvent.VK_ENTER){
                    enterCompare();
                    return;
                }
                if (e.getKeyCode() == KeyEvent.VK_DELETE){
                    enterCompare();
                    return;
                }
                doCompare();
            }

            @Override
            public void keyPressed(KeyEvent e) {
                super.keyPressed(e);
                count = 2;
                int charCode = e.getKeyCode();
                if (ignoreKeys(charCode)) return;
                if (e.isAltDown()) return;
                if (charCode == KeyEvent.VK_ENTER){
                    if (e.isShiftDown()) {
                        e.setModifiers(0);
                    }
                }
                if (e.isControlDown()) {
                    if (charCode == KeyEvent.VK_V) {
                        pasteIntoEdit();
                    }
                    else if (charCode == KeyEvent.VK_X) {
                        CopyIntoEdit(true);
                    }
                    else if (charCode == KeyEvent.VK_C) {
                        CopyIntoEdit(false);
                    }
                    needFullReset = true;
                    return;
                }
                if (e.isActionKey()) {
                    moveToNextIndex();
                    if (!e.isShiftDown() && editorTextArea.getSelectedText() != null) {
                        if (charCode == KeyEvent.VK_LEFT || charCode == KeyEvent.VK_UP) {
                            editorTextArea.setCaretPosition(editorTextArea.getSelectionStart());
                        } else if (charCode == KeyEvent.VK_RIGHT || charCode == KeyEvent.VK_DOWN) {
                            editorTextArea.setCaretPosition(editorTextArea.getSelectionEnd());
                        }
                    }
                    return;
                }
                var selected = editorTextArea.getSelectedText();
                needFullReset = selected != null;

                if (charCode == KeyEvent.VK_TAB && (e.isShiftDown() || needFullReset)) {
                    removeTab(e.isShiftDown());
                    needFullReset = true;
                    return;
                }
                if (needFullReset) {
                    addRemoveToUndo(selected);
                    editorTextArea.replaceRange("", editorTextArea.getSelectionStart(), editorTextArea.getSelectionEnd());
                    if (charCode == KeyEvent.VK_BACK_SPACE || charCode == KeyEvent.VK_DELETE) {
                        return;
                    }
                    addToUndo(e.getKeyChar());
                    return;
                }
                if (e.isShiftDown() && charCode == KeyEvent.VK_DELETE) {
                    deleteLine();
                    return;
                }
                if (charCode == KeyEvent.VK_BACK_SPACE || charCode == KeyEvent.VK_DELETE) {
                    int location = editorTextArea.getCaretPosition();
                    try {
                        if (charCode == KeyEvent.VK_BACK_SPACE) {
                            if (location == 0) {
                                return;
                            }
                            addRemoveToUndo(editorTextArea.getText(location - 1, 1));
                        } else {
                            if (location >= editorTextArea.getLineEndOffset(editorTextArea.getLineCount())) {
                                return;
                            }
                            addRemoveToUndo(editorTextArea.getText(location, 1));
                        }
                        needFullReset = true;
                    } catch (BadLocationException ignored) {
                    }
                    return;
                }
                addToUndo(e.getKeyChar());
            }
        });
        editorScrollPane = new JScrollPane(editorTextArea);
        editorScrollPaneOutArea = new JScrollPane(editorQuickOutArea);
        editorScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        editorScrollPaneOutArea.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        editorScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        editorScrollPaneOutArea.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        editorScrollPane.setBorder(new LineBorder(Color.WHITE, 0));
        editorScrollPaneOutArea.setBorder(new LineBorder(Color.WHITE, 0));


        if (InitialValues.getFilePath() != null) {
            loadFileToEditor();
        }

        doUpdateWork();

        editorTextArea.addCaretListener(e -> findNSetPositionIndicator());
        editorTextArea.setSelectionStart(editorTextArea.getText().length());
    }

    private boolean ignoreKeys(int charTyped) {
        return charTyped == KeyEvent.VK_SHIFT || charTyped == KeyEvent.VK_CONTROL ||
                charTyped == KeyEvent.VK_ALT || charTyped == KeyEvent.VK_META ||
                charTyped == KeyEvent.VK_CAPS_LOCK;
    }

    private void deleteLine() {
        var caretPosition = editorTextArea.getCaretPosition();
        int lineStart, lineEnd;
        try {
            int line = editorTextArea.getLineOfOffset(caretPosition);
            lineStart = editorTextArea.getLineStartOffset(line);
            lineEnd = editorTextArea.getLineEndOffset(line);
        } catch (BadLocationException e) {
            throw new RuntimeException(e);
        }
        editorTextArea.select(lineStart, lineEnd);
        var selected = editorTextArea.getSelectedText();
        if (selected == null) return;
        addRemoveToUndo(selected);
        editorTextArea.replaceRange("", editorTextArea.getSelectionStart(), editorTextArea.getSelectionEnd());
    }

    private void CopyIntoEdit(boolean cut) {
        var selected = editorTextArea.getSelectedText();
        if (selected == null) {
            var caretPosition = editorTextArea.getCaretPosition();
            int lineStart, lineEnd;
            try {
                int line = editorTextArea.getLineOfOffset(caretPosition);
                lineStart = editorTextArea.getLineStartOffset(line);
                lineEnd = editorTextArea.getLineEndOffset(line);
            } catch (BadLocationException e) {
                throw new RuntimeException(e);
            }
            editorTextArea.select(lineStart, lineEnd);
            selected = editorTextArea.getSelectedText();
            if (selected == null || selected.isEmpty()) return;
        }
        if (cut) {
            addRemoveToUndo(selected);
            editorTextArea.replaceRange("", editorTextArea.getSelectionStart(), editorTextArea.getSelectionEnd());
        }
        Clipboard cb = Toolkit.getDefaultToolkit().getSystemClipboard();

        StringSelection data = new StringSelection(selected);
        cb.setContents(data, null);
    }

    private void pasteIntoEdit() {
        var selected = editorTextArea.getSelectedText();
        if (selected != null) {
            addRemoveToUndo(selected);
            editorTextArea.replaceRange("", editorTextArea.getSelectionStart(), editorTextArea.getSelectionEnd());
        }
        Clipboard cb = Toolkit.getDefaultToolkit().getSystemClipboard();
        try {
            String text;
            var trans = cb.getContents(null);
            if (trans.isDataFlavorSupported(DataFlavor.stringFlavor)) {
                text = (String) trans.getTransferData(DataFlavor.stringFlavor);
            } else {
                System.out.println("Couldn't get data from the clipboard");
                return;
            }
            addToUndo(text);
        } catch (IOException | UnsupportedFlavorException ex) {
            throw new RuntimeException(ex);
        }
    }

    private void removeTab(boolean shiftDown) {
        undoIndex = 0;
        for (int i = 0; i < MAX_LIST; i++) {
            undoActionList.set(i, null);
        }
        try {
            var lineNumQuickEnd = editorTextArea.getLineOfOffset(editorTextArea.getSelectionEnd());
            var lineNumQuickStart = editorTextArea.getLineOfOffset(editorTextArea.getSelectionStart());
            for (int i = lineNumQuickStart; i <= lineNumQuickEnd; i++) {
                int index = editorTextArea.getLineStartOffset(i);
                if (shiftDown) {
                    if (editorTextArea.getText(index, 1).equals("\t")) {
                        editorTextArea.setText(editorTextArea.getText(0, index) +
                                editorTextArea.getText(index + 1, editorTextArea.getText().length() - index - 1)
                        );
                    }
                } else {
                    editorTextArea.insert("\t", index);
                }
            }
        } catch (BadLocationException ignored) {

        }
    }

    private void enterCompare() {
        try {
            int line = editorTextArea.getLineOfOffset(editorTextArea.getCaretPosition());

            if (line == 0){
                fullCompare();
                return;
            }
            line--;

            int lineStart = editorTextArea.getLineStartOffset(line);
            int lineStartOut = 0;
            if (line != 0) lineStartOut = editorQuickOutArea.getLineStartOffset(line);
            int lineEnd = editorTextArea.getLineEndOffset(editorTextArea.getLineCount() - 1);
            var newText = editorQuickOutArea.getText(0, lineStartOut) + AddedWord.createText(editorTextArea.getText(lineStart, lineEnd - lineStart), quicktype.data);
            editorQuickOutArea.setText(newText);
        } catch (Exception ignored){

        }
    }

    public static void doCompare(){
        try {
            doCompare(editorTextArea.getLineOfOffset(editorTextArea.getCaretPosition()));
        } catch (Exception ignored){

        }
    }

    public static void doCompare(int line){
        try {
            int lineStart = editorTextArea.getLineStartOffset(line);

            var oldText = editorTextArea.getText(lineStart, editorTextArea.getLineEndOffset(line) - lineStart);
            var newText = AddedWord.createText(oldText, quicktype.data);

            setIncorrect(oldText, newText);

            while (editorQuickOutArea.getLineCount() <= line)
                editorQuickOutArea.append("\n");

            editorQuickOutArea.replaceRange(newText, editorQuickOutArea.getLineStartOffset(line), editorQuickOutArea.getLineEndOffset(line));
        } catch (Exception ignored) {
        }
    }

    private static void setIncorrect(String oldText, String newText) {
        var oldWords = Arrays.stream(oldText.split(" ")).distinct().toList();

        for (int i = Math.max(0, oldWords.size() - 2); i < oldWords.size(); i++) {
            if (oldWords.get(i).length() < 3) continue;
            if (newText.contains(oldWords.get(i))){
                var wrong = AddedWord.exists(oldWords.get(i), quicktype.data);
                if (!wrong.isEmpty()) {
                    statusBar.setHintText(wrong);
                    return;
                }
            }
        }
        statusBar.setHintText("");
    }

    public static void fullCompare() {
        var textOld = editorTextArea.getText();
        editorQuickOutArea.setText(AddedWord.createText(textOld, quicktype.data));
    }

    private boolean isSplitter(int c) {
        return c == KeyEvent.VK_ENTER || c == KeyEvent.VK_SPACE || c == KeyEvent.VK_TAB || c == KeyEvent.VK_MINUS;
    }

    public static void findNSetPositionIndicator() {
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

    /// move to the next index of the list or clip the list
    private void moveToNextIndex() {
        if (undoIndex < 0) {
            undoIndex++;
            return;
        }
        if (null == undoActionList.get(undoIndex)) {
            return;
        }
        if (undoIndex == MAX_LIST - 1) {
            undoListSubFirst();
        } else {
            undoIndex++;
        }
    }

    private void addToUndo(int keyChar) {
        if (isSplitter(keyChar)) {
            moveToNextIndex();
        }
        var currentUndo = undoActionList.get(undoIndex);
        if (currentUndo == null) {
            undoActionList.set(undoIndex,
                    new UndoAction(editorTextArea.getCaretPosition(),
                            Character.toString(keyChar),
                            false));
        } else {
            currentUndo.text.append(Character.toString(keyChar));
        }
    }

    private void addToUndo(String full) {
        moveToNextIndex();
        undoActionList.set(undoIndex,
                new UndoAction(editorTextArea.getSelectionStart(),
                        full,
                        false));
        moveToNextIndex();
    }


    private void addRemoveToUndo(String text) {
        moveToNextIndex();
        undoActionList.set(undoIndex,
                new UndoAction(editorTextArea.getSelectionStart(),
                        text,
                        true));
        moveToNextIndex();
    }

    /**
     * if the counter is on the max you need to make the items in the list go one back, and forget the first one
     */
    private void undoListSubFirst() {
        if (undoIndex < 0)
            undoIndex--;
        int max = MAX_LIST - 1;
        for (int i = 0; i < max; i++) {
            undoActionList.set(i, undoActionList.get(i + 1));
        }
        undoActionList.set(max, null);
    }

    /**
     * doUpdateWork method is help to set enable/disable the edit action of the
     * text editor.
     */
    private void doUpdateWork() {
        if (editorTextArea.getText().isEmpty()) {
            findEdit.setEnabled(false);
            replaceEdit.setEnabled(false);
        } else {
            findEdit.setEnabled(true);
            replaceEdit.setEnabled(true);
        }
    }

    /**
     * loadFileToEditor method is help to load/reload the file,
     * whose name is saved in InitialValues class.
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
            System.out.println(path);
            System.out.println("File not found...");
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
    }

    /**
     * initialiseMenuBar method is help to initialise and setup the menu bar
     * of this app, and also initialize the menus and their item in the menu.
     */
    private void initialiseMenuBar() {
        menuBar = new JMenuBar();
        menuBar.setBorderPainted(false);

        fileMenu = makeMenu("File");
        editMenu = makeMenu("Edit");
        formatMenu = makeMenu("Format");
        viewMenu = makeMenu("View");
        initialiseMenuItems();

        menuBar.add(fileMenu);
        menuBar.add(editMenu);
        menuBar.add(formatMenu);
        menuBar.add(viewMenu);
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

    private void initialiseMenuItems() {
        JMenuItem newFile = makeMenuItem(new FileMenuActions.NewFileAction());
        JMenuItem newWindowFile = makeMenuItem(new FileMenuActions.NewWindowFileAction());
        JMenuItem openFile = makeMenuItem(new FileMenuActions.OpenFileAction());
        JMenuItem editQuicktype = makeMenuItem(new FileMenuActions.OpenQuickTypeEditAction());
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
        fileMenu.add(editQuicktype);
        fileMenu.add(copyQuicktype);
        fileMenu.add(exportQuicktype);
        fileMenu.addSeparator();
        fileMenu.add(exitFile);

        JMenuItem undoEdit = makeMenuItem(new EditMenuActions.UndoEditAction());
        JMenuItem redoEdit = makeMenuItem(new EditMenuActions.RedoEditAction());
        findEdit = makeMenuItem(new EditMenuActions.FindEditAction());
        replaceEdit = makeMenuItem(new EditMenuActions.ReplaceEditAction());
        JMenuItem selectAllEdit = makeMenuItem(new EditMenuActions.SelectAllEditAction());
        editMenu.add(undoEdit);
        editMenu.add(redoEdit);
        editMenu.addSeparator();
        editMenu.addSeparator();
        editMenu.add(findEdit);
        editMenu.add(replaceEdit);
        editMenu.addSeparator();
        editMenu.add(selectAllEdit);

        JCheckBoxMenuItem wordWrapFormat = makeCheckBoxMenuItem(new FormatMenuActions.WordWrapFormatAction());
        wordWrapFormat.setState(InitialValues.getWrapTheLine());
        JMenuItem fontChangeFormat = makeMenuItem(new FormatMenuActions.FontChangeFormatAction());
        formatMenu.add(wordWrapFormat);
        formatMenu.add(fontChangeFormat);

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

    /**
     * getFrame is the getter of the frame.
     *
     * @return the frame.
     */
    public static JFrame getFrame() {
        return frame;
    }

    /**
     * getEditorTextArea method is the getter of main text-area.
     *
     * @return the editorTextArea.
     */
    public static JTextArea getEditorTextArea() {
        return editorTextArea;
    }

    public static String getFullQuicktypeExport() {
        fullCompare();
        return editorQuickOutArea.getText();
    }

    /**
     * getStatusBar method is the getter of statusBar.
     *
     * @return the statusBar.
     */
    public static JPanel getStatusBar() {
        return statusBar;
    }

    /**
     * getSaveAsMenuItem method is the getter of saveAsFile.
     *
     * @return the saveAsFile.
     */
    public static JMenuItem getSaveAsMenuItem() {
        return saveAsFile;
    }

    /**
     * getIsSaved method is the getter of isSaved variable.
     *
     * @return the isSaved variable.
     */
    public static boolean getNotSaved() {
        return !isSaved.get();
    }

    /**
     * setIsSave method is set the isSaved value the variable.
     *
     * @param value the value of isSaved.
     */
    public static void setIsSaved(boolean value) {
        isSaved.set(value);
        updateFrameTitle();
    }

    /**
     * setIsLoadingFile method is set the loading flag of the this app.
     *
     * @param value loading flag.
     */
    public static void setIsLoadingFile(boolean value) {
        isLoadingFile.set(value);
    }

    /**
     * getZoomValue method is help to the zoomValue of the textArea.
     *
     * @return the zoomLevel.
     */
    public static int getZoomValue() {
        return zoomLevel;
    }

    /**
     * setZoomValue method is help to set the zoom level of the editor textArea.
     *
     * @param value the zoom level.
     */
    public static void setZoomValue(int value) {
        zoomLevel = value;
        editorTextArea.setFont(InitialValues.getEditorFont());
        editorQuickOutArea.setFont(InitialValues.getEditorFont());
    }

    /**
     * updateFrameTitle method is help to change the file name and unsaved mark
     * of the opened file.
     */
    public static void updateFrameTitle() {
        frame.setTitle(InitialValues.getFileName());
    }

    public static List<AddedWord> getWordList() {
        return quicktype.getWords();
    }

    public static Quicktype getQuicktype() {
        return quicktype;
    }

    public static UndoAction getUndoAction() {
        if (undoIndex >= MAX_LIST) {
            undoIndex = MAX_LIST - 1;
        } else if (undoIndex < 0)
            return null;
        var found = undoActionList.get(undoIndex);
        if (found != null || undoIndex == 0)
            return found;
        undoIndex--;
        return undoActionList.get(undoIndex);
    }

    public static UndoAction getRedoAction() {
        if (undoIndex < 0)
            undoIndex = 0;
        else if (undoIndex == MAX_LIST) {
            return null;
        }
        return undoActionList.get(undoIndex);
    }

    public static void decreaseUndo() {
        if (undoIndex != -1)
            undoIndex--;
    }

    public static void increaseUndo() {
        if (undoIndex < MAX_LIST) {
            undoIndex++;
        }
    }

}