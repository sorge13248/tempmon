package com.francescosorge.java;

import com.diogonunes.jcdp.color.api.Ansi;

import java.util.*;

public class Main {
    private static List<String> arguments;
    private static CheckStatus checkStatus = null;

    public static void main(String[] args) {
        arguments = new LinkedList<>(Arrays.asList(args));

        if (arguments.contains("--no-generic-log")) { // disabled generic logging
            Common.logGeneric = false;
        } else if (arguments.contains("--log-level") && arguments.indexOf("--log-level")+1 < arguments.size()) {
            String level = arguments.get(arguments.indexOf("--log-level")+1);
            arguments.remove(arguments.get(arguments.indexOf("--log-level")+1));
            arguments.remove(arguments.get(arguments.indexOf("--log-level")));

            try {
                short levelInt = Short.parseShort(level);
                Common.genericLogging.setLevel(Logging.Levels.values()[levelInt]);
            }catch(NumberFormatException e) {
                switch (level) {
                    case "INFO":
                        Common.genericLogging.setLevel(Logging.Levels.INFO);
                        break;
                    case "SUCCESS":
                        Common.genericLogging.setLevel(Logging.Levels.SUCCESS);
                        break;
                    case "WARNING":
                        Common.genericLogging.setLevel(Logging.Levels.WARNING);
                        break;
                    case "ERROR":
                        Common.genericLogging.setLevel(Logging.Levels.ERROR);
                        break;
                    case "FATAL_ERROR":
                        Common.genericLogging.setLevel(Logging.Levels.FATAL_ERROR);
                        break;
                }
            }

            Common.genericLogging.add(Logging.Levels.ALWAYS, "Level set to " + Common.genericLogging.getLevel());
        }

        if (Common.logGeneric) {
            Common.genericLogging.add(Logging.Levels.INFO, "TempMon started");
            Common.genericLogging.add(Logging.Levels.INFO,"Program arguments: " + String.join(" ", args));
        }

        if (!OsUtils.isWindows() && !OsUtils.isLinux() && !OsUtils.isMac()) {
            String error = "Unsupported operating system " + OsUtils.getOsName() + ". You may contact the developer and ask to add support for your operating system. Or you might ask the community, or do it yourself :)";
            System.out.println(error);
            if (Common.logGeneric) {
                Common.genericLogging.add(Logging.Levels.FATAL_ERROR, error);
            }
            System.exit(404); // unsupported operating system
        }

        if (arguments.contains("--gui")) { // GUI must be requested explicitly
            BasicWindow basicWindow = new BasicWindow("TempMon GUI");
            basicWindow.setVisible(true);
            if (Common.logGeneric) {
                Common.genericLogging.add(Logging.Levels.INFO, "GUI launched");
            }
        }

        // Header
        System.out.println("TempMon - A simple temperature monitor for your device");
        System.out.println("Developed by Francesco Sorge (www.francescosorge.com) - Version " + Common.VERSION);
        System.out.println("++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");

        // Provide URL
        boolean validURL = false;
        String URL;
        JsonFromInternet tempMonServer = null;

        do { // Ask for an URL until a valid one is provided
            if (arguments.contains("--url") && arguments.indexOf("--url")+1 < arguments.size()) {
                URL = arguments.get(arguments.indexOf("--url")+1);
                if (URL.equals("default")) {
                    URL = Common.defaultURL; // If no URL is typed, it assumes to use the default one
                }
                arguments.remove(arguments.get(arguments.indexOf("--url")+1));
                arguments.remove(arguments.get(arguments.indexOf("--url")));
            } else {
                System.out.print("TempMon server URL (default = " + Common.defaultURL + "): ");
                URL = Common.scanner.nextLine();
                if (URL.equals("")) URL = Common.defaultURL; // If no URL is typed, it assumes to use the default one
                
                if (Common.logGeneric) {
                    Common.genericLogging.add(Logging.Levels.INFO, "URL provided: " + URL);
                }
            }
            System.out.println("Trying to establish connection with TempMon server at " + URL);

            try { // Tries to connect with server
                tempMonServer = new JsonFromInternet(URL + "/sw-info.php");
                validURL = true;
                Common.url = URL;
                System.out.println("Success! Connection with TempMon server established correctly.");
                System.out.println("Server is running version " + tempMonServer.getValue("version")); // Everything went OK and server version is printed on screen

                if (Common.logGeneric) {
                    Common.genericLogging.add(Logging.Levels.SUCCESS, "Connection successful with " + URL + " with version " + tempMonServer.getValue("version"));
                }
            } catch (Exception e) {
                String error = "Error! An invalid URL has been provided.";
                Common.print.println(error, Ansi.Attribute.NONE, Ansi.FColor.NONE, Ansi.BColor.RED); // Invalid URL provided

                if (Common.logGeneric) {
                    Common.genericLogging.add(Logging.Levels.ERROR, error);
                    Common.genericLogging.add(Logging.Levels.ERROR, "Java reported: " + e.toString());
                }
                
                Common.print.clear();
                System.out.println();
            }
        }while(!validURL);

        if (Float.parseFloat(tempMonServer.getValue("version")) != Common.VERSION && !arguments.contains("--skip-update")) {
            String warning = "WARNING: Client version (" + Common.VERSION + ") and Server version (" + tempMonServer.getValue("version") + ") mismatches.";
            if (Common.logGeneric) {
                Common.genericLogging.add(Logging.Levels.WARNING, warning);
            }
            
            Common.print.println(warning, Ansi.Attribute.NONE, Ansi.FColor.BLACK, Ansi.BColor.YELLOW);
            Common.print.clear();
            System.out.print("You may encounter bugs if you continue. We suggest you to download latest versions at http://tempmon.francescosorge.com/.\nWould you like to open the web page now? [y/n]: ");
            String openNow = Common.scanner.nextLine();
            if (openNow.equals("y")) {
                try {
                    OsUtils.openInBrowser(Common.defaultURL + "/download");
                }catch(java.awt.HeadlessException e) {
                    if (Common.logGeneric) {
                        Common.genericLogging.add(Logging.Levels.ERROR, e.toString());
                    }
                    System.out.println("Error " + e.toString());
                }catch(Exception e) {
                    e.printStackTrace();
                }
            }
            System.out.println("+++++++++++++++++++++++++++++++++++++++");
        }

        System.out.println(); // Adds a blank row

        // Provide token
        boolean validToken = false;
        String token;
        JsonFromInternet tempMonSettings = new JsonFromInternet();
        do {
            if (arguments.contains("--token") && arguments.indexOf("--token")+1 < arguments.size()) {
                token = arguments.get(arguments.indexOf("--token")+1);
                arguments.remove(arguments.get(arguments.indexOf("--token")+1));
                arguments.remove(arguments.get(arguments.indexOf("--token")));
            } else {
                System.out.print("Type your access token: "); // Asks for access token
                token = Common.scanner.nextLine();
            }
            
            if (Common.logGeneric) {
                Common.genericLogging.add(Logging.Levels.INFO, "Token provided: " + token);
            }

            try { // Tries to use access token to get user settings
                tempMonSettings = new JsonFromInternet(URL + "/retrieve-data?type=list-devices&token=" + token);
            } catch (Exception e) {
                System.out.println("Error " + e);
                System.exit(-1);
            }

            if (tempMonSettings.hasKey("user_id")) { // If 'user_id' key is present, token was valid so it moves to next steps
                validToken = true;
                Common.token = token;
                if (!arguments.contains("--token")) System.out.print("Success! ");
                System.out.println("Welcome back, " + tempMonSettings.getValue("user_name") + " " + tempMonSettings.getValue("user_surname") + ".");
                if (Common.logGeneric) {
                    Common.genericLogging.add(Logging.Levels.SUCCESS, "Successful logged in with user " + tempMonSettings.getValue("user_name") + " " + tempMonSettings.getValue("user_surname"));
                }
            } else {
                String error = "Error! Invalid token provided.";
                Common.print.println(error, Ansi.Attribute.NONE, Ansi.FColor.NONE, Ansi.BColor.RED); // Token not valid
                if (Common.logGeneric) {
                    Common.genericLogging.add(Logging.Levels.ERROR, error);
                }
                Common.print.clear();
            }
        }while(!validToken);

        if (arguments.contains("--log-cpu")) {
            changeLogSettings("cpu", true);
            arguments.remove(arguments.get(arguments.indexOf("--log-cpu")));
        }

        if (arguments.contains("--log-gpu")) {
            changeLogSettings("gpu", true);
            arguments.remove(arguments.get(arguments.indexOf("--log-gpu")));
        }

        printMenu();
    }

