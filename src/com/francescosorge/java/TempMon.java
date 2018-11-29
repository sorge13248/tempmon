package com.francescosorge.java;

import com.diogonunes.jcdp.color.ColoredPrinter;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;

final class TempMon {
    static final float VERSION = 0.8f;
    static final String defaultURL = "https://tempmon.francescosorge.com";
    static final ColoredPrinter print = new ColoredPrinter.Builder(1, false).build();

    static String url;
    static String token;
    static boolean logGeneric = true;
    static boolean logCPU = false;
    static boolean logGPU = false;
    static AssociativeArray selectedDevice = null;
    static JsonFromInternet deviceSettings = null;

    static Logging genericLogging = new Logging("log", Common.getTimestamp("yyyy-MM-dd_HH-mm-ss") + ".txt", Logging.Levels.INFO);


    private TempMon() { // class cannot be instantiated
    }

    static void updateDeviceSettings() throws Exception {
        try { // Tries to use access token to get user settings
            if (TempMon.selectedDevice != null) {
                TempMon.deviceSettings =  new JsonFromInternet(TempMon.url + "/retrieve-data?type=single-device&id=" + TempMon.selectedDevice.get("id") + "&token=" + TempMon.token);
            } else {
                throw new Exception("No device selected");
            }
        } catch (Exception e) {
            throw new Exception(e);
        }
    }

    static void printDeviceSettings() {
        try {
            updateDeviceSettings();

            System.out.println("CPU");
            System.out.println("\tMax temperature: " + (!TempMon.deviceSettings.getValue("cpu-max-temperature").equals("") ? TempMon.deviceSettings.getValue("cpu-max-temperature") + " °C" : "CPU section disabled"));
            System.out.println("\tKill processes: " + (!TempMon.deviceSettings.getValue("cpu-kill-process").equals("") ? TempMon.deviceSettings.getValue("cpu-kill-process") : "Do nothing"));
            System.out.println("\tChange device state: " + (!TempMon.deviceSettings.getValue("cpu-device-state").equals("") ? TempMon.deviceSettings.getValue("cpu-device-state") : "Do nothing"));

            System.out.println("GPU");
            System.out.println("\tMax temperature: " + (!TempMon.deviceSettings.getValue("gpu-max-temperature").equals("") ? TempMon.deviceSettings.getValue("gpu-max-temperature") + " °C" : "GPU section disabled"));
            System.out.println("\tKill processes: " + (!TempMon.deviceSettings.getValue("gpu-kill-process").equals("") ? TempMon.deviceSettings.getValue("gpu-kill-process") : "Do nothing"));
            System.out.println("\tChange device state: " + (!TempMon.deviceSettings.getValue("gpu-device-state").equals("") ? TempMon.deviceSettings.getValue("gpu-device-state") : "Do nothing"));
        }catch(Exception e) {
            System.out.println(e.toString());
        }
    }
}
