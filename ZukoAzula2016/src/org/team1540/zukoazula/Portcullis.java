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

    private static final FloatInput control = ZukoAzula.controlBinding.addFloat("Portcullis Grabber Axis").deadzone(0.2f);

    private static final ArbitratedFloat leftInput = ZukoAzula.behaviors.addFloat();
    private static final ArbitratedFloat rightInput = ZukoAzula.behaviors.addFloat();

    public static void setup() throws ExtendedMotorFailureException {
        Cluck.publish("Zero Portcullis Encoders", leftEncoder.eventSet(0).combine(rightEncoder.eventSet(0)));
        Cluck.publish("Portcullis Left Angle", leftEncoder);
        Cluck.publish("Portcullis Right Angle", rightEncoder);

        PIDController pid = PIDController.createFixed(FRC.constantPeriodic, leftEncoder.asInput(), rightEncoder.asInput(), 
                ZukoAzula.mainTuning.getFloat("Portcullis PID:P", 0.7f).get(), ZukoAzula.mainTuning.getFloat("Portcullis PID:I", 0.0f).get(), ZukoAzula.mainTuning.getFloat("Portcullis PID:D", 0f).get());
        pid.setOutputBounds(ZukoAzula.mainTuning.getFloat("Portcullis Auto-level Speed", 0.3f));

        leftInput.attach(ZukoAzula.teleop, control.plus(pid));
        rightInput.attach(ZukoAzula.teleop, control.minus(pid));
        leftInput.attach(ZukoAzula.pit, control.plus(pid));
        rightInput.attach(ZukoAzula.pit, control.minus(pid));

        leftInput.send(leftGrabMotor.simpleControl(FRC.MOTOR_FORWARD));
        rightInput.send(rightGrabMotor.simpleControl(FRC.MOTOR_REVERSE));

    }

}
