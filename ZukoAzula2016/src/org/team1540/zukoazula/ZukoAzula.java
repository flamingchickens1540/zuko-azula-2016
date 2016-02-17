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

<<<<<<< Upstream, based on master
    public static final BehaviorArbitrator behaviors = new BehaviorArbitrator("Behaviors");
    public static final Behavior autonomous = behaviors.addBehavior("Teleop", FRC.inAutonomousMode());
    public static final Behavior teleop = behaviors.addBehavior("Teleop", FRC.inTeleopMode());
    private static final BooleanCell pitModeEnable = new BooleanCell();
    public static final Behavior pit = behaviors.addBehavior("Pit Mode", pitModeEnable.andNot(FRC.isOnFMS()));

=======
>>>>>>> 4269209 Reorgainzed portcullis code and added encoder limits
    @Override
    public void setupRobot() throws ExtendedMotorFailureException {
        Logger.info("🐣 CHEEP CHEEP 🐣");

        DriveCode.setup();
        Autonomous.setup();
        Shooter.setup();
        Portcullis.setup();
        IntakeArm.setup();
    }
}
