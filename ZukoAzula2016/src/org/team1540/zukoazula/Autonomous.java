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

    public static void setup() {
        mainModule.publishDefaultControls(true, true);
        mainModule.publishRConfControls();
        mainModule.addMode(new AutonomousModeTestSpeeds());
        mainModule.addMode(new AutonomousModeLowBar()); // Will need to be retuned after we fix the encoders
        mainModule.addMode(new AutonomousModeRockWall());
        mainModule.addMode(new AutonomousModeMoat());
        mainModule.addMode(new AutonomousModeRamparts());
        mainModule.addMode(new AutonomousModeRoughTerrain());
        mainModule.addMode(new AutonomousModeHighGoal());
        mainModule.addMode(new AutonomousModePortcullis()); // Untested
        mainModule.loadSettings(mainModule.addNullMode("none", "I'm a sitting chicken!"));
        FRC.registerAutonomous(mainModule);
    }
}
