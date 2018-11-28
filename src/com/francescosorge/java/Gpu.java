package com.francescosorge.java;

import com.profesorfalken.jsensors.JSensors;

import java.util.List;

class Gpu {
    static void printComponents() {
        List<com.profesorfalken.jsensors.model.components.Gpu> gpus = JSensors.get.components().gpus;

        if (gpus != null) {
            for (final com.profesorfalken.jsensors.model.components.Gpu gpu : gpus) {
                System.out.println("Found GPU component: " + gpu.name);

                Component.printSensors(gpu.sensors);
            }
        }
    }

    /**
     *
     * @param type: supports type "max", "min", "average", "sum"
     * @return double
     */
    static double calculateTemp(String type) {
        List<com.profesorfalken.jsensors.model.components.Gpu> gpus = JSensors.get.components().gpus;
        double finalTemp = 0.00d;
        if (type.equals("min")) finalTemp = 1000.00d;
        short i = 0;

        if (gpus != null) {
            for (final com.profesorfalken.jsensors.model.components.Gpu gpu : gpus) {
                if (type.equals("max")) {
                    if (finalTemp < Component.calculateTemp(gpu.sensors, type)) {
                        finalTemp = Component.calculateTemp(gpu.sensors, type);
                    }
                } else if (type.equals("min")) {
                    if (finalTemp > Component.calculateTemp(gpu.sensors, type)) {
                        finalTemp = Component.calculateTemp(gpu.sensors, type);
                    }
                } else {
                    finalTemp += Component.calculateTemp(gpu.sensors, type);
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

            return calculateTemp("max") > Double.parseDouble(Common.deviceSettings.getValue("gpu-max-temperature"));
        }catch(Exception e) {
            throw new Exception(e);
        }
    }
}
