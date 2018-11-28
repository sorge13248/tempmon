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

            try {
                //Common.updateDeviceSettings();
            }catch(Exception e) {

            }

            System.out.println(formatter.format(date) + "");
            System.out.println("Server url: " + Common.url);
            System.out.println("User token: " + Common.token);
            System.out.println("Fetched from server:");
            Common.printDeviceSettings();

            // CPU SECTION
            try {
                checkComponents("cpu");
            }catch(Exception e) {
                e.printStackTrace();
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

    public synchronized void checkComponents(String component) throws Exception {
        boolean validComponent = false;
        boolean log = false;
        boolean overheated = false;

        switch (component) {
            case "cpu":
                validComponent = true;
                log = Common.logCPU;
                overheated = Cpu.isOverheated();
                break;
            case "gpu":
                validComponent = true;
                log = Common.logGPU;
                overheated = Gpu.isOverheated();
                break;
        }

        if (!validComponent) {
            System.out.println("Invalid component '" + component + "' provided.");
        } else {
            if (!Common.deviceSettings.getValue(component + "-max-temperature").equals("")) {
                if (log) {
                    double maxTemp = 100.00d;
                    if (component.equalsIgnoreCase("cpu")) {
                        maxTemp = Cpu.calculateTemp("max");
                    } else if (component.equalsIgnoreCase("gpu")) {
                        maxTemp = Gpu.calculateTemp("max");
                    }
                    System.out.println("Current " + component + " temperature (max): " + maxTemp);
                }

                if (log) {
                    System.out.println("Is " + component + " overheated? " + (overheated ? "Yes" : "No"));
                }

                if (overheated) {
                    String processToKill = null;
                    if (!Common.deviceSettings.getValue(component + "-kill-process").equals("")) {
                        processToKill = Common.deviceSettings.getValue(component + "-kill-process");
                    }

                    if (log) {
                        System.out.println("Process to kill: " + (processToKill == null ? "Do nothing" : processToKill));
                    }

                    String[] processToKillArray = Common.deviceSettings.getValue(component + "-kill-process").split(", ");
                    for (int i = 0; i < processToKillArray.length; i++) {
                        try {
                            OsUtils.killProcess(processToKillArray[i]);
                        } catch (Exception e) {
                            System.out.println(e.toString());
                        }
                    }

                    String action = null;
                    if (!Common.deviceSettings.getValue(component + "-device-state").equals("")) {
                        action = Common.deviceSettings.getValue(component + "-device-state");
                    }
                    if (log) {
                        System.out.println("Action to intraprend: " + (action == null ? "Do nothing" : action));
                    }
                }
            } else {
                if (Common.logGPU) {
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
