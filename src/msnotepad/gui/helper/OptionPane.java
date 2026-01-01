/*
 * Copyright (c) 2021 Mohit Saini, Under MIT License. Use is subject to license terms.
 * 
 */


import java.awt.Font;

import javax.swing.JLabel;
import javax.swing.JOptionPane;


/**
 * OptionPane class help to make initial OptionPane for the MSNotepad.
 */
public class OptionPane extends JOptionPane {

	/**
	 * showOptionPane method show the optionPane as initialized in the class.
	 * @return the response of the OptionPane.
	 */
	public static int showOptionPane() {
		String[] options = new String[]{"Don't Save", "Save", "Cancel"};
		String message = "Do you want to save changes to \"" + InitialValues.getFileName() + "\" ?";

		JLabel label = new JLabel(message);
		label.setFont(new Font("", Font.PLAIN, 14 + 2));

        return showOptionDialog(
			GUIHandler.getFrame(),
				label,
			"MyNotepad",
			YES_NO_CANCEL_OPTION,
			PLAIN_MESSAGE,
			null,
				options,
            options[0]
		);
	}
}