    private static void printMenu() {
        do {
            if (Common.logGeneric) {
                Common.genericLogging.add(Logging.Levels.INFO, "printMenu() is called");
            }

            short option;
            if (arguments.contains("--menu") && arguments.indexOf("--menu")+1 < arguments.size()) {
                option = Short.parseShort(arguments.get(arguments.indexOf("--menu")+1));
                arguments.remove(arguments.get(arguments.indexOf("--menu")+1));
                arguments.remove(arguments.get(arguments.indexOf("--menu")));

                if (Common.logGeneric) {
                    Common.genericLogging.add(Logging.Levels.INFO, "Automatic option picked: " + option);
                }
            } else {
                System.out.println();
                System.out.println("Device paired: " + (Common.selectedDevice == null ? "None" : Common.selectedDevice.get("name")));
                System.out.println("Main menu");
                System.out.println("\t0. " + (Common.selectedDevice == null ? "Pair" : "Change paired") + " device");
                System.out.println("\t1. Print software settings");
                System.out.println("\t2. Print CPUs status");
                System.out.println("\t3. Print GPUs status");
                System.out.println("\t4. Print processes status");
                System.out.println("\t5. Start/stop thread (" + (checkStatus == null ? "Not running" : "Running") + ")");
                System.out.println("\t6. Logging options");
                System.out.println("\t7. Exit");
                System.out.print("Choose an option [0-7]: ");
                option = Common.scanner.nextShort();
                Common.scanner.nextLine();

                if (Common.logGeneric) {
                    Common.genericLogging.add(Logging.Levels.INFO, "Menu entry chose: " + option);
                }
            }

            switch (option) {
                case 0:
                    pairDevice();
                    break;
                case 1:
                    if (Common.selectedDevice == null) {
                        System.out.println("Cannot print device settings before you chose a device.");
                    } else {
                        Common.printDeviceSettings();
                    }
                    break;
                case 2:
                    Cpu.printComponents();
                    break;
                case 3:
                    Gpu.printComponents();
                    break;
                case 4:
                    printProcess();
                    break;
                case 5:
                    if (checkStatus == null) {
                        if (Common.selectedDevice == null) {
                            String error = "Cannot start thread before a device is paired.";
                            System.out.println(error);
                            if (Common.logGeneric) {
                                Common.genericLogging.add(Logging.Levels.ERROR, error);
                            }
                        } else {
                            checkStatus = new CheckStatus(5);
                            checkStatus.start();
                        }
                    } else {
                        checkStatus.shutdown();
                        checkStatus = null;
                    }
                    break;
                case 6:
                    logSettings();
                    break;
                case 7:
                    if (checkStatus != null) {
                        checkStatus.shutdown();
                        try {
                            checkStatus.join();
                        }catch(Exception e) {
                            System.out.println(e.toString());
                        }
                        checkStatus = null;
                    }
                    String info = "Exiting TempMon...";
                    System.out.println(info);
                    if (Common.logGeneric) {
                        Common.genericLogging.add(Logging.Levels.ERROR, info);
                    }
                    System.exit(0);
            }
        }while(true);
    }

