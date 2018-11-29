package com.francescosorge.java;

import java.util.ArrayList;

public class Logging {
    private String path, file;
    private ArrayList<String> content = new ArrayList<>();

    public Logging(String path, String file) {
        this.path = path;
        this.file = file;
    }

    public void add(String type, String row) {
        content.add("[" + Common.getTimestamp() + "] " + type.toUpperCase() + ": " + row);
        try {
            OsUtils.writeToFile(path, file, content.toArray(new String[0]));
        }catch(Exception e) {
            System.out.println(e.toString());
        }
    }
}
