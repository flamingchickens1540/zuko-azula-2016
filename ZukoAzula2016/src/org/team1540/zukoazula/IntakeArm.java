package org.team1540.zukoazula;

import ccre.behaviors.ArbitratedFloat;
import ccre.behaviors.Behavior;
import ccre.behaviors.BehaviorArbitrator;
import ccre.channel.BooleanCell;
import ccre.channel.BooleanIO;
import ccre.channel.BooleanInput;
import ccre.channel.EventInput;
import ccre.channel.EventLogger;
import ccre.channel.EventOutput;
import ccre.channel.FloatCell;
import ccre.channel.FloatIO;
import ccre.channel.FloatInput;
import ccre.channel.FloatOutput;
import ccre.cluck.Cluck;
import ccre.ctrl.ExtendedMotor;
import ccre.ctrl.ExtendedMotorFailureException;
import ccre.drivers.ctre.talon.TalonExtendedMotor;
import ccre.frc.FRC;
import ccre.instinct.InstinctModule;
import ccre.log.LogLevel;

public class IntakeArm {
    private static final TalonExtendedMotor intakeArmCAN = FRC.talonCAN(9);
    private static final FloatIO encoder = intakeArmCAN.modEncoder().getEncoderPosition();
    private static final FloatInput outputCurrent = intakeArmCAN.modFeedback().getOutputCurrent();

    private static final FloatInput intakeArmAxis = ZukoAzula.controlBinding.addFloat("Intake Arm Axis").deadzone(0.2f).negated();
    private static final FloatInput targetArmVelocity = intakeArmAxis.multipliedBy(ZukoAzula.mainTuning.getFloat("Intake Arm Speed", .5f));

    private static final BooleanCell needsToCalibrate = new BooleanCell(true);

    private static final BehaviorArbitrator armBehaviors = new BehaviorArbitrator("Intake Arm Behaviors");
    private static final ArbitratedFloat control = armBehaviors.addFloat();

    private static final FloatInput passiveSpeed = ZukoAzula.mainTuning.getFloat("Intake Arm Counteract Gravity Speed", .1f);

    public static void setup() throws ExtendedMotorFailureException {
        FloatInput armPosition = encoder.normalize(ZukoAzula.mainTuning.getFloat("Intake Distance to Low Position", -2760), ZukoAzula.mainTuning.getFloat("Intake Distance to High Position", -100));
        BooleanInput tooHigh = armPosition.atLeast(1);
        BooleanInput tooLow = armPosition.atMost(0);

        BooleanInput stop = tooHigh.and(targetArmVelocity.atLeast(0)).or(tooLow.and(targetArmVelocity.atMost(0)));
        BooleanInput autonomousStop = tooHigh.and(Autonomous.intakeArm.atLeast(0)).or(tooLow.and(Autonomous.intakeArm.atMost(0)));
        autonomousStop.send(Autonomous.intakeArmStopped);

        BooleanInput calibrating = needsToCalibrate.and(FRC.inTeleopMode().or(FRC.inAutonomousMode()));
        EventOutput calibrateArms = encoder.eventSet(0).combine(needsToCalibrate.eventSet(false));
        calibrateArms.on(outputCurrent.atLeast(ZukoAzula.mainTuning.getFloat("Intake Arm Stalling Current Threshold", 4)).onPress().and(calibrating));

        control.attach(armBehaviors.addBehavior("autonomous", FRC.inAutonomousMode()), autonomousStop.toFloat(Autonomous.intakeArm, 0));
        control.attach(armBehaviors.addBehavior("teleop", FRC.inTeleopMode()), stop.toFloat(targetArmVelocity, 0f));
        control.attach(armBehaviors.addBehavior("counteract gravity", armPosition.atMost(.5f).and(targetArmVelocity.inRange(FloatInput.zero, passiveSpeed))), passiveSpeed);
        control.attach(armBehaviors.addBehavior("calibrating", calibrating), ZukoAzula.mainTuning.getFloat("Intake Arm Speed During Calibration", .3f));
        control.send(intakeArmCAN.simpleControl());

        EventLogger.log(calibrating.onPress(), LogLevel.INFO, "Started intake arm calibration");
        EventLogger.log(calibrating.onRelease(), LogLevel.INFO, "Finished intake arm calibration");

        Cluck.publish("Intake Arm Calibrate", needsToCalibrate.eventSet(true));
        Cluck.publish("Intake Arm Set High Point", calibrateArms);
        Cluck.publish("Intake Arm Output Current", outputCurrent);
        Cluck.publish("Intake Arm Encoder", encoder);
        Cluck.publish("Intake Arm Position", armPosition);
    }
}
