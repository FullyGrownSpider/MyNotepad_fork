/*
 * Copyright (c) 2021 Mohit Saini, Under MIT License. Use is subject to license terms.
 * 
 */


import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileWriter;
import java.util.Scanner;

import javax.swing.AbstractAction;
import javax.swing.KeyStroke;
import javax.swing.JFileChooser;

import static java.awt.event.InputEvent.CTRL_DOWN_MASK;
import static java.awt.event.InputEvent.SHIFT_DOWN_MASK;

public class FileMenuActions {
    
    public static class NewFileAction extends AbstractAction {
        public NewFileAction() {
            super();
            putValue(AbstractAction.NAME, "New File");
            putValue(MNEMONIC_KEY, KeyEvent.VK_N);
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_N, CTRL_DOWN_MASK));
        }
        @Override
        public void actionPerformed(ActionEvent e) {
            if(GUIHandler.getNotSaved()) {
                if (InitialValues.getFilePath() != null) {
                    FileMenuActions.saveFile();
                } else {
                    int value = OptionPane.showOptionPane();
                    if (value == 1) {
                        GUIHandler.getSaveAsMenuItem().doClick();
                    } else if (value == 0) {
                        return;
                    }
                }
            }
            GUIHandler.setIsLoadingFile(true);

            InitialValues.setFileName(InitialValues.NEW_FILE);
            InitialValues.setFilePath(null);
            GUIHandler.setIsSaved(true);
            GUIHandler.getEditorTextArea().setText("");
            GUIHandler.setIsLoadingFile(false);
        }
    }

    public static class NewWindowFileAction extends AbstractAction {
        public NewWindowFileAction() {
            super();
            putValue(AbstractAction.NAME, "New Window");
            putValue(MNEMONIC_KEY, KeyEvent.VK_W);
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_N, CTRL_DOWN_MASK + SHIFT_DOWN_MASK));
        }
        @Override
        public void actionPerformed(ActionEvent e) {
            String fileName = InitialValues.getFileName();
            String filePath = InitialValues.getFilePath();
            InitialValues.setFileName(InitialValues.NEW_FILE);
            InitialValues.setFilePath(null);
            InitialValues.writeToFile(); // <---- this is because new window should be empty
            try {
                Runtime.getRuntime().exec("cmd /c start cmd.exe /K \"cd bin && java com.gmail.mohitsainiknl2.mynotepad.MainLauncher && exit\"");
            } catch (Exception ex) {
                System.out.println("Your are doing something worng...");
            }
            InitialValues.setFileName(fileName);
            InitialValues.setFilePath(filePath);
            InitialValues.writeToFile();
        }
    }

    public static class OpenFileAction extends AbstractAction {
        public OpenFileAction() {
            super();
            putValue(AbstractAction.NAME, "Open");
            putValue(MNEMONIC_KEY, KeyEvent.VK_O);
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_DOWN_MASK));
        }
        @Override
        public void actionPerformed(ActionEvent e) {
            JFileChooser fileChooser = new JFileChooser();
            int value = fileChooser.showOpenDialog(GUIHandler.getFrame());
            if(value == JFileChooser.APPROVE_OPTION) {
                GUIHandler.setIsLoadingFile(true);
                File file = fileChooser.getSelectedFile();
                String filePath = fileChooser.getSelectedFile().getAbsolutePath();
                String fileName = fileChooser.getSelectedFile().getName();
                InitialValues.setFileName(fileName);
                InitialValues.setFilePath(filePath);

                StringBuilder fileText = new StringBuilder();
                try {
                    Scanner read = new Scanner(file);
                    while(read.hasNextLine()) {
                        fileText.append(read.nextLine()).append("\n");
                    }
                    read.close();
                    GUIHandler.setIsSaved(true);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                GUIHandler.getEditorTextArea().setText(fileText.substring(0, fileText.length() - 1));
                GUIHandler.getEditorTextArea().setCaretPosition(0);
                GUIHandler.setIsLoadingFile(false);
            }
        }
    }
    
    public static class SaveAsFileAction extends AbstractAction {
        public SaveAsFileAction() {
            super();
            putValue(AbstractAction.NAME, "Save As");
            putValue(MNEMONIC_KEY, KeyEvent.VK_A);
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.SHIFT_DOWN_MASK | InputEvent.CTRL_DOWN_MASK));
        }
        @Override
        public void actionPerformed(ActionEvent e) {
            JFileChooser fileChooser = new JFileChooser();
            int value = fileChooser.showSaveDialog(GUIHandler.getFrame());
            if(value == JFileChooser.APPROVE_OPTION) {
                String filePath = fileChooser.getSelectedFile().getAbsolutePath();
                String fileName = fileChooser.getSelectedFile().getName();
                InitialValues.setFileName(fileName);
                InitialValues.setFilePath(filePath);

                saveFile();
                GUIHandler.updateFrameTitle();
            }
        }
    }
    public static void saveFile(){
        File file = new File(InitialValues.getFilePath());

        try {
            FileWriter writer = new FileWriter(file);
            writer.write(GUIHandler.getEditorTextArea().getText());
            writer.close();
            GUIHandler.setIsSaved(true);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        GUIHandler.updateFrameTitle();
    }

    public static class ExitFileAction extends AbstractAction {
        public ExitFileAction() {
            super();
            putValue(AbstractAction.NAME, "Kill");
        }
        @Override
        public void actionPerformed(ActionEvent e) {
            System.exit(0);
            
        }
    }


    public static class OpenQuickTypeEditAction extends AbstractAction {
        public OpenQuickTypeEditAction() {
            super();
            putValue(AbstractAction.NAME, "Open Quicktype Edit");
            putValue(MNEMONIC_KEY, KeyEvent.VK_E);
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_E, CTRL_DOWN_MASK));
        }
        @Override
        public void actionPerformed(ActionEvent e) {
            new FullEditForm(GUIHandler.getWordList());
        }
    }

    public static class exportQuickTypeAction extends AbstractAction {
        public exportQuickTypeAction() {
            super();
            putValue(AbstractAction.NAME, "Export Quicktype");
            putValue(MNEMONIC_KEY, KeyEvent.VK_P);
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_P, CTRL_DOWN_MASK));
        }
        @Override
        public void actionPerformed(ActionEvent e) {
            JFileChooser fileChooser = new JFileChooser();
            int value = fileChooser.showSaveDialog(GUIHandler.getFrame());
            if(value == JFileChooser.APPROVE_OPTION) {
                String filePath = fileChooser.getSelectedFile().getAbsolutePath();
                File file = new File(filePath);
                try {
                    FileWriter writer = new FileWriter(file);
                    writer.write(GUIHandler.getFullQuicktypeExport());
                    writer.close();
                    GUIHandler.setIsSaved(true);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                GUIHandler.updateFrameTitle();
            }
        }
    }

}
