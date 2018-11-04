package com.francescosorge.java;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.profesorfalken.jsensors.JSensors;
import com.profesorfalken.jsensors.model.components.Components;
import com.profesorfalken.jsensors.model.components.Cpu;
import com.profesorfalken.jsensors.model.components.Gpu;
import com.profesorfalken.jsensors.model.sensors.Temperature;

import java.awt.*;
import java.net.URI;
import java.util.*;
import java.util.List;

public class Main {
    private static final Scanner scanner = new Scanner(System.in);
    private static final double VERSION = 0.1;
    private static final String defaultURL = "http://localhost/temp-mon";
    private static CheckStatus checkStatus = null;
    private static AssociativeArray selectedDevice = null;
    private static String swURL = null;
    private static String swToken = null;

    public static void main(String[] args) {
        if (!OsUtils.isWindows() && !OsUtils.isLinux() && !OsUtils.isMac()) {
            System.out.println("Unsupported operating system " + OsUtils.getOsName() + ".\nYou may contact the developer and ask to add support for your operating system. Or you might ask the community, or do it yourself :)");
            System.exit(-1);
        }

        List<String> argsList = new LinkedList<>(Arrays.asList(args));;

        // Header
        System.out.println("TempMon - A simple temperature monitor for your device");
        System.out.println("Developed by Francesco Sorge (www.francescosorge.com) - Version " + VERSION);
        System.out.println("++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");

        // Provide URL
        boolean validURL = false;
        String URL = "";

        do { // Ask for an URL until a valid one is provided
            if (argsList.contains("--url") && argsList.indexOf("--url")+1 < argsList.size()) {
                URL = argsList.get(argsList.indexOf("--url")+1);
                if (URL.equals("default")) {
                    URL = defaultURL; // If no URL is typed, it assumes to use the default one
                }
                argsList.remove(argsList.get(argsList.indexOf("--url")+1));
            } else {
                System.out.print("TempMon server URL (default = " + defaultURL + "): ");
                URL = scanner.nextLine();
                if (URL.equals("")) URL = defaultURL; // If no URL is typed, it assumes to use the default one
            }
            System.out.println("Trying to establish connection with TempMon server at " + URL);

            try { // Tries to connect with server
                JsonFromInternet tempMonServer = new JsonFromInternet(URL + "/sw-info");
                validURL = true;
                swURL = URL;
                System.out.println("Success! Connection with TempMon server established correctly.");
                System.out.println("Server is running version " + tempMonServer.getValue("version")); // Everything went OK and server version is printed on screen
                if (Double.parseDouble(tempMonServer.getValue("version")) != VERSION) {
                    System.out.print("\n+++++++++++++++++++++++++++++++++++++++\nWARNING: Client version (" + VERSION + ") and Server version (" + tempMonServer.getValue("version") + ") mismatches.\nYou may encounter bugs if you continue. We suggest you to download latest versions at http://tempmon.francescosorge.com/.\nWould you like to open the web page now? [y/n]: ");
                    String openNow = scanner.nextLine();
                    if (openNow.equals("y")) {
                        if (Desktop.isDesktopSupported()) {
                            try {
                                Desktop.getDesktop().browse(new URI("http://tempmon.francescosorge.com/"));
                            } catch(Exception e) {
                                System.out.println("Error " + e.toString());
                            }
                        } else {
                            System.out.println("Cannot open default web browser. You'll have to do it manually.");
                        }
                    }
                    System.out.println("+++++++++++++++++++++++++++++++++++++++");
                }
            } catch (java.net.MalformedURLException e) {
                System.out.println("Error! An invalid URL has been provided."); // Invalid URL provided
            }
        }while(!validURL);

        System.out.println(); // Adds a blank row

        // Provide token
        boolean validToken = false;
        String token = "";
        JsonFromInternet tempMonSettings = new JsonFromInternet();
        do {
            if (argsList.contains("--token") && argsList.indexOf("--token")+1 < argsList.size()) {
                token = argsList.get(argsList.indexOf("--token")+1);
                argsList.remove(argsList.get(argsList.indexOf("--token")+1));
            } else {
                System.out.print("Type your access token: "); // Asks for access token
                token = scanner.nextLine();
            }

            try { // Tries to use access token to get user settings
                tempMonSettings = new JsonFromInternet(URL + "/retrieve-data?type=list-devices&token=" + token);
            } catch (Exception e) {
                System.out.println("Error " + e);
                System.exit(-1);
            }

            if (tempMonSettings.hasKey("user_id")) { // If 'user_id' key is present, token was valid so it moves to next steps
                validToken = true;
                swToken = token;
                if (!argsList.contains("--token")) System.out.print("Success! ");
                System.out.println("Welcome back, " + tempMonSettings.getValue("user_name") + " " + tempMonSettings.getValue("user_surname") + ".");
            } else {
                System.out.println("Error! Invalid token provided."); // Token not valid
            }
        }while(!validToken);

        printMenu();
    }

