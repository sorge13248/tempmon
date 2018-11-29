package com.francescosorge.java;

import java.util.ArrayList;

public class Logging {
    private String path, file;
    private ArrayList<String> content = new ArrayList<>();
    private Levels level = Levels.INFO;
    public enum Levels {
        ALWAYS,
        INFO,
        SUCCESS,
        WARNING,
        ERROR,
        FATAL_ERROR
    };

    public Logging(String path, String file, Levels level) {
        this.path = path;
        this.file = file;
        setLevel(level);
    }

    public void setLevel(Levels level) {
        if (level == Levels.ALWAYS) {
            level = Levels.INFO;
        }
        this.level = level;
    }

    public Levels getLevel() {
        return level;
    }

    public void add(Levels levels, String row) {
        if (levels.ordinal() >= level.ordinal() || levels == Levels.ALWAYS) {
            content.add("[" + Common.getTimestamp() + "] " + levels.toString().toUpperCase() + ": " + row);
            try {
                OsUtils.writeToFile(path, file, content.toArray(new String[0]));
            } catch (Exception e) {
                System.out.println(e.toString());
            }
        }
    }
}
