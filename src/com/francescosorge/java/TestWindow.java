package com.francescosorge.java;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class TestWindow extends JFrame {
    private JTextField serverURL;
    private JTextField userToken;
    private JButton verifyURL;
    private JButton verifyToken;
    private JButton startApp;
    private JLabel responseText;

    public static void main(String[] args) {
        new TestWindow().setVisible(true);
    }
}
