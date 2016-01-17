package org.team1540.zukoazula;

import ccre.channel.FloatInput;
import ccre.channel.FloatOutput;
import ccre.cluck.Cluck;
import ccre.ctrl.Drive;
import ccre.ctrl.ExtendedMotor;
import ccre.ctrl.ExtendedMotorFailureException;
import ccre.frc.FRC;

public class DriveCode {

    private static final FloatInput pilotLeftAxis = FRC.joystick1.axis(2).deadzone(0.2f), pilotRightAxis = FRC.joystick1.axis(6).deadzone(0.2f);
    private static final FloatInput pilotRightTrigger = FRC.joystick1.axis(4).deadzone(0.1f), pilotLeftTrigger = FRC.joystick1.axis(3).deadzone(0.1f);

    private static final ExtendedMotor rightFrontCAN = FRC.talonCAN(2), rightBackCAN = FRC.talonCAN(3), leftFrontCAN = FRC.talonCAN(0), leftBackCAN = FRC.talonCAN(1);

    public static void setupDrive() throws ExtendedMotorFailureException {

        final FloatOutput leftMotors = leftFrontCAN.simpleControl(FRC.MOTOR_FORWARD).combine(leftBackCAN.simpleControl(FRC.MOTOR_FORWARD));
        final FloatOutput rightMotors = rightFrontCAN.simpleControl(FRC.MOTOR_REVERSE).combine(rightBackCAN.simpleControl(FRC.MOTOR_REVERSE));

        final FloatOutput leftOut = leftMotors.addRamping(0.1f, FRC.constantPeriodic);
        final FloatOutput rightOut = rightMotors.addRamping(0.1f, FRC.constantPeriodic);

        Drive.extendedTank(pilotLeftAxis, pilotRightAxis, pilotLeftTrigger.minus(pilotRightTrigger), leftOut, rightOut);

        Cluck.publish("Drive Left Axis", pilotLeftAxis);
        Cluck.publish("Drive Right Axis", pilotRightAxis);
        Cluck.publish("Drive Left Output", leftOut);
        Cluck.publish("Drive Right Output", rightOut);
    }
}
