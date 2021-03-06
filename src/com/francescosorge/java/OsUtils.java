package com.francescosorge.java;

import java.awt.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;

public final class OsUtils {
    private static final double VERSION = 1.3;

    private static String OS = null;

    private OsUtils(){
    }

    static String getOsName() {
        if(OS == null) { OS = System.getProperty("os.name"); }
        return OS;
    }
    static boolean isWindows() {
        return getOsName().toLowerCase().contains("win");
    }

    static boolean isLinux() {
        return (getOsName().toLowerCase().contains("nix") || getOsName().toLowerCase().contains("nux") || getOsName().toLowerCase().contains("aix"));
    }

    static boolean isMac() {
        return getOsName().toLowerCase().contains("mac");
    }

    public static boolean isSolaris() {
        return getOsName().toLowerCase().contains("sunos");
    }

    static int openInBrowser(String URL) throws Exception {
        try {
            Desktop.getDesktop().browse(new URI(URL));
            return 0;
        }catch(Exception e) {
            if (OsUtils.isLinux()) {
                Process p = Runtime.getRuntime().exec("xdg-open " + URL);
                p.waitFor();
                return p.exitValue();
            } else {
                return 5; // The action failed.
            }
        }
    }

    static boolean executeCommand(String command) throws Exception {
        try {
            Runtime.getRuntime().exec(command);
            return true;
        }catch(Exception e) {
            return false;
        }
    }

    static boolean killProcess(String processName) throws Exception {
        String command = "";
        if (isWindows()) {
            command = "taskkill /F /IM " + processName;
        } else if (isLinux()) {
            command = "pkill -f " + processName;
        }
        return executeCommand(command);
    }

    static boolean changeDeviceStatus(String state) throws Exception {
        String command = "echo 'Empty command'";
        if (isWindows()) {
            switch (state) {
                case "shutdown":
                    command = "shutdown /s";
                    break;
                case "reboot":
                    command = "shutdown /r";
                    break;
                case "hibernate":
                    command = "shutdown /h";
                    break;
                case "standby":
                    command = "psshutdown -d -t 0";
                    break;
                case "logoff":
                    command = "shutdown /I";
                    break;
            }
        } else if (isLinux()) {
            throw new Exception("Not implemented yet.");
        }
        return executeCommand(command);
    }

    static void writeToFile(String path, String file, String[] content) throws IOException {
        File folder = new File(path);
        if (!folder.exists()) {
            folder.mkdirs();
        }
        BufferedWriter output = new BufferedWriter(new FileWriter(path + File.separator + file));
        for (short i = 0; i < content.length; i++) {
            output.write(content[i]);
            output.newLine();
        }
        output.flush();
        output.close();
    }
}