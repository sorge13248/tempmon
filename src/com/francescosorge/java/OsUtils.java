package com.francescosorge.java;

import java.awt.*;
import java.net.URI;

public final class OsUtils {
    private static String OS = null;

    public static String getOsName() {
        if(OS == null) { OS = System.getProperty("os.name"); }
        return OS;
    }
    public static boolean isWindows() {
        return getOsName().toLowerCase().contains("win");
    }

    public static boolean isLinux() {
        return (getOsName().toLowerCase().contains("nix") || getOsName().toLowerCase().contains("nux") || getOsName().toLowerCase().contains("aix"));
    }

    public static boolean isMac() {
        return getOsName().toLowerCase().contains("mac");
    }

    public static boolean isSolaris() {
        return getOsName().toLowerCase().contains("sunos");
    }

    public static int openInBrowser(String URL) throws Exception {
        try {
            Desktop.getDesktop().browse(new URI(URL));
            return 0;
        }catch(Exception e) {
            if (OsUtils.isLinux()) {
                Process p = Runtime.getRuntime().exec("xdg-open " + URL);
                p.waitFor();
                return p.exitValue();
            } else {
                System.out.println("Cannot open default web browser. You'll have to do it manually.");
                return 5; // The action failed.
            }
        }
    }
}