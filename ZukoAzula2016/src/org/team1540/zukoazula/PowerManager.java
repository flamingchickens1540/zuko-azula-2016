package org.team1540.zukoazula;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import ccre.channel.BooleanCell;
import ccre.channel.BooleanInput;
import ccre.channel.EventOutput;
import ccre.channel.FloatCell;
import ccre.channel.FloatIO;
import ccre.channel.FloatInput;
import ccre.channel.FloatOutput;
import ccre.cluck.Cluck;
import ccre.ctrl.ExtendedMotor;
import ccre.ctrl.ExtendedMotorFailureException;
import ccre.frc.FRC;
import ccre.log.Logger;
import ccre.timers.StopwatchTimer;

public class PowerManager {
    private static Map<Integer, List<FloatCell>> filters = new HashMap<Integer, List<FloatCell>>();
    private static StopwatchTimer timer = new StopwatchTimer();

    public static FloatCell spikePeak = ZukoAzula.mainTuning.getFloat("[Power] Spike Peak", 50);
    public static FloatCell spikeLength = ZukoAzula.mainTuning.getFloat("[Power] Spike Length", 2);
    public static FloatCell degree = ZukoAzula.mainTuning.getFloat("[Power] Degree of Reduction", (float) 1.3);
    public static FloatCell scalingFactor = ZukoAzula.mainTuning.getFloat("[Power] Scaling Factor", 8);

    public static FloatOutput managePower(int priority, FloatOutput motor) {
        FloatCell filter = new FloatCell(1);

        if (filters.get(priority) == null) {
            filters.put(priority, new ArrayList<FloatCell>());
        }

        filters.get(priority).add(filter);
        return (float value) -> {
            motor.set(value * filter.get());
        };
    }

    public static void setup() {
        BooleanInput spiking = FRC.totalCurrentPDP().atLeast(spikePeak);
        BooleanInput timerFinished = timer.atLeast(spikeLength);
        spiking.onPress().send(() -> timer.reset());
        BooleanInput currentlySpiking = PIDTalon.getThreeState(spiking.and(timerFinished), spiking.not());

        BooleanCell enablePowerManagement = ZukoAzula.mainTuning.getBoolean("[Power] Enable Power Management", true);
        FRC.sensorPeriodic.and(enablePowerManagement).send(() -> {
            int direction = currentlySpiking.get() ? -1 : 1;
            for (Map.Entry<Integer, List<FloatCell>> partition : filters.entrySet()) {
                float reduction = priorityToReduction(partition.getKey());
                for (FloatCell filter : partition.getValue()) {
                    filter.set(Math.max(Math.min(filter.get() + (reduction * direction), 1), 0));
                }
            }
        });
    }

    private static float priorityToReduction(int priority) {
        // We reduce the filter on a polynomial curve based on the priority
        // compared to the highest priority, scaled to a small percentage.
        int highestPriority = 0;
        for (Map.Entry<Integer, List<FloatCell>> partition : filters.entrySet()) {
            highestPriority = Math.max(partition.getKey(), highestPriority);
        }

        return (float) (Math.pow((highestPriority + 1) - priority, degree.get()) / (Math.pow(highestPriority, 2) * scalingFactor.get()));
    }
}

/// shooter top priority
/// drive motors not so much
/// intake last priority
