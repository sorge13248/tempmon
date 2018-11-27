package com.francescosorge.java;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class CheckStatus extends Thread {
    private volatile boolean done = false;
    private volatile int seconds = 1;
    private volatile JsonFromInternet settings = null;

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

            try {
                settings = Main.getDeviceSettings();
            }catch(Exception e) {

            }

            System.out.println(formatter.format(date) + "");
            System.out.println("Server url: " + Settings.url);
            System.out.println("User token: " + Settings.token);
            System.out.println("Fetched from server:");
            Main.printSoftwareSettings();

            // CPU SECTION
            try {
                checkComponents("cpu");
            }catch(Exception e) {
                System.out.println(e.toString());
            }

            // GPU SECTION
            try {
                checkComponents("gpu");
            }catch(Exception e) {
                System.out.println(e.toString());
            }
        }

        System.out.println("+++++++++++++++++++++++++++++++++++++++++++++\nPress 5 now to terminate the thread.\n");
        try {
            TimeUnit.SECONDS.sleep(seconds);
        }catch(Exception e) {
            System.out.println("Error: " + e.toString());
        }
    }

    public void checkComponents(String component) throws Exception {
        boolean validComponent = false;
        boolean log = false;
        boolean overheated = false;

        switch (component) {
            case "cpu":
                validComponent = true;
                log = Settings.logCPU;
                overheated = Main.checkAgainstCpuTemp();
                break;
            case "gpu":
                validComponent = true;
                log = Settings.logGPU;
                overheated = Main.checkAgainstGpuTemp();
                break;
        }

        if (!validComponent) {
            System.out.println("Invalid component '" + component + "' provided.");
        } else {
            if (!settings.getValue(component + "-max-temperature").equals("")) {
                if (log) {
                    System.out.println("Current " + component + " temperature (max): " + Main.calculateGpuTemp("max"));
                }

                if (log) {
                    System.out.println("Is " + component + " overheated? " + (overheated ? "Yes" : "No"));
                }

                if (overheated) {
                    String processToKill = null;
                    if (!settings.getValue(component + "-kill-process").equals("")) {
                        processToKill = settings.getValue(component + "-kill-process");
                    }

                    if (log) {
                        System.out.println("Process to kill: " + (processToKill == null ? "Do nothing" : processToKill));
                    }

                    String[] processToKillArray = settings.getValue(component + "-kill-process").split(", ");
                    for (int i = 0; i < processToKillArray.length; i++) {
                        try {
                            OsUtils.killProcess(processToKillArray[i]);
                        } catch (Exception e) {
                            System.out.println(e.toString());
                        }
                    }

                    String action = null;
                    if (!settings.getValue(component + "-device-state").equals("")) {
                        action = settings.getValue(component + "-device-state");
                    }
                    if (log) {
                        System.out.println("Action to intraprend: " + (action == null ? "Do nothing" : action));
                    }
                }
            } else {
                if (Settings.logGPU) {
                    System.out.println("GPU section is disabled by current device settings.");
                }
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
