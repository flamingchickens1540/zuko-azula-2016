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

    private static final FloatInput control = ZukoAzula.controlBinding.addFloat("Portcullis Grabber Axis").deadzone(0.3f);

    private static final ArbitratedFloat leftInput = ZukoAzula.behaviors.addFloat();
    private static final ArbitratedFloat rightInput = ZukoAzula.behaviors.addFloat();

    private static final FloatCell maximumSpeed = ZukoAzula.mainTuning.getFloat("Portcullis Maximum Speed", .3f);

    public static void setup() throws ExtendedMotorFailureException {

        PIDController pid = new PIDController(leftEncoder, rightEncoder.negated(), ZukoAzula.mainTuning.getFloat("Portcullis PID:P", 0.001f), ZukoAzula.mainTuning.getFloat("Portcullis PID:I", 0.0f), ZukoAzula.mainTuning.getFloat("Portcullis PID:D", 0.0f));

        pid.updateWhen(FRC.constantPeriodic);
        pid.setOutputBounds(ZukoAzula.mainTuning.getFloat("Portcullis Auto-level Speed", 0.2f));

        FloatInput leftOut = control.minus(control.inRange(-0.1f, 0.1f).toFloat(0.0f, pid)).multipliedBy(maximumSpeed);
        FloatInput rightOut = control.multipliedBy(maximumSpeed);

        leftInput.attach(ZukoAzula.teleop, leftOut);
        rightInput.attach(ZukoAzula.teleop, rightOut);
        leftInput.attach(ZukoAzula.pit, leftOut);
        rightInput.attach(ZukoAzula.pit, rightOut);

        leftInput.send(leftGrabMotor.simpleControl(FRC.MOTOR_REVERSE));
        rightInput.send(rightGrabMotor.simpleControl(FRC.MOTOR_FORWARD));

        leftEncoder.eventSet(0).combine(rightEncoder.eventSet(0)).on(FRC.startAuto.or(FRC.startTele));
        Cluck.publish("Portcullis Reset Encoders", leftEncoder.eventSet(0).combine(rightEncoder.eventSet(0)));
        Cluck.publish("Portcullis Left Angle", leftEncoder);
        Cluck.publish("Portcullis Right Angle", rightEncoder.negated());
        Cluck.publish("Portcullis PID", (FloatInput) pid);
    }
}
