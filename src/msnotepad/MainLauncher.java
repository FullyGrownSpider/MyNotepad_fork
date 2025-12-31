/*
 * Copyright (c) 2021 Mohit Saini, Under MIT License. Use is subject to license terms.
 * 
 */


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
        loadLookAndFeel();
        new MainLauncher();     //<--- Internal Constructor
	}
    
    private static void loadLookAndFeel() {
        try{
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ee) {
            ee.printStackTrace();
        }
    }
}

//while typing make text appear in the second JTextArea (for now put it on the right, but make it so you can put it under above or left too (use settings.txt))
//make it so you can edit the words in the second JTextArea (zt = zt, *user edits on the left, zt = that)
