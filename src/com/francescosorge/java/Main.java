package com.francescosorge.java;

import com.profesorfalken.jsensors.JSensors;
import com.profesorfalken.jsensors.model.components.Components;
import com.profesorfalken.jsensors.model.components.Cpu;
import com.profesorfalken.jsensors.model.components.Gpu;
import com.profesorfalken.jsensors.model.sensors.Fan;
import com.profesorfalken.jsensors.model.sensors.Temperature;

import java.util.List;
import java.util.Scanner;

public class Main {
    private static final Scanner scanner = new Scanner(System.in);
    private static final double VERSION = 0.1;
    private static final String defaultURL = "http://localhost/temp-mon";

    public static void main(String[] args) {
        // Header
        System.out.println("TempMob - A simple temperature monitor for your device");
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
            } else {
                System.out.println("Error! Invalid token provided."); // Token not valid
            }
        }while(!validToken);

        System.out.println(tempMonSettings.toString());
        System.out.println(tempMonSettings.getValue("cpu-max-temperature"));
        System.exit(0);


        Components components = JSensors.get.components();

        List<Cpu> cpus = components.cpus;
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
}