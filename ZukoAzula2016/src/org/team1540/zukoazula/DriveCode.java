package org.team1540.zukoazula;

import ccre.behaviors.ArbitratedFloat;
import ccre.behaviors.Behavior;
import ccre.behaviors.BehaviorArbitrator;
import ccre.channel.BooleanCell;
import ccre.channel.FloatCell;
import ccre.channel.FloatInput;
import ccre.channel.FloatOutput;
import ccre.cluck.Cluck;
import ccre.ctrl.Drive;
import ccre.ctrl.ExtendedMotor;
import ccre.ctrl.ExtendedMotorFailureException;
import ccre.frc.FRC;

public class DriveCode {

    private static final FloatInput driveLeftAxis = ZukoAzula.controlBinding.addFloat("Drive Left Axis").deadzone(0.2f).negated();
    private static final FloatInput driveRightAxis = ZukoAzula.controlBinding.addFloat("Drive Right Axis").deadzone(0.2f).negated();
    private static final FloatInput driveRightTrigger = ZukoAzula.controlBinding.addFloat("Drive Forwards Trigger").deadzone(0.2f);
    private static final FloatInput driveLeftTrigger = ZukoAzula.controlBinding.addFloat("Drive Backwards Trigger").deadzone(0.2f);

    private static final ExtendedMotor rightFrontCAN = FRC.talonCAN(2), rightBackCAN = FRC.talonCAN(3);
    private static final ExtendedMotor leftFrontCAN = FRC.talonCAN(0), leftBackCAN = FRC.talonCAN(1);

    private static final BehaviorArbitrator behaviors = new BehaviorArbitrator("Drive Code");
    private static final ArbitratedFloat leftInput = behaviors.addFloat();
    private static final ArbitratedFloat rightInput = behaviors.addFloat();
    private static final Behavior teleop = behaviors.addBehavior("Teleop", FRC.inTeleopMode());
    private static final BooleanCell pitModeEnable = new BooleanCell();
    private static final Behavior pit = behaviors.addBehavior("Pit Mode", pitModeEnable.andNot(FRC.isOnFMS()));

    public static void setup() throws ExtendedMotorFailureException {
        leftInput.attach(teleop, driveLeftAxis.plus(driveRightTrigger.minus(driveLeftTrigger)));
        rightInput.attach(teleop, driveRightAxis.plus(driveRightTrigger.minus(driveLeftTrigger)));
        leftInput.attach(pit, FloatInput.zero);
        rightInput.attach(pit, FloatInput.zero);

        FloatOutput leftMotors = leftFrontCAN.simpleControl(FRC.MOTOR_FORWARD).combine(leftBackCAN.simpleControl(FRC.MOTOR_FORWARD));
        FloatOutput rightMotors = rightFrontCAN.simpleControl(FRC.MOTOR_REVERSE).combine(rightBackCAN.simpleControl(FRC.MOTOR_REVERSE));

        leftInput.send(leftMotors.addRamping(0.1f, FRC.constantPeriodic));
        rightInput.send(rightMotors.addRamping(0.1f, FRC.constantPeriodic));

        Cluck.publish("Drive Left Raw", driveLeftAxis);
        Cluck.publish("Drive Right Raw", driveRightAxis);
        Cluck.publish("Drive Forwards Raw", driveRightTrigger);
        Cluck.publish("Drive Backwards Raw", driveLeftTrigger);
        Cluck.publish("Drive Left Motors", leftInput);
        Cluck.publish("Drive Right Motors", rightInput);
        Cluck.publish("Pit Mode Enable", pitModeEnable);
    }
}