    private static void logSettings() {
        System.out.println("Logging CPU: " + (Common.logCPU ? "Yes" : "No"));
        System.out.println("Logging GPU: " + (Common.logGPU ? "Yes" : "No"));
        System.out.print("Type EXIT to leave logging settings or type [cpu=yes] if you want to enable CPU logging, type [gpu=yes] to log GPU, type [cpu=yes] [gpu=yes] to log both. Substitute yes with no to disable logging. ");
        String logOptionsString = Common.scanner.nextLine().trim();
        if (!logOptionsString.equalsIgnoreCase("EXIT")) {
            String[] logOptions = logOptionsString.split("\\ ", -1);
            System.out.println("Log option chosen");
            for (final String option : logOptions) {
                if (option.startsWith("[cpu=")) {
                    if (option.endsWith("=yes]")) {
                        changeLogSettings("cpu", true);
                    } else if (option.endsWith("=no]")) {
                        changeLogSettings("cpu", false);
                    }
                } else if (option.startsWith("[gpu=")) {
                    if (option.endsWith("=yes]")) {
                        changeLogSettings("gpu", true);
                    } else if (option.endsWith("=no]")) {
                        changeLogSettings("gpu", false);
                    }
                }
            }
        }
    }

    private static void changeLogSettings(String component, boolean state) {
        if (component.equalsIgnoreCase("cpu")) {
            Common.logCPU = state;
        } else if (component.equalsIgnoreCase("gpu")) {
            Common.logGPU = state;
        }
        if (Common.logGeneric) {
            Common.genericLogging.add(Logging.Levels.INFO, component.substring(0, 1).toUpperCase() + component.substring(1) + " section logging " + (state ? "activated" : "deactivated"));
        }
        System.out.println(component.substring(0, 1).toUpperCase() + component.substring(1) + " log changed to " + (state ? "true" : "false"));
    }

