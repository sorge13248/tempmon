package com.francescosorge.java;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

public class CheckStatus extends Thread {
    private volatile boolean done = false;
    private long seconds = 1L;

    public CheckStatus() {
    }

    public CheckStatus(long seconds) {
        this.seconds = seconds;
    }

    @Override
    public synchronized void run() {
        super.run();
        while (!done) {
            SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            Date date = new Date();
            System.out.println(formatter.format(date));
            try {
                TimeUnit.SECONDS.sleep(seconds);
            }catch(Exception e) {
                System.out.println("Error: " + e.toString());
            }
        }
    }

    public void shutdown() {
        done = true;
    }

    public boolean isRunning() {
        return !done;
    }
}
