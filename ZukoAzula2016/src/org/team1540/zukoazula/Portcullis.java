package org.team1540.zukoazula;

import ccre.behaviors.ArbitratedFloat;
import ccre.behaviors.Behavior;
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
import ccre.ctrl.PIDController;
import ccre.drivers.ctre.talon.TalonExtendedMotor;
import ccre.frc.FRC;
import ccre.log.LogLevel;

public class Portcullis {

    private static final TalonExtendedMotor leftGrabMotor = FRC.talonCAN(12);
    private static final TalonExtendedMotor rightGrabMotor = FRC.talonCAN(13);

    private static final FloatIO leftEncoder = leftGrabMotor.modEncoder().getEncoderPosition();
    private static final FloatIO rightEncoder = rightGrabMotor.modEncoder().getEncoderPosition();
    private static final FloatInput leftOutputCurrent = leftGrabMotor.modFeedback().getOutputCurrent();
    private static final FloatInput rightOutputCurrent = rightGrabMotor.modFeedback().getOutputCurrent();

    private static final FloatInput grabberAxis = ZukoAzula.controlBinding.addFloat("Portcullis Grabber Axis").deadzone(0.3f).negated();
    private static final FloatInput targetVelocity = grabberAxis.multipliedBy(ZukoAzula.mainTuning.getFloat("Portcullis Speed", .5f));

    private static final BehaviorArbitrator grabBehaviors = new BehaviorArbitrator("Portcullis Behaviors");

    private static final ArbitratedFloat leftInput = grabBehaviors.addFloat();
    private static final ArbitratedFloat rightInput = grabBehaviors.addFloat();

    private static final BooleanCell needsToCalibrate = new BooleanCell(true);

    private static final FloatInput pidP = ZukoAzula.mainTuning.getFloat("Portcullis PID:P", 0.001f);
    private static final FloatInput pidI = ZukoAzula.mainTuning.getFloat("Portcullis PID:I", 0.0f);
    private static final FloatInput pidD = ZukoAzula.mainTuning.getFloat("Portcullis PID:D", 0.0f);

    private static final FloatInput autolevelSpeed = ZukoAzula.mainTuning.getFloat("Portcullis Auto-level Speed", 0.2f);

    private static final FloatCell autonomousVelocity = new FloatCell();
    private static FloatInput position;

    public static void setup() throws ExtendedMotorFailureException {
        position = leftEncoder.normalize(ZukoAzula.mainTuning.getFloat("Portcullis Distance to Low Position", 2470), ZukoAzula.mainTuning.getFloat("Portcullis Distance to High Position", -100));

        PIDController levelPID = new PIDController(leftEncoder, leftEncoder.plus(rightEncoder.negated()).dividedBy(2), pidP, pidI, pidD);
        levelPID.updateWhen(FRC.constantPeriodic);
        levelPID.setOutputBounds(autolevelSpeed);

        BooleanInput calibrating = needsToCalibrate.and(FRC.inTeleopMode().or(FRC.inAutonomousMode()));
        EventOutput resetEncoders = leftEncoder.eventSet(0).combine(rightEncoder.eventSet(0)).combine(needsToCalibrate.eventSet(false));
        FloatInput stalling = ZukoAzula.mainTuning.getFloat("Portcullis Stalling Current Threshold", 1.75f);
        BooleanInput bothStalling = leftOutputCurrent.atLeast(stalling).and(rightOutputCurrent.atLeast(stalling));
        resetEncoders.on(bothStalling.onPress().and(calibrating));

        Behavior teleop = grabBehaviors.addBehavior("teleop", FRC.inTeleopMode());
        leftInput.attach(teleop, targetVelocity.minus(targetVelocity.inRange(-0.1f, 0.1f).toFloat(0.0f, levelPID)));
        rightInput.attach(teleop, targetVelocity.plus(targetVelocity.inRange(-0.1f, 0.1f).toFloat(0.0f, levelPID)));
        Behavior autonomous = grabBehaviors.addBehavior("autonomous", FRC.inAutonomousMode());
        leftInput.attach(autonomous, autonomousVelocity.minus(autonomousVelocity.inRange(-0.1f, 0.1f).toFloat(0.0f, levelPID)));
        rightInput.attach(autonomous, autonomousVelocity.plus(autonomousVelocity.inRange(-0.1f, 0.1f).toFloat(0.0f, levelPID)));
        Behavior duringCalibration = grabBehaviors.addBehavior("calibration", calibrating);
        FloatInput calibrationSpeed = ZukoAzula.mainTuning.getFloat("Portcullis Speed During Calibration", .25f);
        leftInput.attach(duringCalibration, calibrationSpeed);
        rightInput.attach(duringCalibration, calibrationSpeed);

        leftInput.send(PowerManager.managePower(1, leftGrabMotor.simpleControl(FRC.MOTOR_REVERSE)));
        rightInput.send(PowerManager.managePower(1, rightGrabMotor.simpleControl(FRC.MOTOR_FORWARD)));

        EventLogger.log(calibrating.onPress(), LogLevel.INFO, "Started portcullis grabber calibration");
        EventLogger.log(calibrating.onRelease(), LogLevel.INFO, "Finished portcullis grabber calibration");

        Cluck.publish("Portcullis Calibrated Position", position);
        Cluck.publish("Portcullis Calibrate", needsToCalibrate);
        Cluck.publish("Portcullis Left Output Current", leftOutputCurrent);
        Cluck.publish("Portcullis Right Output Current", rightOutputCurrent);
        Cluck.publish("Portcullis Left Angle", leftEncoder.asInput());
        Cluck.publish("Portcullis Right Angle", rightEncoder.negated());
        Cluck.publish("Portcullis PID", (FloatInput) levelPID);
    }

    public static FloatOutput getPortcullisOutput() {
        return autonomousVelocity;
    }

    public static FloatInput getArmHeight() {
        return position;
    }
}
