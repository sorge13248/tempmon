package com.francescosorge.java;

import com.profesorfalken.jsensors.JSensors;
import com.profesorfalken.jsensors.model.components.Components;
import com.profesorfalken.jsensors.model.components.Cpu;
import com.profesorfalken.jsensors.model.components.Gpu;
import com.profesorfalken.jsensors.model.sensors.Fan;
import com.profesorfalken.jsensors.model.sensors.Temperature;

import java.awt.*;
import java.net.URI;
import java.util.List;
import java.util.Scanner;

public class Main {
    private static final Scanner scanner = new Scanner(System.in);
    private static final double VERSION = 0.1;
    private static final String defaultURL = "http://localhost/temp-mon";

    public static void main(String[] args) {
        if (!OsUtils.isWindows() && !OsUtils.isLinux() && !OsUtils.isMac()) {
            System.out.println("Unsupported operating system " + OsUtils.getOsName() + ".\nYou may contact the developer and ask to add support for your operating system. Or you might ask the community, or do it yourself :)");
            System.exit(-1);
        }

        // Header
        System.out.println("TempMon - A simple temperature monitor for your device");
        System.out.println("Developed by Francesco Sorge (www.francescosorge.com) - Version " + VERSION);
        System.out.println("++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");

        // Provide URL
        boolean validURL = false;
        String URL;

        do { // Ask for an URL until a valid one is provided
            System.out.print("TempMon server URL (default = " + defaultURL + "): ");
            URL = scanner.nextLine();
            if (URL.equals("")) URL = defaultURL; // If no URL is typed, it assumes to use the default one
            System.out.println("Trying to establish connection with TempMon server at " + URL);

            try { // Tries to connect with server
                JsonFromInternet tempMonServer = new JsonFromInternet(URL + "/sw-info");
                validURL = true;
                System.out.println("Success! Connection with TempMon server established correctly.");
                System.out.println("Server is running version " + tempMonServer.getValue("version")); // Everything went OK and server version is printed on screen
                if (Double.parseDouble(tempMonServer.getValue("version")) != VERSION) {
                    System.out.print("\n+++++++++++++++++++++++++++++++++++++++\nWARNING: Client version (" + VERSION + ") and Server version (" + tempMonServer.getValue("version") + ") mismatches.\nYou may encounter bugs if you continue. We suggest you to download latest versions at http://tempmon.francescosorge.com/.\nWould you like to open the web page now? [y/n] ");
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
        JsonFromInternet tempMonSettings = new JsonFromInternet();
        do {
            System.out.print("Type your access token: "); // Asks for access token
            String token = scanner.nextLine();

            try { // Tries to use access token to get user settings
                tempMonSettings = new JsonFromInternet(URL + "/retrieve-data?token=" + token);
            } catch (Exception e) {
                System.out.println("Error " + e);
                System.exit(-1);
            }

            if (tempMonSettings.hasKey("user_id")) { // If 'user_id' key is present, token was valid so it moves to next steps
                validToken = true;
                System.out.println("Success! Welcome back, " + tempMonSettings.getValue("user_name") + " " + tempMonSettings.getValue("user_surname") + ".");
            } else {
                System.out.println("Error! Invalid token provided."); // Token not valid
            }
        }while(!validToken);

        printMenu(tempMonSettings);
    }

    public static void printMenu(JsonFromInternet tempMonSettings) {
        boolean exit = false;

        do {
            System.out.println();
            System.out.println("Main menu\nChoose an option");
            System.out.println("\t1. Print software settings");
            System.out.println("\t2. Print CPUs status");
            System.out.println("\t3. Print GPUs status");
            System.out.println("\t4. Print process status");
            System.out.println("\t5. Exit");
            int option = scanner.nextInt();
            scanner.nextLine();

            switch (option) {
                case 1:
                    printSoftwareSettings(tempMonSettings);
                    break;
                case 2:
                    printCpuStatus();
                    break;
                case 3:
                    printGpuStatus();
                    break;
                case 4:
                    printProcess(tempMonSettings);
                    break;
                case 5:
                    exit = true;
                    System.out.println("Exiting TempMon...");
                    break;
            }
        }while(!exit);
    }

    public static void printSoftwareSettings(JsonFromInternet tempMonSettings) {
        System.out.println("CPU");
        System.out.println("\tMax temperature: " + (!tempMonSettings.isValueNull("cpu-max-temperature") ? tempMonSettings.getValue("cpu-max-temperature") : "CPU section disabled"));
        System.out.println("\tKill processes: " + (!tempMonSettings.isValueNull("cpu-kill-proccess") ? tempMonSettings.getValue("cpu-kill-proccess") : "Do nothing"));
        System.out.println("\tChange device state: " + (!tempMonSettings.isValueNull("cpu-device-state") ? tempMonSettings.getValue("cpu-device-state") : "Do nothing"));
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

                    //Print fan speed
                    List<Fan> fans = cpu.sensors.fans;
                    for (final Fan fan : fans) {
                        System.out.println(fan.name + ": " + fan.value + " RPM");
                    }
                }
            }
        }
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

    public static void printProcess(JsonFromInternet tempMonSettings) {
        TaskList taskList = new TaskList();

        if (!tempMonSettings.isValueNull("cpu-kill-proccess")) {
            String[] processToKill = tempMonSettings.getValue("cpu-kill-proccess").split(", ");

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
}