package org.team1540.zukoazula;

import ccre.behaviors.ArbitratedFloat;
import ccre.behaviors.Behavior;
import ccre.behaviors.BehaviorArbitrator;
import ccre.channel.BooleanCell;
import ccre.channel.BooleanIO;
import ccre.channel.BooleanInput;
import ccre.channel.EventInput;
import ccre.channel.EventOutput;
import ccre.channel.FloatCell;
import ccre.channel.FloatInput;
import ccre.channel.FloatOutput;
import ccre.cluck.Cluck;
import ccre.ctrl.ExtendedMotor;
import ccre.ctrl.ExtendedMotorFailureException;
import ccre.drivers.ctre.talon.TalonExtendedMotor;
import ccre.frc.FRC;
import ccre.instinct.InstinctModule;
import ccre.log.Logger;

public class IntakeArm {
    private static final TalonExtendedMotor intakeArmCAN = FRC.talonCAN(9);
    private static final FloatInput encoder = intakeArmCAN.modEncoder().getEncoderPosition();
    private static final FloatInput outputCurrent = intakeArmCAN.modFeedback().getOutputCurrent();

    private static final FloatInput intakeArmAxis = ZukoAzula.controlBinding.addFloat("Intake Arm Axis").deadzone(0.2f).negated();

    private static final BooleanCell calibrationEnabled = ZukoAzula.mainTuning.getBoolean("Intake Should Calibrate", true);
    private static final BooleanCell needsToCalibrate = new BooleanCell(calibrationEnabled.get());

    private static final FloatCell armLow = ZukoAzula.mainTuning.getFloat("Intake Arm Low Position", 0f);
    private static final FloatCell armHigh = ZukoAzula.mainTuning.getFloat("Intake Arm High Position", 1f);

    private static final BehaviorArbitrator armBehaviors = new BehaviorArbitrator("Intake Arm Behaviors");
    private static final ArbitratedFloat control = armBehaviors.addFloat();

    public static void setup() throws ExtendedMotorFailureException {
        FloatInput armPosition = encoder.normalize(armLow, armHigh);
        BooleanInput tooHigh = armPosition.atLeast(1).and(intakeArmAxis.atLeast(0));
        BooleanInput tooLow = armPosition.atMost(0).and(intakeArmAxis.atMost(0));
        BooleanInput stop = tooHigh.or(tooLow);

        control.attach(armBehaviors.addBehavior("teleop", FRC.inTeleopMode()), stop.toFloat(intakeArmAxis.multipliedBy(ZukoAzula.mainTuning.getFloat("Intake Arm Speed", .25f)), 0f));

        FloatCell controlDuringCalibration = new FloatCell();
        control.attach(armBehaviors.addBehavior("calibrating", needsToCalibrate), controlDuringCalibration);

        FloatCell calibrationSpeed = ZukoAzula.mainTuning.getFloat("Intake Arm Speed During Calibration", .1f);
        FloatCell threshold = ZukoAzula.mainTuning.getFloat("Intake Arm Stalling Current Threshold", .5f);

        EventOutput calibrateArms = armHigh.eventSet(encoder).combine(armLow.eventSet(encoder.minus(ZukoAzula.mainTuning.getFloat("Intake Distance Between Encoders", 20))).combine(needsToCalibrate.eventSet(false)));

        control.send(intakeArmCAN.simpleControl());

        new InstinctModule(needsToCalibrate.and(FRC.robotEnabled()).and(FRC.inAutonomousMode().or(FRC.inTeleopMode()))) {
            @Override
            protected void autonomousMain() throws Throwable {
                try {
                    Logger.info("Started Calibrating");
                    controlDuringCalibration.set(calibrationSpeed.get());
                    waitUntilAtMost(outputCurrent, threshold.get());
                } finally {
                    controlDuringCalibration.set(0);
                    calibrateArms.event();
                    Logger.info("Finished Calibrating");
                }
            }
        };

        Cluck.publish("Intake Arm Set Low Position", armLow.eventSet(encoder).combine(needsToCalibrate.eventSet(false)));
        Cluck.publish("Intake Arm Set High Position", armHigh.eventSet(encoder).combine(needsToCalibrate.eventSet(false)));
        Cluck.publish("Intake Arm Calibrate", calibrateArms);
        Cluck.publish("Intake Arm Output Current", outputCurrent);
        Cluck.publish("Intake Arm Encoder", encoder);
        Cluck.publish("Intake Arm Position", armPosition);
    }
}
