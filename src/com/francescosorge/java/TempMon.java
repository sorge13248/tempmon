package com.francescosorge.java;

import com.diogonunes.jcdp.color.ColoredPrinter;

final class TempMon {
    static final float VERSION = 1.0f;
    static final String defaultURL = "https://tempmon.francescosorge.com";
    static final ColoredPrinter print = new ColoredPrinter.Builder(1, false).build();

    static String url;
    static String token;
    static boolean logGeneric = true;
    static boolean logCPU = false;
    static boolean logGPU = false;
    static boolean logEmail = false;
    static AssociativeArray selectedDevice = null;
    static JsonFromInternet deviceSettings = null;
    static boolean guiEnabled = false;

    static Logging genericLogging = new Logging("log", Common.getTimestamp("yyyy-MM-dd_HH-mm-ss") + ".txt");


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

            System.out.println("Email");
            System.out.println("\tReport each X hours: " + (TempMon.deviceSettings.hasKey("report-each-x-hours") ? "Yes, each " + TempMon.deviceSettings.getValue("report-each-x-hours-select") : "No"));
            System.out.println("\tAlert when CPU gets critical: " + (TempMon.deviceSettings.hasKey("alert-when-cpu-critical") ? "Yes" : "No"));
            System.out.println("\tAlert when GPU gets critical: " + (TempMon.deviceSettings.hasKey("alert-when-gpu-critical") ? "Yes" : "No"));
        }catch(Exception e) {
            System.out.println(e.toString());
        }
    }
}
