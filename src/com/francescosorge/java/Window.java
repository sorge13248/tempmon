package com.francescosorge.java;

import javax.swing.*;
import java.awt.*;

public class Window extends JFrame {
    public Window() throws HeadlessException {
    }

    public Window(GraphicsConfiguration gc) {
        super(gc);
    }

    public Window(String title) throws HeadlessException {
        super(title);
    }

    public Window(String title, GraphicsConfiguration gc) {
        super(title, gc);
    }

    public void setWindowPadding(int padding) {
        Insets insets = this.getInsets();
        this.setSize(new Dimension(insets.left + insets.right + padding,insets.top + insets.bottom + padding));
    }

    public void centerScreen() {
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        this.setLocation(dim.width/2 - this.getSize().width/2, dim.height/2 - this.getSize().height/2);
    }

    @Override
    public void setVisible(boolean b) {
        super.setVisible(b);
        SwingUtilities.updateComponentTreeUI(this); // updates UI look
    }
}
