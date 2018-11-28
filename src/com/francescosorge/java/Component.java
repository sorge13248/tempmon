package com.francescosorge.java;

import com.profesorfalken.jsensors.JSensors;
import com.profesorfalken.jsensors.model.sensors.Sensors;
import com.profesorfalken.jsensors.model.sensors.Temperature;

import java.util.List;

class Component {
    private Component() {
    }

    static void printSensors(Sensors sensors) {
        if (sensors != null) {
            System.out.println("Sensors: ");

            //Print temperatures
            List<Temperature> temps = sensors.temperatures;
            for (final Temperature temp : temps) {
                System.out.println(temp.name + ": " + temp.value + " C");
            }
        }
    }

    static double calculateTemp(Sensors sensors, String type) {
        double finalTemp = 0.00d;
        if (type.equals("min")) finalTemp = 1000.00d;
        short i = 0;

        if (sensors != null) {
            //Print temperatures
            List<Temperature> temps = sensors.temperatures;
            for (final Temperature temp : temps) {
                if (type.equals("max")) {
                    if (finalTemp < temp.value) {
                        finalTemp = temp.value;
                    }
                } else if (type.equals("min")) {
                    if (finalTemp > temp.value) {
                        finalTemp = temp.value;
                    }
                } else {
                    finalTemp += temp.value;
                }
                i++;
            }
        }

        return type.equals("average") ? finalTemp / i : finalTemp;
    }
}
