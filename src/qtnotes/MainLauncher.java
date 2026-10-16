package qtnotes;/*
 * Copyright (c) 2021 Mohit Saini, Under MIT License. Use is subject to license terms.
 * 
 */


import qtnotes.gui.GUIHandler;
import qtnotes.gui.OverlayMode;
import qtnotes.quicktype.Loading;
import qtnotes.spellcheck.Compression;

import javax.swing.UIManager;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

/** 
 * qtnotes.MainLauncher is the starting point of this Notepad, Moreover,
 * it also set it's look and launch the qtnotes.gui.GUIHandler class,
 * Which handle the further execution.
 */
public class MainLauncher {
    GUIHandler gui;

    private MainLauncher() {
        gui = new GUIHandler();
        gui.handle();
    }
     
	public static void main(String[] args) {
        var list = Arrays.stream(args).toList();
        var found = list.stream().filter(a -> a.endsWith("MyChords.txt")).findFirst();
        Loading.path = found.orElseGet(() -> System.getProperty("user.dir") + File.separator + "MyChords.txt");
        found = list.stream().filter(a -> a.endsWith("Words.dat")).findFirst();
        Compression.path = found.orElseGet(() -> System.getProperty("user.dir") + File.separator + "Words.dat");
        if (!Files.exists(Path.of(Loading.path))){
            try {
                Files.createFile(Path.of(Loading.path));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        if (!Files.exists(Path.of(Compression.path))){
            try {
                Files.createFile(Path.of(Compression.path));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        }
        if (list.contains("-q")) {
            new OverlayMode(null, null, false);
            return;
        }

        loadLookAndFeel();
        new MainLauncher();     //<--- Internal Constructor
	}
    
    private static void loadLookAndFeel() {
        try{
            UIManager.setLookAndFeel("com.sun.java.swing.plaf.gtk.GTKLookAndFeel");
        } catch (Exception ee) {
            ee.printStackTrace();
        }
    }
}