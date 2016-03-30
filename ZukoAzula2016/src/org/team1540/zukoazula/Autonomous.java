package org.team1540.zukoazula;

import ccre.channel.BooleanCell;
import ccre.channel.EventCell;
import ccre.channel.FloatCell;
import ccre.channel.FloatInput;
import ccre.channel.FloatOutput;
import ccre.cluck.Cluck;
import ccre.frc.FRC;
import ccre.instinct.InstinctMultiModule;
import ccre.timers.ExpirationTimer;
import ccre.tuning.TuningContext;

public class Autonomous {
    public static final TuningContext autoTuning = new TuningContext("AutonomousTuning").publishSavingEvent();
    private static final FloatInput spikeLevel = autoTuning.getFloat("Autonomous Abort Threshold", 60.0f);

    public static final InstinctMultiModule mainModule = new InstinctMultiModule(autoTuning);

    public static void setup() {
        mainModule.publishDefaultControls(true, true);
        mainModule.publishRConfControls();
        // Will need to be retuned after we fix the encoders
        mainModule.addMode(new AutonomousModeLowBar());
        mainModule.addMode(new AutonomousModeLowBarAutoShoot());
        mainModule.addMode(new AutonomousModeRockWall());
        mainModule.addMode(new AutonomousModeMoat());
        mainModule.addMode(new AutonomousModeRamparts());
        mainModule.addMode(new AutonomousModeRoughTerrain());
        mainModule.addMode(new AutonomousModePortcullis()); // Untested
        mainModule.loadSettings(mainModule.addNullMode("none", "I'm a sitting chicken!"));
        Cluck.publish("(TEST) SPIKE CUR", DriveCode.maximumCurrent);
        ExpirationTimer spikeChecker = new ExpirationTimer();
        DriveCode.maximumCurrent.atLeast(spikeLevel).send(spikeChecker.getRunningControl());
        spikeChecker.schedule(500, mainModule::abortMode);
        FRC.registerAutonomous(mainModule);
    }
}
