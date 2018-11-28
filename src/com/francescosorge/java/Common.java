package com.francescosorge.java;

import com.diogonunes.jcdp.color.ColoredPrinter;
import java.util.Scanner;

final class Common {
    static final float VERSION = 0.5f;
    static final String defaultURL = "https://tempmon.francescosorge.com";
    static final Scanner scanner = new Scanner(System.in);
    static final ColoredPrinter print = new ColoredPrinter.Builder(1, false).build();

    static String url;
    static String token;
    static boolean logCPU = false;
    static boolean logGPU = false;
    static AssociativeArray selectedDevice = null;
    static JsonFromInternet deviceSettings = null;

    private Common() { // class cannot be instantiated
    }

    static void updateDeviceSettings() throws Exception {
        try { // Tries to use access token to get user settings
            if (Common.selectedDevice != null) {
                Common.deviceSettings =  new JsonFromInternet(Common.url + "/retrieve-data?type=single-device&id=" + Common.selectedDevice.get("id") + "&token=" + Common.token);
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
            System.out.println("\tMax temperature: " + (!Common.deviceSettings.getValue("cpu-max-temperature").equals("") ? Common.deviceSettings.getValue("cpu-max-temperature") + " °C" : "CPU section disabled"));
            System.out.println("\tKill processes: " + (!Common.deviceSettings.getValue("cpu-kill-process").equals("") ? Common.deviceSettings.getValue("cpu-kill-process") : "Do nothing"));
            System.out.println("\tChange device state: " + (!Common.deviceSettings.getValue("cpu-device-state").equals("") ? Common.deviceSettings.getValue("cpu-device-state") : "Do nothing"));

            System.out.println("GPU");
            System.out.println("\tMax temperature: " + (!Common.deviceSettings.getValue("gpu-max-temperature").equals("") ? Common.deviceSettings.getValue("gpu-max-temperature") + " °C" : "GPU section disabled"));
            System.out.println("\tKill processes: " + (!Common.deviceSettings.getValue("gpu-kill-process").equals("") ? Common.deviceSettings.getValue("gpu-kill-process") : "Do nothing"));
            System.out.println("\tChange device state: " + (!Common.deviceSettings.getValue("gpu-device-state").equals("") ? Common.deviceSettings.getValue("gpu-device-state") : "Do nothing"));
        }catch(Exception e) {
            System.out.println(e.toString());
        }
    }
}
