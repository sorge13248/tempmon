package com.francescosorge.java;

import java.awt.*; // Abstract Window Toolkit
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class BasicWindow extends Frame {
    public BasicWindow(String title) {
        super(title);
        setSize(500, 140);
        addWindowFocusListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);

        Font sansSerifLagre = new Font("SansSerif", Font.BOLD, 18);
        Font sansSerifSmall = new Font("SansSerif", Font.BOLD, 13);

        g.setFont(sansSerifLagre);
        g.drawString("TempMon", 60, 60);

        g.setFont(sansSerifSmall);
        g.drawString("Version 0.1 - by FrancescoSorge.com", 60, 80);
    }
}
