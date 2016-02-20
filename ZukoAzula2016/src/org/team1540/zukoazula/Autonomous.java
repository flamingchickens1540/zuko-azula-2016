package org.team1540.zukoazula;

import ccre.channel.BooleanCell;
import ccre.channel.EventCell;
import ccre.channel.FloatCell;
import ccre.channel.FloatOutput;
import ccre.frc.FRC;
import ccre.instinct.InstinctMultiModule;
import ccre.tuning.TuningContext;

public class Autonomous {
    public static final TuningContext autoTuning = new TuningContext("AutonomousTuning").publishSavingEvent();

    public static final InstinctMultiModule mainModule = new InstinctMultiModule(autoTuning);

    public static final FloatCell rightMotors = new FloatCell();
    public static final FloatCell leftMotors = new FloatCell();
    public static final FloatOutput allMotors = rightMotors.combine(leftMotors);
    public static final FloatOutput turnMotors = leftMotors.combine(rightMotors.negate());
    public static final FloatCell driveEncoder = new FloatCell();

    public static final BooleanCell warmup = new BooleanCell();
    public static final BooleanCell fire = new BooleanCell();

    public static final FloatCell intakeArm = new FloatCell();
    public static final BooleanCell intakeArmStopped = new BooleanCell();

    public static void setup() {
        mainModule.publishDefaultControls(true, true);
        mainModule.publishRConfControls();
        mainModule.loadSettings(mainModule.addNullMode("none", "I'm a sitting chicken!"));
        FRC.registerAutonomous(mainModule);
    }
}
