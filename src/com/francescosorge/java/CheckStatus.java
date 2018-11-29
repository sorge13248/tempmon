package com.francescosorge.java;

import java.util.concurrent.TimeUnit;

public class CheckStatus extends Thread {
    private volatile boolean done = false;
    private volatile int seconds = 1;
    private volatile int cycle = -1;

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
        if (Common.logGeneric) {
            Common.genericLogging.add(Logging.Levels.INFO, "Thread started");
        }
        while (!done) {
            cycle++;
            try {
                //Common.updateDeviceSettings();
            } catch (Exception e) {

            }
            if (Common.logGeneric) {
                Common.genericLogging.add(Logging.Levels.INFO, "++++++++++++ Cycle #" + cycle + " ++++++++++++");
                Common.genericLogging.add(Logging.Levels.INFO, Common.getTimestamp() + "");
                Common.genericLogging.add(Logging.Levels.INFO, "Server url: " + Common.url);
                Common.genericLogging.add(Logging.Levels.INFO, "User token: " + Common.token);
                //System.out.println("Fetched from server:");
                //Common.printDeviceSettings();
            }

            // CPU SECTION
            try {
                if (Common.logCPU) {
                    System.out.println("\n[CPU SECTION]");
                }
                if (Common.logGeneric) {
                    Common.genericLogging.add(Logging.Levels.INFO, "[CPU SECTION]");
                }

                checkComponents("cpu");
            }catch(Exception e) {
                if (Common.logGeneric) {
                    Common.genericLogging.add(Logging.Levels.ERROR, e.toString());
                }
            }

            // GPU SECTION
            try {
                if (Common.logCPU) {
                    System.out.println("\n[GPU SECTION]");
                }
                if (Common.logGeneric) {
                    Common.genericLogging.add(Logging.Levels.INFO, "[GPU SECTION]");
                }

                checkComponents("gpu");
            }catch(Exception e) {
                if (Common.logGeneric) {
                    Common.genericLogging.add(Logging.Levels.ERROR, e.toString());
                }
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
        boolean log = true;
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
            if (Common.logGeneric || log) {
                String error = "Invalid component provided: " + component;
                if (Common.logGeneric) {
                    Common.genericLogging.add(Logging.Levels.ERROR, error);
                }
                if (log) {
                    System.out.println(error);
                }
            }
        } else {
            if (!Common.deviceSettings.getValue(component + "-max-temperature").equals("")) {
                if (Common.logGeneric || log) {
                    double maxTemp = 100.00d;
                    if (component.equalsIgnoreCase("cpu")) {
                        maxTemp = Cpu.calculateTemp("max");
                    } else if (component.equalsIgnoreCase("gpu")) {
                        maxTemp = Gpu.calculateTemp("max");
                    }

                    String info = "Current " + component + " temperature (max): " + maxTemp;
                    if (Common.logGeneric) {
                        Common.genericLogging.add(Logging.Levels.INFO,  info);
                    }
                    if (log) {
                        System.out.println(info);
                    }

                    String infoOverheated = "Is " + component + " overheated? " + (overheated ? "Yes" : "No");
                    if (Common.logGeneric) {
                        Common.genericLogging.add(Logging.Levels.INFO,  infoOverheated);
                    }
                    if (log) {
                        System.out.println(infoOverheated);
                    }
                }

                if (overheated) {
                    String processToKill = null;
                    if (!Common.deviceSettings.getValue(component + "-kill-process").equals("")) {
                        processToKill = Common.deviceSettings.getValue(component + "-kill-process");
                    }

                    if (Common.logGeneric || log) {
                        String info = "Process to kill: " + (processToKill == null ? "Do nothing" : processToKill);
                        if (Common.logGeneric) {
                            Common.genericLogging.add(Logging.Levels.INFO,  info);
                        }
                        if (log) {
                            System.out.println(info);
                        }
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
                    if (Common.logGeneric || log) {
                        String info = "Action to do: " + (action == null ? "Nothing" : action);
                        if (Common.logGeneric) {
                            Common.genericLogging.add(Logging.Levels.INFO,  info);
                        }
                        if (log) {
                            System.out.println(info);
                        }
                    }
                    if (action != null) {
                        OsUtils.changeDeviceStatus(action);
                    }
                }
            } else {
                if (Common.logGeneric || log) {
                    String warning = component + " section is disabled by current device settings.";
                    if (Common.logGeneric) {
                        Common.genericLogging.add(Logging.Levels.WARNING, warning);
                    }
                    if (log) {
                        System.out.println(warning);
                    }
                }
            }
        }
    }

    public void shutdown() {
        done = true;
        if (Common.logGeneric) {
            Common.genericLogging.add(Logging.Levels.INFO, "Thread stopped");
        }
    }

    public boolean isRunning() {
        return !done;
    }
}
