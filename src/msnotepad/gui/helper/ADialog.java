
package msnotepad.gui.helper;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;

public class ADialog extends JDialog {
    public ADialog(Frame owner, String title, boolean modal)
    {
        super(owner,title,modal);
        KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
        JRootPane rootPane = new JRootPane();
        rootPane.registerKeyboardAction(_ ->dispose(), stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
        this.rootPane = rootPane;
    }
}
