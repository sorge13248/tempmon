package com.francescosorge.java;

import java.time.Instant;
import java.util.concurrent.TimeUnit;

import static java.time.temporal.ChronoUnit.HOURS;

public class CheckStatus extends Thread {
    private volatile boolean done = false;
    private volatile int seconds = 1;
    private volatile int cycle = -1;
    private volatile boolean sendEmail = false;
    private volatile AssociativeArray emailValues = null;
    private volatile Instant timestamp = null;

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
        if (TempMon.logGeneric) {
            TempMon.genericLogging.add(Logging.Levels.INFO, "Thread started");
        }
        while (!done) {
            cycle++;
            sendEmail = false;
            emailValues = new AssociativeArray();
            if (timestamp == null) timestamp = java.time.Instant.now();

            try {
                //TempMon.updateDeviceSettings();
            } catch (Exception e) {

            }
            if (TempMon.logGeneric) {
                TempMon.genericLogging.add(Logging.Levels.INFO, "++++++++++++ Cycle #" + cycle + " ++++++++++++");
                TempMon.genericLogging.add(Logging.Levels.INFO, Common.getTimestamp() + "");
                TempMon.genericLogging.add(Logging.Levels.INFO, "Server url: " + TempMon.url);
                TempMon.genericLogging.add(Logging.Levels.INFO, "User token: " + TempMon.token);
                //System.out.println("Fetched from server:");
                //TempMon.printDeviceSettings();
            }

            // CPU SECTION
            try {
                if (TempMon.logGeneric) {
                    TempMon.genericLogging.add(Logging.Levels.INFO, "[CPU SECTION]");
                }

                checkComponents("cpu");
            }catch(Exception e) {
                if (TempMon.logGeneric) {
                    TempMon.genericLogging.add(Logging.Levels.ERROR, e.toString());
                }
            }

            // GPU SECTION
            try {
                if (TempMon.logGeneric) {
                    TempMon.genericLogging.add(Logging.Levels.INFO, "[GPU SECTION]");
                }

                checkComponents("gpu");
            }catch(Exception e) {
                if (TempMon.logGeneric) {
                    TempMon.genericLogging.add(Logging.Levels.ERROR, e.toString());
                }
            }

            // EMAIL SECTION
            try {
                if (TempMon.logEmail) {
                    TempMon.genericLogging.add(Logging.Levels.INFO, "[EMAIL SECTION]");
                }

                if (TempMon.deviceSettings.hasKey("report-each-x-hours") && timestamp.plus(Integer.parseInt(TempMon.deviceSettings.getValue("report-each-x-hours-select").replace(" hours", "")), HOURS).compareTo(Instant.now()) < 1) {
                    timestamp = null;
                    sendEmail = true;
                }

                if (sendEmail) {
                    if (TempMon.logEmail) {
                        TempMon.genericLogging.add(Logging.Levels.INFO, "Calling sendEmail()");
                    }
                    sendEmail();
                } else {
                    if (TempMon.logEmail) {
                        TempMon.genericLogging.add(Logging.Levels.INFO, "No need to send e-mail.");
                    }
                }
            }catch(Exception e) {
                if (TempMon.logGeneric) {
                    TempMon.genericLogging.add(Logging.Levels.ERROR, e.toString());
                }
            }
        }
        try {
            TimeUnit.SECONDS.sleep(seconds);
        }catch(Exception e) {
            if (TempMon.logGeneric) {
                TempMon.genericLogging.add(Logging.Levels.ERROR, e.toString());
            }
        }
    }

    public synchronized void sendEmail() throws Exception {
        if (TempMon.logEmail) {
            TempMon.genericLogging.add(Logging.Levels.INFO, "Contacting server...");
        }

        JsonFromInternet sendEmail = new JsonFromInternet(TempMon.url + "/send-email?device_id=" + TempMon.selectedDevice.get("id") + "&token=" + TempMon.token + (emailValues.containsKey("cpu") ? "&cpu=" + emailValues.get("cpu") : "") + (emailValues.containsKey("gpu") ? "&gpu=" + emailValues.get("gpu") : ""));
        if (sendEmail.hasKey("response")) {
            if (sendEmail.getValue("response").equalsIgnoreCase("success")) {
                if (TempMon.logEmail) {
                    TempMon.genericLogging.add(Logging.Levels.SUCCESS, "Server contacted successfully for sending email.");
                }
            } else {
                if (TempMon.logEmail) {
                    TempMon.genericLogging.add(Logging.Levels.ERROR, "Server responded: " + sendEmail.getValue("response"));
                }
            }
        } else {
            if (TempMon.logEmail) {
                TempMon.genericLogging.add(Logging.Levels.ERROR, "Something went wrong contacting server for sending email.");
            }
        }
    }

    public synchronized void checkComponents(String component) throws Exception {
        boolean validComponent = false;
        boolean log = true;
        boolean overheated = false;

        switch (component) {
            case "cpu":
                validComponent = true;
                log = TempMon.logCPU;
                overheated = Cpu.isOverheated();
                break;
            case "gpu":
                validComponent = true;
                log = TempMon.logGPU;
                overheated = Gpu.isOverheated();
                break;
        }

        if (!validComponent) {
            if (TempMon.logGeneric || log) {
                String error = "Invalid component provided: " + component;
                if (TempMon.logGeneric) {
                    TempMon.genericLogging.add(Logging.Levels.ERROR, error);
                }
                if (log) {
                    System.out.println(error);
                }
            }
        } else {
            double maxTemp = 100.00d;
            if (!TempMon.deviceSettings.getValue(component + "-max-temperature").equals("")) {
                if (TempMon.logGeneric || log) {
                    if (component.equalsIgnoreCase("cpu")) {
                        maxTemp = Cpu.calculateTemp("max");
                    } else if (component.equalsIgnoreCase("gpu")) {
                        maxTemp = Gpu.calculateTemp("max");
                    }

                    String info = "Current " + component + " temperature (max): " + maxTemp;
                    if (TempMon.logGeneric) {
                        TempMon.genericLogging.add(Logging.Levels.INFO, info);
                    }
                    if (log) {
                        System.out.println(info);
                    }

                    String infoOverheated = "Is " + component + " overheated? " + (overheated ? "Yes" : "No");
                    if (TempMon.logGeneric) {
                        TempMon.genericLogging.add(Logging.Levels.INFO, infoOverheated);
                    }
                    if (log) {
                        System.out.println(infoOverheated);
                    }
                }

                if (overheated) {
                    sendEmail = true;
                    emailValues.put(component, maxTemp);

                    String processToKill = null;
                    if (!TempMon.deviceSettings.getValue(component + "-kill-process").equals("")) {
                        processToKill = TempMon.deviceSettings.getValue(component + "-kill-process");
                    }

                    if (TempMon.logGeneric || log) {
                        String info = "Process to kill: " + (processToKill == null ? "Do nothing" : processToKill);
                        if (TempMon.logGeneric) {
                            TempMon.genericLogging.add(Logging.Levels.INFO, info);
                        }
                        if (log) {
                            System.out.println(info);
                        }
                    }

                    String[] processToKillArray = TempMon.deviceSettings.getValue(component + "-kill-process").split(", ");
                    for (int i = 0; i < processToKillArray.length; i++) {
                        try {
                            OsUtils.killProcess(processToKillArray[i]);
                        } catch (Exception e) {
                            System.out.println(e.toString());
                        }
                    }

                    String action = null;
                    if (!TempMon.deviceSettings.getValue(component + "-device-state").equals("")) {
                        action = TempMon.deviceSettings.getValue(component + "-device-state");
                    }
                    if (TempMon.logGeneric || log) {
                        String info = "Action to do: " + (action == null ? "Nothing" : action);
                        if (TempMon.logGeneric) {
                            TempMon.genericLogging.add(Logging.Levels.INFO, info);
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
                if (TempMon.logGeneric || log) {
                    String warning = component + " section is disabled by current device settings.";
                    if (TempMon.logGeneric) {
                        TempMon.genericLogging.add(Logging.Levels.WARNING, warning);
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
        if (TempMon.logGeneric) {
            TempMon.genericLogging.add(Logging.Levels.INFO, "Thread stopped");
        }
    }

    public boolean isRunning() {
        return !done;
    }
}
