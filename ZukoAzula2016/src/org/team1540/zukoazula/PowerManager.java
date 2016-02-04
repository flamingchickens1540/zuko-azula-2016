package org.team1540.zukoazula;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import ccre.channel.FloatCell;
import ccre.channel.FloatIO;
import ccre.channel.FloatOutput;
import ccre.cluck.Cluck;
import ccre.ctrl.ExtendedMotor;
import ccre.ctrl.ExtendedMotorFailureException;
import ccre.frc.FRC;

public class PowerManager {
    private static Map<Integer, List<FloatCell>> filters;

    public static FloatCell spikePeak = new FloatCell(150);
    public static FloatCell spikeLength = new FloatCell(2);

    public static FloatOutput managePower(int priority, FloatOutput motor) {
        filters.get(priority).add(new FloatCell(1));
        int index = filters.get(priority).size();
        return (float value) -> {
            motor.set(filters.get(priority).get(index).get());
        };
    }

    public static void setupPowerManager() {
        Cluck.publish("[Power] Spike Peak", spikePeak);
        Cluck.publish("[Power] Spike Length", spikeLength);

        FRC.voltagePDP().send((float current) -> { // XXX: should be current
            boolean spiking = current > spikePeak.get(); // XXX: should be over
                                                         // time
            if (spiking) {
                for (Map.Entry<Integer, List<FloatCell>> partition : filters.entrySet()) {
                    float reduction = priorityToReduction(partition.getKey());
                    for (FloatCell filter : partition.getValue()) {
                        filter.set(filter.get() - reduction);
                    }
                }
            }
        });
    }
    
    private static float priorityToReduction(int priority) {
        return (float)Math.pow(priority * (100/5), 2);
    }
}

/// shooter top priority
/// drive motors not so much
/// intake last priority