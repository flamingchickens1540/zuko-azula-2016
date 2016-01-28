package org.team1540.zukoazula;

import ccre.behaviors.ArbitratedFloat;
import ccre.behaviors.Behavior;
import ccre.behaviors.BehaviorArbitrator;
import ccre.channel.FloatCell;
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

    public static final FloatCell autoLeftMotors = new FloatCell();
    public static final FloatCell autoRightMotors = new FloatCell();
    public static final FloatOutput autoAllMotors = autoLeftMotors.combine(autoRightMotors);

    private static final BehaviorArbitrator behaviors = new BehaviorArbitrator("Drive Code");
    private static final ArbitratedFloat leftInput = behaviors.addFloat();
    private static final ArbitratedFloat rightInput = behaviors.addFloat();
    private static final Behavior teleop = behaviors.addBehavior("Teleop", FRC.inTeleopMode());
    private static final Behavior auto = behaviors.addBehavior("Autonomous", FRC.inAutonomousMode());
    private static final Behavior pit = behaviors.addBehavior("Pit Mode", ZukoAzula.mainTuning.getBoolean("Pit Mode Enable", false));

    public static void setup() throws ExtendedMotorFailureException {
        leftInput.attach(teleop, driveLeftAxis.plus(driveLeftTrigger.minus(driveRightTrigger)));
        rightInput.attach(teleop, driveRightAxis.plus(driveRightTrigger.minus(driveLeftTrigger)));
        leftInput.attach(auto, autoLeftMotors);
        rightInput.attach(auto, autoRightMotors);
        leftInput.attach(pit, FloatInput.zero);
        rightInput.attach(pit, FloatInput.zero);

        final FloatOutput leftMotors = leftFrontCAN.simpleControl(FRC.MOTOR_FORWARD).combine(leftBackCAN.simpleControl(FRC.MOTOR_FORWARD));
        final FloatOutput rightMotors = rightFrontCAN.simpleControl(FRC.MOTOR_REVERSE).combine(rightBackCAN.simpleControl(FRC.MOTOR_REVERSE));

        final FloatOutput leftOut = leftMotors.addRamping(0.1f, FRC.constantPeriodic);
        final FloatOutput rightOut = rightMotors.addRamping(0.1f, FRC.constantPeriodic);

        leftInput.send(leftOut);
        rightInput.send(rightOut);

        Cluck.publish("Drive Left Raw", driveLeftAxis);
        Cluck.publish("Drive Right Raw", driveRightAxis);
        Cluck.publish("Drive Forwards Raw", driveRightTrigger);
        Cluck.publish("Drive Backwards Raw", driveLeftTrigger);
        Cluck.publish("Drive Left Output", leftOut);
        Cluck.publish("Drive Right Output", rightOut);
    }
}
