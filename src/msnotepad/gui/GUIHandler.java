/*
 * Copyright (c) 2021 Mohit Saini, Under MIT License. Use is subject to license terms.
 *
 */

import quicktype.AddedWord;
import quicktype.Quicktype;

import javax.swing.*;
import java.awt.*;
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

import static java.awt.event.InputEvent.CTRL_DOWN_MASK;

/**
 * GUIHandler class handle the main gui functioning of the MSNotepad, this is the point of
 * distribution of work to other classes.
 */
public class GUIHandler {
    private static JFrame frame;
    private static JScrollPane editorScrollPane;
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
    private static JMenuItem cutEdit;
    private static JMenuItem copyEdit;
    private static JMenuItem pasteEdit;
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
                new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, editorScrollPane, editorQuickOutArea);
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

    private JTextArea initTextArea(){
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
            @Override
            public void keyTyped(KeyEvent e) {
                super.keyTyped(e);
                fullCompare(e.getKeyChar());
            }

            @Override
            public void keyPressed(KeyEvent e) {
                count = 2;
                if (e.isControlDown() || e.isAltDown()) return;
                int charTyped = e.getKeyChar();
                //TODO shift tab (for every line remove one tab)
                if (e.isActionKey()){
                    if (undoIndex == MAX_LIST){
                        return;
                    } else if (undoActionList.get(undoIndex) != null){
                        undoIndex++;
                        return;
                    }
                }
                var selected = editorTextArea.getSelectedText();
                if (selected != null) {
                    addRemoveToUndo(selected);
//                    removeCompare();
                    undoIndex++;
                    if (charTyped == KeyEvent.VK_BACK_SPACE || charTyped == KeyEvent.VK_DELETE) {
                        return;
                    }
                    if (e.isShiftDown() && charTyped == KeyEvent.VK_ENTER) {
                        e.consume();
                        editorTextArea.insert("\n", editorTextArea.getSelectionStart());
                    }
                    addToUndo(charTyped);
                    undoIndex++;
                }
                else if (isSplitter(charTyped) ) {
                    addToUndo(charTyped);
//                    doCompare();
                    undoIndex++;
                    if (e.isShiftDown() && charTyped == KeyEvent.VK_ENTER) {
                        e.consume();
                        editorTextArea.insert("\n", editorTextArea.getSelectionEnd());
                    }
                } else if (charTyped == KeyEvent.VK_BACK_SPACE || charTyped == KeyEvent.VK_DELETE) {
                    int location = editorTextArea.getSelectionEnd();
                    try {
                        if (charTyped == KeyEvent.VK_BACK_SPACE) {
                            if (location == 0) {
                                e.consume();
                                return;
                            }
                            addRemoveToUndo(editorTextArea.getText(location -1, 1));
                        } else {
                            addRemoveToUndo(editorTextArea.getText(location, 1));
                        }
//                        removeCompare();
                        undoIndex++;
                    } catch (BadLocationException ex) {
                        e.consume();
                    }
                } else {
                    addToUndo(charTyped);
                }
                removeThingsAhead();
                super.keyPressed(e);
            }
        });
        editorScrollPane = new JScrollPane(editorTextArea);
        editorScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        editorScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        editorScrollPane.setBorder(new LineBorder(Color.WHITE, 0));


        if (InitialValues.getFilePath() != null) {
            loadFileToEditor();
        }

        doUpdateWork();
        editorTextArea.addCaretListener(e -> {
            if (editorTextArea.getSelectedText() == null) {
                cutEdit.setEnabled(false);
                copyEdit.setEnabled(false);
            } else {
                cutEdit.setEnabled(true);
                copyEdit.setEnabled(true);
            }
        });

        editorTextArea.addCaretListener(e -> findNSetPositionIndicator());
        editorTextArea.setSelectionStart(editorTextArea.getText().length());
    }

    private void removeCompare() {
//        fullCompare();
        //TODO
    }

    private void doCompare() {
        //TODO
//        fullCompare();
    }

    public static void fullCompare(int lastChar){
        if (lastChar == KeyEvent.VK_BACK_SPACE || lastChar == KeyEvent.VK_DELETE) return;

        var textOld = editorTextArea.getText();
        var text = (textOld.substring(0, editorTextArea.getSelectionStart()) + Character.toString(lastChar) + textOld.substring(editorTextArea.getSelectionStart()));

        editorQuickOutArea.setText("");
        editorQuickOutArea.setText(AddedWord.createText(text, quicktype.data));
        //TODO
    }

    private boolean isSplitter(int c){
        return c == KeyEvent.VK_ENTER || c == KeyEvent.VK_SPACE || c == KeyEvent.VK_TAB;
    }

    public static void findNSetPositionIndicator() {
        int lineNum = 1;
        int columnNum = 1;

        int caretPosition;
        try {
            caretPosition = editorTextArea.getCaretPosition();
            lineNum = editorTextArea.getLineOfOffset(caretPosition);
            columnNum = caretPosition - editorTextArea.getLineStartOffset(lineNum);

        } catch (BadLocationException ee) {
            ee.printStackTrace();
        }
        if (statusBar != null)
            statusBar.setCaretPosition(lineNum, columnNum);
    }

    private void addToUndo(int keyChar) {
        removeThingsAhead();
        var undo = undoActionList.get(undoIndex);
        if (undo == null) {
            undoActionList.set(undoIndex,
                    new UndoAction(editorTextArea.getSelectionEnd(),
                            Character.toString(keyChar),
                            false));
        } else {
            undo.text.append(Character.toString(keyChar));
        }
    }

    /**
     * the protections of the list
     * will call the list shuffle (0 = 1, 1 = 2...)
     * else will check if there are any redo's ahead of the current index
     */
    private void removeThingsAhead() {
        if (undoIndex == undoActionList.size()) undoListSubFirst();
        else if (undoIndex == -1) undoIndex = 0;
        else {
            int counter = undoIndex + 1;
            if (counter != MAX_LIST) {
                var undo = undoActionList.get(counter);
                while (undo != null) {
                    undoActionList.set(counter, null);
                    counter++;
                    if (counter == MAX_LIST) break;
                    undo = undoActionList.get(counter);
                }
            }
        }
    }

    private void addRemoveToUndo(String text) {
        removeThingsAhead();
        undoActionList.set(undoIndex,
                new UndoAction(editorTextArea.getSelectionStart(),
                        text,
                        true));
    }

    /**
     * if the counter is on the max you need to make the items in the list go one back, and forget the first one
     */
    private void undoListSubFirst() {
        undoIndex--;
        int max = MAX_LIST - 1;
        for (int i = 0; i < max; i++){
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

    private JMenu makeMenu(String s){
        var newMenu = new JMenu(s);
        newMenu.setForeground(Color.green);
        return newMenu;
    }
    private JMenuItem makeMenuItem(Action a){
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
        JMenuItem exitFile = makeMenuItem(new FileMenuActions.ExitFileAction());
        fileMenu.add(newFile);
        fileMenu.add(newWindowFile);
        fileMenu.addSeparator();
        fileMenu.add(openFile);
        fileMenu.add(saveAsFile);
        fileMenu.add(openFile);
        fileMenu.add(editQuicktype);
        fileMenu.add(exportQuicktype);
        fileMenu.addSeparator();
        fileMenu.add(exitFile);

        cutEdit = makeMenuItem(ClipboardActions.getCutAction());
        copyEdit = makeMenuItem(ClipboardActions.getCopyAction());
        pasteEdit = makeMenuItem(ClipboardActions.getPasteAction());
        JMenuItem undoEdit = makeMenuItem(new EditMenuActions.UndoEditAction());
        JMenuItem redoEdit = makeMenuItem(new EditMenuActions.RedoEditAction());
        JMenuItem deleteEdit = makeMenuItem(new EditMenuActions.DeleteEditAction());
        findEdit = makeMenuItem(new EditMenuActions.FindEditAction());
        replaceEdit = makeMenuItem(new EditMenuActions.ReplaceEditAction());
        JMenuItem selectAllEdit = makeMenuItem(new EditMenuActions.SelectAllEditAction());
        editMenu.add(undoEdit);
        editMenu.add(redoEdit);
        editMenu.addSeparator();
        editMenu.add(cutEdit);
        editMenu.add(copyEdit);
        editMenu.add(pasteEdit);
        editMenu.addSeparator();
        editMenu.add(findEdit);
        editMenu.add(replaceEdit);
        editMenu.addSeparator();
        editMenu.add(deleteEdit);
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

        setRemainingMnemonicAndAccelerator();
    }

    private JCheckBoxMenuItem makeCheckBoxMenuItem(Action a) {
        var newMenu = new JCheckBoxMenuItem(a);
        newMenu.setForeground(Color.green);
        return newMenu;
    }

    /**
     * setRemainingMnemonicAndAccelerator method is help to set the MnemonicKeys and
     * the Accelerator of the remaining items of the menus in the menu bar.
     */
    private void setRemainingMnemonicAndAccelerator() {
        cutEdit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, CTRL_DOWN_MASK));
        copyEdit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, CTRL_DOWN_MASK));
        pasteEdit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, CTRL_DOWN_MASK));
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
        fullCompare(KeyEvent.VK_ENTER);
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
            undoIndex = MAX_LIST -1;
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
        else if (undoIndex == MAX_LIST){
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