    public static void printMenu() {
        boolean exit = false;

        do {
            System.out.println();
            System.out.println("Device paired: " + (selectedDevice == null ? "None" : selectedDevice.get("name")));
            System.out.println("Main menu");
            System.out.println("\t0. " + (selectedDevice == null ? "Pair" : "Change paired") + " device");
            System.out.println("\t1. Print software settings");
            System.out.println("\t2. Print CPUs status");
            System.out.println("\t3. Print GPUs status");
            System.out.println("\t4. Print processes status");
            System.out.println("\t5. Start/stop thread (" + (checkStatus == null ? "Not running" : "Running") + ")");
            System.out.println("\t7. Exit");
            System.out.print("Choose an option [0-7]: ");
            int option = scanner.nextInt();
            scanner.nextLine();

            switch (option) {
                case 0:
                    pairDevice();
                    break;
                case 1:
                    printSoftwareSettings();
                    break;
                case 2:
                    printCpuStatus();
                    break;
                case 3:
                    printGpuStatus();
                    break;
                case 4:
                    printProcess();
                    break;
                case 5:
                    if (checkStatus == null) {
                        checkStatus = new CheckStatus();
                        checkStatus.start();
                    } else {
                        checkStatus.shutdown();
                        checkStatus = null;
                    }
                    break;
                case 7:
                    exit = true;
                    System.out.println("Exiting TempMon...");
                    System.exit(0);
                    break;
            }
        }while(!exit);
    }

    public static void pairDevice() {
        try {
            JsonFromInternet deviceListJson = new JsonFromInternet(swURL + "/retrieve-data?type=list-devices&token=" + swToken);

            if (!deviceListJson.isValueNull("user_id")) {
                AssociativeArray attributes = deviceListJson.getAsIterable("String");

                int i = 0;
                for(Map.Entry<String,Object> att : attributes.entrySet()){
                    if (att.getKey().contains("device-") && att.getKey().contains("-name")) {
                        System.out.println(" - " + att.getValue());
                        i++;
                    }
                }

                if (i == 0) {
                    System.out.println("No device available. Go to the web interface and configure at least one device.");
                } else {
                    boolean exit = false;
                    do {
                        System.out.print("Choose device by typing its name (or type EXIT to leave without pairing a device): ");
                        String deviceName = scanner.nextLine();
                        if (deviceName.equals("EXIT")) {
                            exit = true;
                        } else {
                            for(Map.Entry<String,Object> att : attributes.entrySet()){
                                if (att.getValue().equals(deviceName)) {
                                    selectedDevice = new AssociativeArray();
                                    selectedDevice.put("name", att.getValue());
                                    selectedDevice.put("id", att.getKey().replace("device-", "").replace("-name", ""));
                                    exit = true;
                                }
                            }
                            if (!exit) {
                                System.out.println("You typed a wrong device name. Try again.");
                            }
                        }
                    }while(!exit);
                }
            } else {
                System.out.println("Something happened and we can't retrieve your device list.");
            }
        }catch (Exception e) {
            System.out.println("Error " + e.toString());
        }
    }

    public static JsonFromInternet getDeviceSettings() throws Exception {
        try { // Tries to use access token to get user settings
            if (selectedDevice != null) {
                return new JsonFromInternet(swURL + "/retrieve-data?type=single-device&id=" + selectedDevice.get("id") + "&token=" + swToken);
            } else {
                throw new Exception("No device selected");
            }
        } catch (Exception e) {
            throw new Exception(e);
        }
    }

    public static boolean checkAgainstCpuTemp() throws Exception {
        try {
            JsonFromInternet deviceSettings = getDeviceSettings();

            return calculateCpuTemp("max") > Double.parseDouble(deviceSettings.getValue("cpu-max-temperature"));
        }catch(Exception e) {
            throw new Exception(e);
        }
    }

