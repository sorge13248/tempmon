package com.francescosorge.java;

import javax.swing.*;
import java.awt.*;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class MainWindow {
    static Window frame = new Window("Frame demo");

    static JLabel defaultText;
    static JTextField defaultInput;
    static JButton defaultButton;

    static boolean waitForUserInput = false;

    static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        }catch(Exception e) {
            TempMon.genericLogging.add(Logging.Levels.ERROR, "UI Manager doesn't support OS look and fell. Using Swing default instead.");
        }

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel serverPanel = createPanel("start");
        frame.pack();
        frame.setWindowPadding(200);
        frame.centerScreen();

        switchPanel(serverPanel);
    }

    static JPanel createPanel(String panelName) {
        return createPanel(panelName, new AssociativeArray());
    }

    static JPanel createPanel(String panelName, AssociativeArray arguments) {
        JPanel jpanel = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.VERTICAL;

        if (panelName.equals("start")) {
            defaultText = new JLabel("TempMon is loading");
            jpanel.add(defaultText, c);
        } else if (panelName.equals("server")) {
            defaultText = new JLabel("Server URL");
            jpanel.add(defaultText, c);

            defaultInput = new JTextField();
            if (arguments.get("server-url") != null) {
                defaultInput.setText(arguments.get("server-url").toString());
            }
            defaultInput.setSize(new Dimension(100, 40));
            jpanel.add(defaultInput, c);

            defaultButton = new JButton("Connect");
            if (arguments.get("server-url") != null) {
                defaultButton.setEnabled(false);
            }
            defaultButton.setSize(new Dimension(50, 40));
            defaultButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    waitForUserInput = false;
                }
            });
            jpanel.add(defaultButton, c);
        } else if (panelName.equals("token")) {
            defaultText = new JLabel("User token");
            jpanel.add(defaultText, c);

            defaultInput = new JTextField();

            if (arguments.get("user-token") != null) {
                defaultInput.setText(arguments.get("user-token").toString());
            }

            defaultInput.setSize(new Dimension(100, 40));
            jpanel.add(defaultInput, c);
        } else if (panelName.equals("ready")) {
            JLabel serverText = new JLabel("TempMon is ready and running.");
            jpanel.add(serverText, c);
        }

        return jpanel;
    }

    static void setText(JLabel label, String text) {
        label.setText(text);
    }

    static void switchPanel(JPanel panel) {
        frame.setContentPane(panel);
        frame.setVisible(true);
    }

    static String getInput(String name) {
        waitForUserInput = true;

        while(waitForUserInput) {
            // aspetta finché l'utente non immette input
        }
        String input = null;
        if (name.equals("server-url")) {
            input = defaultInput.getText();
        }

        return input;
    }
}