    private static void pairDevice() {
        try {
            if (Common.logGeneric) {
                Common.genericLogging.add(Logging.Levels.INFO, "Getting device list from server");
            }
            JsonFromInternet deviceListJson = new JsonFromInternet(Common.url + "/retrieve-data?type=list-devices&token=" + Common.token);

            if (!deviceListJson.isValueNull("user_id")) {
                AssociativeArray attributes = deviceListJson.getAsIterable("String");

                if (Common.logGeneric) {
                    Common.genericLogging.add(Logging.Levels.INFO, "Device list:");
                }

                short i = 0;
                for(Map.Entry<String,Object> att : attributes.entrySet()){
                    if (att.getKey().contains("device-") && att.getKey().contains("-name")) {
                        if (!arguments.contains("--pair-device")) {
                            System.out.println(" - " + att.getValue());
                        }
                        if (Common.logGeneric) {
                            Common.genericLogging.add(Logging.Levels.INFO, att.getValue().toString());
                        }
                        i++;
                    }
                }

                if (i == 0) {
                    String error = "No device available. Go to the web interface and configure at least one device.";
                    if (Common.logGeneric) {
                        Common.genericLogging.add(Logging.Levels.ERROR, error);
                    }
                    Common.print.println(error, Ansi.Attribute.NONE, Ansi.FColor.BLACK, Ansi.BColor.YELLOW);
                    Common.print.clear();
                } else {
                    boolean exit = false;
                    do {
                        String deviceName;
                        if (arguments.contains("--pair-device") && arguments.indexOf("--pair-device")+1 < arguments.size()) {
                            deviceName = arguments.get(arguments.indexOf("--pair-device")+1);
                            arguments.remove(arguments.get(arguments.indexOf("--pair-device")+1));
                            arguments.remove(arguments.get(arguments.indexOf("--pair-device")));
                            if (Common.logGeneric) {
                                Common.genericLogging.add(Logging.Levels.INFO, "Trying to auto-pair device name " + deviceName);
                            }
                        } else {
                            System.out.print("Choose device by typing its name (or type EXIT to leave without pairing a device): ");
                            if (Common.logGeneric) {
                                Common.genericLogging.add(Logging.Levels.INFO, "Waiting for user input");
                            }
                            deviceName = Common.scanner.nextLine();
                        }
                        if (deviceName.equalsIgnoreCase("EXIT")) {
                            exit = true;
                        } else {
                            for(Map.Entry<String,Object> att : attributes.entrySet()){
                                if (att.getValue().equals(deviceName)) {
                                    Common.selectedDevice = new AssociativeArray();
                                    Common.selectedDevice.put("name", att.getValue());
                                    Common.selectedDevice.put("id", att.getKey().replace("device-", "").replace("-name", ""));
                                    exit = true;
                                    if (Common.logGeneric) {
                                        Common.genericLogging.add(Logging.Levels.SUCCESS, "Device " + deviceName + " chosen correctly");
                                    }
                                }
                            }
                            if (!exit) {
                                String error = "You typed a wrong device name '" + deviceName + "'. Try again.";
                                Common.print.println(error, Ansi.Attribute.NONE, Ansi.FColor.NONE, Ansi.BColor.RED);
                                Common.print.clear();
                                if (Common.logGeneric) {
                                    Common.genericLogging.add(Logging.Levels.ERROR, error);
                                }
                            }
                        }
                    }while(!exit);
                }
            } else {
                String error = "Something happened and we can't retrieve your device list.";
                Common.print.println(error, Ansi.Attribute.NONE, Ansi.FColor.NONE, Ansi.BColor.RED);
                Common.print.clear();
                if (Common.logGeneric) {
                    Common.genericLogging.add(Logging.Levels.ERROR, error);
                }
            }
        }catch (Exception e) {
            Common.print.println("Error " + e.toString(), Ansi.Attribute.NONE, Ansi.FColor.NONE, Ansi.BColor.RED);
            Common.print.clear();
            if (Common.logGeneric) {
                Common.genericLogging.add(Logging.Levels.ERROR, e.toString());
            }
        }
    }

