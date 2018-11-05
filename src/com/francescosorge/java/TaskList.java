package com.francescosorge.java;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.util.LinkedList;

public class TaskList {
    private LinkedList<String> commandOutput = new LinkedList<>();

    public TaskList() {
        this.update();
    }

    public void update() {
        try {
            Process p;
            if (OsUtils.isWindows()) {
                p = Runtime.getRuntime().exec(System.getenv("windir") + "\\system32\\" + "tasklist.exe");
            } else if (OsUtils.isLinux()) {
                p = Runtime.getRuntime().exec("ps -A -o comm");
            } else {
                throw new Exception("Unsupported operating system");
            }

            BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));

            String line;
            while ((line = input.readLine()) != null) {
                commandOutput.add(line);
            }

            input.close();
        }catch(Exception e) {
            System.out.println("Error: " + e.toString());
            e.printStackTrace();
        }
    }

    public String getCommandOutput() {
        return this.commandOutput.toString();
    }

    public boolean isRunning(String processName) {
        if (OsUtils.isWindows()) {
            boolean found = false;
            try {
                File file = File.createTempFile("realhowto",".vbs");
                file.deleteOnExit();
                FileWriter fw = new java.io.FileWriter(file);

                String vbs = "Set WshShell = WScript.CreateObject(\"WScript.Shell\")\n"
                        + "Set locator = CreateObject(\"WbemScripting.SWbemLocator\")\n"
                        + "Set service = locator.ConnectServer()\n"
                        + "Set processes = service.ExecQuery _\n"
                        + " (\"select * from Win32_Process where name='" + processName +"'\")\n"
                        + "For Each process in processes\n"
                        + "wscript.echo process.Name \n"
                        + "Next\n"
                        + "Set WSHShell = Nothing\n";

                fw.write(vbs);
                fw.close();
                Process p = Runtime.getRuntime().exec("cscript //NoLogo " + file.getPath());
                BufferedReader input =
                        new BufferedReader
                                (new InputStreamReader(p.getInputStream()));
                String line;
                line = input.readLine();
                if (line != null) {
                    if (line.equals(processName)) {
                        found = true;
                    }
                }
                input.close();

            }
            catch(Exception e){
                e.printStackTrace();
            }
            return found;
        } else {
            return commandOutput.contains(processName);
        }
    }
}