    public static void printSoftwareSettings() {
        try {
            JsonFromInternet deviceSettings = getDeviceSettings();

            System.out.println("CPU");
            System.out.println("\tMax temperature: " + (!deviceSettings.isValueNull("cpu-max-temperature") ? deviceSettings.getValue("cpu-max-temperature") + " °C" : "CPU section disabled"));
            System.out.println("\tKill processes: " + (!deviceSettings.isValueNull("cpu-kill-proccess") ? deviceSettings.getValue("cpu-kill-proccess") : "Do nothing"));
            System.out.println("\tChange device state: " + (!deviceSettings.isValueNull("cpu-device-state") ? deviceSettings.getValue("cpu-device-state") : "Do nothing"));

            System.out.println("GPU");
            System.out.println("\tMax temperature: " + (!deviceSettings.isValueNull("gpu-max-temperature") ? deviceSettings.getValue("gpu-max-temperature") + " °C" : "GPU section disabled"));
            System.out.println("\tKill processes: " + (!deviceSettings.isValueNull("gpu-kill-proccess") ? deviceSettings.getValue("gpu-kill-proccess") : "Do nothing"));
            System.out.println("\tChange device state: " + (!deviceSettings.isValueNull("gpu-device-state") ? deviceSettings.getValue("gpu-device-state") : "Do nothing"));
        }catch(Exception e) {
            System.out.println(e.toString());
        }
    }

    public static void printCpuStatus() {
        Components components = JSensors.get.components();
        List<Cpu> cpus = components.cpus;

        if (cpus != null) {
            for (final Cpu cpu : cpus) {
                System.out.println("Found CPU component: " + cpu.name);
                if (cpu.sensors != null) {
                    System.out.println("Sensors: ");

                    //Print temperatures
                    List<Temperature> temps = cpu.sensors.temperatures;
                    for (final Temperature temp : temps) {
                        System.out.println(temp.name + ": " + temp.value + " C");
                    }
                }
            }
        }
    }

    /**
     *
     * @param type: supports type "max", "min", "average", "sum"
     * @return double
     */
    public static double calculateCpuTemp(String type) {
        Components components = JSensors.get.components();
        List<Cpu> cpus = components.cpus;
        double finalTemp = 0.00d;
        if (type.equals("min")) finalTemp = 1000.00d;
        int i = 0;

        if (cpus != null) {
            for (final Cpu cpu : cpus) {
                if (cpu.sensors != null) {
                    //Print temperatures
                    List<Temperature> temps = cpu.sensors.temperatures;
                    for (final Temperature temp : temps) {
                        if (type.equals("max")) {
                            if (finalTemp < temp.value) {
                                finalTemp = temp.value;
                            }
                        } else if (type.equals("min")) {
                            if (finalTemp > temp.value) {
                                finalTemp = temp.value;
                            }
                        } else {
                            finalTemp += temp.value;
                        }
                        i++;
                    }
                }
            }
        }

        return type.equals("average") ? finalTemp / i : finalTemp;
    }

    public static double calculateCpuTemp() {
        return calculateCpuTemp("max");
    }

    public static void printGpuStatus() {
        Components components = JSensors.get.components();
        List<Gpu> gpus = components.gpus;

        if (gpus != null) {
            for (final Gpu gpu : gpus) {
                System.out.println("Found GPU component: " + gpu.name);
                if (gpu.sensors != null) {
                    System.out.println("Sensors: ");

                    //Print temperatures
                    List<Temperature> temps = gpu.sensors.temperatures;
                    for (final Temperature temp : temps) {
                        System.out.println(temp.name + ": " + temp.value + " C");
                    }
                }
            }
        }
    }

    public static void printProcess() {
        try {
            JsonFromInternet deviceSettings = getDeviceSettings();
            TaskList taskList = new TaskList();
            String[] check = {"cpu", "gpu"};

            for (final String current : check) {
                System.out.println();
                if (deviceSettings.isValueNull(current + "-max-temperature")) {
                    System.out.println(current.toUpperCase() + " skipped because disabled");
                }
                if (!deviceSettings.isValueNull(current + "-kill-proccess") && !deviceSettings.isValueNull(current + "-max-temperature")) {
                    String[] processToKill = deviceSettings.getValue(current + "-kill-proccess").split("\\, ");

                    System.out.println(current.toUpperCase());
                    for (int i = 0; i < processToKill.length; i++) {
                        System.out.println("Process #" + i + ": " + processToKill[i]);
                        System.out.print("\tIs it running? ");
                        if (taskList.isRunning(processToKill[i])) {
                            System.out.println("Yes");
                        } else {
                            System.out.println("No");
                        }
                    }
                }
            }
        }catch(Exception e) {
            System.out.println(e.toString());
        }
    }
}