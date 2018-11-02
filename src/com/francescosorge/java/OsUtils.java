package com.francescosorge.java;

public final class OsUtils
{
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
}