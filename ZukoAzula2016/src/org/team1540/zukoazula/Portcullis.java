package org.team1540.zukoazula;

import ccre.behaviors.ArbitratedFloat;
import ccre.channel.BooleanCell;
import ccre.channel.BooleanInput;
import ccre.channel.EventOutput;
import ccre.channel.FloatCell;
import ccre.channel.FloatIO;
import ccre.channel.FloatInput;
import ccre.cluck.Cluck;
import ccre.ctrl.ExtendedMotorFailureException;
import ccre.ctrl.PIDController;
import ccre.drivers.ctre.talon.TalonExtendedMotor;
import ccre.frc.FRC;

public class Portcullis {

    private static final TalonExtendedMotor leftGrabMotor = FRC.talonCAN(12);
    private static final TalonExtendedMotor rightGrabMotor = FRC.talonCAN(13);

    private static final FloatIO leftEncoder = leftGrabMotor.modEncoder().getEncoderPosition();
    private static final FloatIO rightEncoder = rightGrabMotor.modEncoder().getEncoderPosition();

    private static final FloatInput control = ZukoAzula.controlBinding.addFloat("Portcullis Grabber Axis").deadzone(0.3f).negated();

    private static final ArbitratedFloat leftInput = ZukoAzula.behaviors.addFloat();
    private static final ArbitratedFloat rightInput = ZukoAzula.behaviors.addFloat();

    private static final FloatInput pidP = ZukoAzula.mainTuning.getFloat("Portcullis PID:P", 0.001f);
    private static final FloatInput pidI = ZukoAzula.mainTuning.getFloat("Portcullis PID:I", 0.0f);
    private static final FloatInput pidD = ZukoAzula.mainTuning.getFloat("Portcullis PID:D", 0.0f);

    private static final FloatInput autolevelSpeed = ZukoAzula.mainTuning.getFloat("Portcullis Auto-level Speed", 0.2f);
    private static final FloatCell maximumSpeed = ZukoAzula.mainTuning.getFloat("Portcullis Maximum Speed", 0.4f);

    public static void setup() throws ExtendedMotorFailureException {

        PIDController levelPID = new PIDController(leftEncoder, leftEncoder.plus(rightEncoder.negated()).dividedBy(2), pidP, pidI, pidD);
        levelPID.updateWhen(FRC.constantPeriodic);
        levelPID.setOutputBounds(autolevelSpeed);
        
        leftEncoder.eventSet(rightEncoder.negated()).on(FRC.startAuto.or(FRC.startTele));
        
        FloatInput leftOut = control.multipliedBy(maximumSpeed).minus(control.outsideRange(-0.1f, 0.1f).toFloat(levelPID, 0.0f));
        FloatInput rightOut = control.multipliedBy(maximumSpeed).plus(control.outsideRange(-0.1f, 0.1f).toFloat(levelPID, 0.0f));

        leftInput.attach(ZukoAzula.teleop, leftOut);
        rightInput.attach(ZukoAzula.teleop, rightOut);
        leftInput.attach(ZukoAzula.pit, leftOut);
        rightInput.attach(ZukoAzula.pit, rightOut);

        leftInput.send(PowerManager.managePower(1, leftGrabMotor.simpleControl(FRC.MOTOR_REVERSE)));
        rightInput.send(PowerManager.managePower(1, rightGrabMotor.simpleControl(FRC.MOTOR_FORWARD)));
        
        Cluck.publish("Portcullis Zero Encoders", leftEncoder.eventSet(0).combine(rightEncoder.eventSet(0)));
        Cluck.publish("Portcullis Reset Encoders", leftEncoder.eventSet(rightEncoder));
        Cluck.publish("Portcullis Left Angle", leftEncoder.negated());
        Cluck.publish("Portcullis Right Angle", rightEncoder);
        Cluck.publish("Portcullis PID", (FloatInput) levelPID);

    }
}