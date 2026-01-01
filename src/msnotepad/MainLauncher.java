/*
 * Copyright (c) 2021 Mohit Saini, Under MIT License. Use is subject to license terms.
 * 
 */


import quicktype.Loading;

import javax.swing.UIManager;

/** 
 * MainLauncher is the starting point of this Notepad, Moreover,
 * it also set it's look and launch the GUIHandler class,
 * Which handle the further execution.
 */
public class MainLauncher {
    GUIHandler gui;

    private MainLauncher() {
        gui = new GUIHandler();
        gui.handle();
    }
     
	public static void main(String[] args) {
        if (args.length == 0)
            return;
        Loading.path = args[0];
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