package com.francescosorge.java;

import com.diogonunes.jcdp.color.api.Ansi;
import java.util.Arrays;
import java.util.Map;
import java.util.List;
import java.util.LinkedList;

public class Main {
    private static List<String> arguments;
    private static CheckStatus checkStatus = null;

    public static void main(String[] args) {
        if (!OsUtils.isWindows() && !OsUtils.isLinux() && !OsUtils.isMac()) {
            System.out.println("Unsupported operating system " + OsUtils.getOsName() + ".\nYou may contact the developer and ask to add support for your operating system. Or you might ask the community, or do it yourself :)");
            System.exit(-1);
        }

        arguments = new LinkedList<>(Arrays.asList(args));

        if (arguments.contains("--gui")) { // GUI must be requested explicitely
            BasicWindow basicWindow = new BasicWindow("TempMon GUI");
            basicWindow.setVisible(true);
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
            }
            System.out.println("Trying to establish connection with TempMon server at " + URL);

            try { // Tries to connect with server
                tempMonServer = new JsonFromInternet(URL + "/sw-info.php");
                validURL = true;
                Common.url = URL;
                System.out.println("Success! Connection with TempMon server established correctly.");
                System.out.println("Server is running version " + tempMonServer.getValue("version")); // Everything went OK and server version is printed on screen
            } catch (Exception e) {
                System.out.println(e.toString());
                Common.print.println("Error! An invalid URL has been provided.", Ansi.Attribute.NONE, Ansi.FColor.NONE, Ansi.BColor.RED); // Invalid URL provided
                Common.print.clear();
                System.out.println();
            }
        }while(!validURL);

        if (Double.parseDouble(tempMonServer.getValue("version")) != Common.VERSION && !arguments.contains("--skip-update")) {
            Common.print.println("WARNING: Client version (" + Common.VERSION + ") and Server version (" + tempMonServer.getValue("version") + ") mismatches.", Ansi.Attribute.NONE, Ansi.FColor.BLACK, Ansi.BColor.YELLOW);
            Common.print.clear();
            System.out.print("You may encounter bugs if you continue. We suggest you to download latest versions at http://tempmon.francescosorge.com/.\nWould you like to open the web page now? [y/n]: ");
            String openNow = Common.scanner.nextLine();
            if (openNow.equals("y")) {
                try {
                    OsUtils.openInBrowser(Common.defaultURL + "/download");
                }catch(java.awt.HeadlessException e) {
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
            } else {
                Common.print.println("Error! Invalid token provided.", Ansi.Attribute.NONE, Ansi.FColor.NONE, Ansi.BColor.RED); // Token not valid
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
            short option;
            if (arguments.contains("--menu") && arguments.indexOf("--menu")+1 < arguments.size()) {
                option = Short.parseShort(arguments.get(arguments.indexOf("--menu")+1));
                arguments.remove(arguments.get(arguments.indexOf("--menu")+1));
                arguments.remove(arguments.get(arguments.indexOf("--menu")));
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
                            System.out.println("Cannot start thread before a device is paired.");
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
                    System.out.println("Exiting TempMon...");
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
        System.out.println(component.substring(0, 1).toUpperCase() + component.substring(1) + " log changed to " + (state ? "true" : "false"));
    }

    private static void pairDevice() {
        try {
            JsonFromInternet deviceListJson = new JsonFromInternet(Common.url + "/retrieve-data?type=list-devices&token=" + Common.token);

            if (!deviceListJson.isValueNull("user_id")) {
                AssociativeArray attributes = deviceListJson.getAsIterable("String");

                short i = 0;
                for(Map.Entry<String,Object> att : attributes.entrySet()){
                    if (att.getKey().contains("device-") && att.getKey().contains("-name")) {
                        if (!arguments.contains("--pair-device")) {
                            System.out.println(" - " + att.getValue());
                        }
                        i++;
                    }
                }

                if (i == 0) {
                    Common.print.println("No device available. Go to the web interface and configure at least one device.", Ansi.Attribute.NONE, Ansi.FColor.BLACK, Ansi.BColor.YELLOW);
                    Common.print.clear();
                } else {
                    boolean exit = false;
                    do {
                        String deviceName;
                        if (arguments.contains("--pair-device") && arguments.indexOf("--pair-device")+1 < arguments.size()) {
                            deviceName = arguments.get(arguments.indexOf("--pair-device")+1);
                            arguments.remove(arguments.get(arguments.indexOf("--pair-device")+1));
                            arguments.remove(arguments.get(arguments.indexOf("--pair-device")));
                        } else {
                            System.out.print("Choose device by typing its name (or type EXIT to leave without pairing a device): ");
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
                                }
                            }
                            if (!exit) {
                                Common.print.println("You typed a wrong device name. Try again.", Ansi.Attribute.NONE, Ansi.FColor.NONE, Ansi.BColor.RED);
                                Common.print.clear();
                            }
                        }
                    }while(!exit);
                }
            } else {
                Common.print.println("Something happened and we can't retrieve your device list.", Ansi.Attribute.NONE, Ansi.FColor.NONE, Ansi.BColor.RED);
                Common.print.clear();
            }
        }catch (Exception e) {
            Common.print.println("Error " + e.toString(), Ansi.Attribute.NONE, Ansi.FColor.NONE, Ansi.BColor.RED);
            Common.print.clear();
        }
    }

    private static void printProcess() {
        try {
            Common.updateDeviceSettings();
            TaskList taskList = new TaskList();
            String[] check = {"cpu", "gpu"};

            for (final String current : check) {
                System.out.println();
                if (Common.deviceSettings.isValueNull(current + "-max-temperature")) {
                    System.out.println(current.toUpperCase() + " skipped because disabled");
                }
                if (!Common.deviceSettings.isValueNull(current + "-kill-process") && !Common.deviceSettings.isValueNull(current + "-max-temperature")) {
                    String[] processToKill = Common.deviceSettings.getValue(current + "-kill-process").split(", ");

                    System.out.println(current.toUpperCase());
                    for (short i = 0; i < processToKill.length; i++) {
                        System.out.println("Process #" + i + ": " + processToKill[i]);
                        System.out.print("\tIs it running? ");
                        if (taskList.isRunning(processToKill[i])) {
                            Common.print.println("Yes", Ansi.Attribute.NONE, Ansi.FColor.NONE, Ansi.BColor.GREEN);
                            Common.print.clear();
                        } else {
                            Common.print.println("No", Ansi.Attribute.NONE, Ansi.FColor.NONE, Ansi.BColor.RED);
                            Common.print.clear();
                        }
                    }
                }
            }
        }catch(Exception e) {
            System.out.println(e.toString());
        }
    }
}