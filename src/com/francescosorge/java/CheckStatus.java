package com.francescosorge.java;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class CheckStatus extends Thread {
    private volatile boolean done = false;
    private volatile int seconds = 1;

    public CheckStatus() {
    }

    public CheckStatus(int seconds) {
        setSeconds(seconds);
    }

    public void setSeconds(int seconds) {
        this.seconds = seconds;
    }

    @Override
    public synchronized void run() {
        super.run();
        while (!done) {
            SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            Date date = new Date();
            System.out.println(formatter.format(date) + "");
            System.out.println("Server url: " + Settings.url);
            System.out.println("User token: " + Settings.token);
            System.out.println("Fetched from server:");
            Main.printSoftwareSettings();
            System.out.println("CPU temperature (max): " + Main.calculateCpuTemp("max"));
            System.out.println("Check against CPU:");

            boolean cpuOverheated = false;
            try {
                cpuOverheated = Main.checkAgainstCpuTemp();
            }catch(Exception e) {
                System.out.println(e.toString());
            }
            System.out.println("Is CPU overheated? " + (cpuOverheated ? "Yes" : "No"));
            System.out.println("+++++++++++++++++++++++++++++++++++++++++++++\nPress 5 now to terminate the thread.\n");
            try {
                TimeUnit.SECONDS.sleep(seconds);
            }catch(Exception e) {
                System.out.println("Error: " + e.toString());
            }
        }
    }

    public void shutdown() {
        done = true;
    }

    public boolean isRunning() {
        return !done;
    }
}
