package org.team1540.zukoazula;

import ccre.channel.FloatInput;
import ccre.channel.FloatOutput;
import ccre.cluck.Cluck;
import ccre.ctrl.Drive;
import ccre.ctrl.ExtendedMotor;
import ccre.ctrl.ExtendedMotorFailureException;
import ccre.frc.FRC;

public class DriveCode {

    private static final FloatInput driveLeftAxis = ZukoAzula.controlBinding.addFloat("Drive Left Axis").deadzone(0.2f);
    private static final FloatInput driveRightAxis = ZukoAzula.controlBinding.addFloat("Drive Right Axis").deadzone(0.2f);
    private static final FloatInput driveRightTrigger = ZukoAzula.controlBinding.addFloat("Drive Forwards Trigger").deadzone(0.2f);
    private static final FloatInput driveLeftTrigger = ZukoAzula.controlBinding.addFloat("Drive Backwards Trigger").deadzone(0.2f);

    private static final ExtendedMotor rightFrontCAN = FRC.talonCAN(2), rightBackCAN = FRC.talonCAN(3);
    private static final ExtendedMotor leftFrontCAN = FRC.talonCAN(0), leftBackCAN = FRC.talonCAN(1);

    public static void setup() throws ExtendedMotorFailureException {

        final FloatOutput leftMotors = leftFrontCAN.simpleControl(FRC.MOTOR_FORWARD).combine(leftBackCAN.simpleControl(FRC.MOTOR_FORWARD));
        final FloatOutput rightMotors = rightFrontCAN.simpleControl(FRC.MOTOR_REVERSE).combine(rightBackCAN.simpleControl(FRC.MOTOR_REVERSE));

        final FloatOutput leftOut = leftMotors.addRamping(0.1f, FRC.constantPeriodic);
        final FloatOutput rightOut = rightMotors.addRamping(0.1f, FRC.constantPeriodic);

        Drive.extendedTank(driveLeftAxis, driveRightAxis, driveLeftTrigger.minus(driveRightTrigger), leftOut, rightOut);

        Cluck.publish("Drive Left Raw", driveLeftAxis);
        Cluck.publish("Drive Right Raw", driveRightAxis);
        Cluck.publish("Drive Forwards Raw", driveRightTrigger);
        Cluck.publish("Drive Backwards Raw", driveLeftTrigger);
        Cluck.publish("Drive Left Output", leftOut);
        Cluck.publish("Drive Right Output", rightOut);
    }
}
