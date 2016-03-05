package org.team1540.zukoazula;

import ccre.ctrl.ExtendedMotorFailureException;
import ccre.ctrl.binding.ControlBindingCreator;
import ccre.frc.FRC;
import ccre.frc.FRCApplication;
import ccre.log.Logger;
import ccre.tuning.TuningContext;

public class ZukoAzula implements FRCApplication {

    public static final ControlBindingCreator controlBinding = FRC.controlBinding();
    public static final TuningContext mainTuning = new TuningContext("MainTuning").publishSavingEvent();

    @Override
    public void setupRobot() throws ExtendedMotorFailureException {
        Logger.info("🐣 CHEEP CHEEP 🐣");

        DriveCode.setup();
        Autonomous.setup();
        Shooter.setup();
        Portcullis.setup();
        IntakeArm.setup();
        ChallengeBrake.setup();
    }
}
