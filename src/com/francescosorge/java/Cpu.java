package com.francescosorge.java;

import com.profesorfalken.jsensors.JSensors;

import java.util.List;

class Cpu {
    static void printComponents() {
        List<com.profesorfalken.jsensors.model.components.Cpu> cpus = JSensors.get.components().cpus;

        if (cpus != null) {
            for (final com.profesorfalken.jsensors.model.components.Cpu cpu : cpus) {
                System.out.println("Found CPU component: " + cpu.name);

                Component.printSensors(cpu.sensors);
            }
        }
    }

    /**
     *
     * @param type: supports type "max", "min", "average", "sum"
     * @return double
     */
    static double calculateTemp(String type) {
        List<com.profesorfalken.jsensors.model.components.Cpu> cpus = JSensors.get.components().cpus;
        double finalTemp = 0.00d;
        if (type.equals("min")) finalTemp = 1000.00d;
        int i = 0;

        if (cpus != null) {
            for (final com.profesorfalken.jsensors.model.components.Cpu cpu : cpus) {
                if (type.equals("max")) {
                    if (finalTemp < Component.calculateTemp(cpu.sensors, type)) {
                        finalTemp = Component.calculateTemp(cpu.sensors, type);
                    }
                } else if (type.equals("min")) {
                    if (finalTemp > Component.calculateTemp(cpu.sensors, type)) {
                        finalTemp = Component.calculateTemp(cpu.sensors, type);
                    }
                } else {
                    finalTemp += Component.calculateTemp(cpu.sensors, type);
                }
                i++;
            }
        }

        return type.equals("average") ? finalTemp / i : finalTemp;
    }

    static double calculateTemp() {
        return calculateTemp("max");
    }

    static boolean isOverheated() throws Exception {
        try {
            //Common.updateDeviceSettings();

            return calculateTemp("max") > Double.parseDouble(Common.deviceSettings.getValue("cpu-max-temperature"));
        }catch(Exception e) {
            throw new Exception(e);
        }
    }
}
