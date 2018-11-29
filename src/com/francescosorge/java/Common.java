package com.francescosorge.java;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;

final class Common {
    public static final double VERSION = 1.1;

    static final Scanner scanner = new Scanner(System.in);

    private Common() { // class cannot be instantiated
    }

    static String getTimestamp(String format) {
        return new SimpleDateFormat(format).format(new Date());
    }

    static String getTimestamp() {
        return getTimestamp("dd/MM/yyyy HH:mm:ss");
    }

    static boolean classExists(String className) {
        try  {
            Class.forName(className);
            return true;
        }  catch (ClassNotFoundException e) {
            return false;
        }
    }
}
