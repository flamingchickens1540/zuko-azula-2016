package org.team1540.zukoazula;

import ccre.behaviors.Behavior;
import ccre.behaviors.BehaviorArbitrator;
import ccre.channel.BooleanCell;
import ccre.channel.BooleanOutput;
import ccre.channel.FloatOutput;
import ccre.cluck.Cluck;
import ccre.ctrl.Drive;
import ccre.ctrl.ExtendedMotorFailureException;
import ccre.ctrl.binding.ControlBindingCreator;
import ccre.frc.FRC;
import ccre.frc.FRCApplication;
import ccre.log.Logger;
import ccre.tuning.TuningContext;

public class ZukoAzula implements FRCApplication {

    public static final ControlBindingCreator controlBinding = FRC.controlBinding();
    public static final TuningContext mainTuning = new TuningContext("MainTuning").publishSavingEvent();

    public static final BehaviorArbitrator behaviors = new BehaviorArbitrator("Behaviors");
    public static final Behavior teleop = behaviors.addBehavior("Teleop", FRC.inTeleopMode());
    private static final BooleanCell pitModeEnable = new BooleanCell();
    public static final Behavior pit = behaviors.addBehavior("Pit Mode", pitModeEnable.andNot(FRC.isOnFMS()));

    @Override
    public void setupRobot() throws ExtendedMotorFailureException {
        Logger.info("🐣 CHEEP CHEEP 🐣");

        DriveCode.setup();
        Shooter.setup();

        Cluck.publish("Pit Mode Enable", pitModeEnable);
    }
}