    private static void printProcess() {
        try {
            if (Common.logGeneric) {
                Common.genericLogging.add(Logging.Levels.INFO, "Printing process status");
            }
            Common.updateDeviceSettings();
            TaskList taskList = new TaskList();
            String[] check = {"cpu", "gpu"};

            for (final String current : check) {
                System.out.println();
                if (Common.deviceSettings.isValueNull(current + "-max-temperature")) {
                    String info = current.toUpperCase() + " skipped because disabled";
                    System.out.println(info);
                    if (Common.logGeneric) {
                        Common.genericLogging.add(Logging.Levels.INFO, info);
                    }
                }
                if (!Common.deviceSettings.isValueNull(current + "-kill-process") && !Common.deviceSettings.isValueNull(current + "-max-temperature")) {
                    String[] processToKill = Common.deviceSettings.getValue(current + "-kill-process").split(", ");
                    String sectionName = current.toUpperCase() + " section";

                    if (Common.logGeneric) {
                        Common.genericLogging.add(Logging.Levels.INFO, sectionName);
                        Common.genericLogging.add(Logging.Levels.INFO, "Process to kill: " + String.join(", ", processToKill));
                    }

                    System.out.println(sectionName);
                    for (short i = 0; i < processToKill.length; i++) {
                        System.out.println("Process #" + i + ": " + processToKill[i]);
                        System.out.print("\tIs it running? ");

                        boolean isProcessRunning = taskList.isRunning(processToKill[i]);
                        if (isProcessRunning) {
                            Common.print.println("Yes", Ansi.Attribute.NONE, Ansi.FColor.NONE, Ansi.BColor.GREEN);
                            Common.print.clear();
                        } else {
                            Common.print.println("No", Ansi.Attribute.NONE, Ansi.FColor.NONE, Ansi.BColor.RED);
                            Common.print.clear();
                        }

                        if (Common.logGeneric) {
                            Common.genericLogging.add(Logging.Levels.INFO, "Is process " + processToKill[i] + " running? " + (isProcessRunning ? "Yes" : "No"));
                        }
                    }
                }
            }
        }catch(Exception e) {
            System.out.println(e.toString());
        }
    }
}