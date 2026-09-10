package qtnotes.gui.helper;/*
 * Copyright (c) 2021 Mohit Saini, Under MIT License. Use is subject to license terms.
 * 
 */


import qtnotes.gui.GUIHandler;
import qtnotes.init.InitialValues;

import java.awt.Font;

import javax.swing.JLabel;
import javax.swing.JOptionPane;


/**
 * qtnotes.gui.helper.OptionPane class help to make initial qtnotes.gui.helper.OptionPane for the MSNotepad.
 */
public class OptionPane extends JOptionPane {

	/**
	 * showOptionPane method show the optionPane as initialized in the class.
	 * @return the response of the qtnotes.gui.helper.OptionPane.
	 */
	public static int showOptionPane() {
		String[] options = new String[]{"Don't Save", "Save"};
		String message = "Do you want to save changes to \"" + InitialValues.getFileName() + "\" ?";

		JLabel label = new JLabel(message);
		label.setFont(new Font("", Font.PLAIN, 14 + 2));

        return showOptionDialog(
			GUIHandler.getFrame(),
				label,
			"MyNotepad",
			YES_NO_OPTION,
			PLAIN_MESSAGE,
			null,
				options,
            options[0]
		);
	}
}
