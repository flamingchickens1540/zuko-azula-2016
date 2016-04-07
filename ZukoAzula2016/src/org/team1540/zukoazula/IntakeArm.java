package org.team1540.zukoazula;

import ccre.behaviors.ArbitratedFloat;
import ccre.behaviors.BehaviorArbitrator;
import ccre.channel.BooleanCell;
import ccre.channel.BooleanInput;
import ccre.channel.EventLogger;
import ccre.channel.EventOutput;
import ccre.channel.FloatCell;
import ccre.channel.FloatIO;
import ccre.channel.FloatInput;
import ccre.channel.FloatOutput;
import ccre.cluck.Cluck;
import ccre.ctrl.ExtendedMotorFailureException;
import ccre.drivers.ctre.talon.TalonExtendedMotor;
import ccre.frc.FRC;
import ccre.log.LogLevel;

public class IntakeArm {
    private static final TalonExtendedMotor intakeArmCAN = FRC.talonCAN(9);
    public static final FloatIO encoder = intakeArmCAN.modEncoder().getEncoderPosition();
    private static final FloatInput outputCurrent = intakeArmCAN.modFeedback().getOutputCurrent();

    private static final FloatInput intakeArmAxis = ZukoAzula.controlBinding.addFloat("Intake Arm Axis").deadzone(0.2f).negated();
    private static final FloatInput targetArmVelocity = intakeArmAxis.multipliedBy(ZukoAzula.mainTuning.getFloat("Intake Arm Speed", .5f));

    private static final BooleanCell needsToCalibrate = new BooleanCell(true);

    private static final BehaviorArbitrator armBehaviors = new BehaviorArbitrator("Intake Arm Behaviors");
    private static final ArbitratedFloat control = armBehaviors.addFloat();

    private static final FloatInput passiveSpeed = ZukoAzula.mainTuning.getFloat("Intake Arm Counteract Gravity Speed", .06f);
    private static final FloatInput forceLowerSpeed = ZukoAzula.mainTuning.getFloat("Intake Arm Force Lower Speed", -.2f);

    private static final FloatCell autonomousVelocity = new FloatCell();
    private static BooleanInput autonomousStop;

    public static void setup() throws ExtendedMotorFailureException {
        FloatInput armPosition = encoder.normalize(ZukoAzula.mainTuning.getFloat("Intake Distance to Low Position", -3200), ZukoAzula.mainTuning.getFloat("Intake Distance to High Position", -100));
        BooleanInput tooHigh = armPosition.atLeast(1);
        BooleanInput tooLow = armPosition.atMost(0);
        BooleanInput stop = tooHigh.and(targetArmVelocity.atLeast(0)).or(tooLow.and(targetArmVelocity.atMost(0)));
        autonomousStop = tooHigh.and(autonomousVelocity.atLeast(0)).or(tooLow.and(autonomousVelocity.atMost(0)));
        
        BooleanInput forceLower = armPosition.atLeast(0.5f).and(Shooter.shouldLowerArm);

        BooleanInput calibrating = needsToCalibrate.and(FRC.inTeleopMode().or(FRC.inAutonomousMode()));
        EventOutput calibrateArms = encoder.eventSet(0).combine(needsToCalibrate.eventSet(false));
        calibrateArms.on(outputCurrent.atLeast(ZukoAzula.mainTuning.getFloat("Intake Arm Stalling Current Threshold", 4)).onPress().and(calibrating));

        control.attach(armBehaviors.addBehavior("autonomous", FRC.inAutonomousMode()), autonomousStop.toFloat(autonomousVelocity, 0));
        control.attach(armBehaviors.addBehavior("teleop", FRC.inTeleopMode()), stop.toFloat(targetArmVelocity, 0f));
        BooleanInput teleopNotMoving = targetArmVelocity.inRange(FloatInput.zero, passiveSpeed).and(FRC.inTeleopMode());
        BooleanInput autonomousNotMoving = autonomousVelocity.inRange(FloatInput.zero, passiveSpeed).or(FRC.inAutonomousMode().not());
        BooleanInput counteractGravity = armPosition.atMost(ZukoAzula.mainTuning.getFloat("Intake Arm Counter Gravity Height Threshold", .5f)).and(teleopNotMoving).and(autonomousNotMoving);
        control.attach(armBehaviors.addBehavior("counteract gravity", counteractGravity), passiveSpeed);
        control.attach(armBehaviors.addBehavior("lower for fire", counteractGravity.and(forceLower)), forceLowerSpeed);
        control.attach(armBehaviors.addBehavior("calibrating", calibrating), ZukoAzula.mainTuning.getFloat("Intake Arm Speed During Calibration", .3f));
        control.send(PowerManager.managePower(1, intakeArmCAN.simpleControl()));

        EventLogger.log(calibrating.onPress(), LogLevel.INFO, "Started intake arm calibration");
        EventLogger.log(calibrating.onRelease(), LogLevel.INFO, "Finished intake arm calibration");

        Cluck.publish("Intake Arm Calibrate", needsToCalibrate);
        Cluck.publish("Intake Arm Output Current", outputCurrent);
        Cluck.publish("Intake Arm Encoder", encoder);
        Cluck.publish("Intake Arm Position", armPosition);
    }

    public static FloatOutput getArmOutput() {
        return autonomousVelocity;
    }

    public static BooleanInput armIsStopped() {
        return autonomousStop;
    }
}
