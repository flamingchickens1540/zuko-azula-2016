package org.team1540.zukoazula;

import ccre.behaviors.ArbitratedFloat;
import ccre.channel.BooleanCell;
import ccre.channel.FloatCell;
import ccre.channel.FloatIO;
import ccre.channel.FloatInput;
import ccre.channel.FloatOutput;
import ccre.cluck.Cluck;
import ccre.ctrl.Drive;
import ccre.ctrl.ExtendedMotor;
import ccre.ctrl.ExtendedMotorFailureException;
import ccre.drivers.ctre.talon.TalonExtendedMotor;
import ccre.frc.FRC;

public class DriveCode {

    private static final FloatInput driveLeftAxis = ZukoAzula.controlBinding.addFloat("Drive Left Axis").deadzone(0.2f).negated();
    private static final FloatInput driveRightAxis = ZukoAzula.controlBinding.addFloat("Drive Right Axis").deadzone(0.2f).negated();
    private static final FloatInput driveRightTrigger = ZukoAzula.controlBinding.addFloat("Drive Forwards Trigger").deadzone(0.2f);
    private static final FloatInput driveLeftTrigger = ZukoAzula.controlBinding.addFloat("Drive Backwards Trigger").deadzone(0.2f);

    private static final TalonExtendedMotor[] rightCANs = new TalonExtendedMotor[] { FRC.talonCAN(4), FRC.talonCAN(5), FRC.talonCAN(6) };
    private static final TalonExtendedMotor[] leftCANs = new TalonExtendedMotor[] { FRC.talonCAN(1), FRC.talonCAN(2), FRC.talonCAN(3) };

    private static final FloatIO driveEncoder = leftCANs[0].modEncoder().getEncoderPosition();

    private static final ArbitratedFloat leftInput = ZukoAzula.behaviors.addFloat();
    private static final ArbitratedFloat rightInput = ZukoAzula.behaviors.addFloat();

    private static final FloatCell autonomousLeft = new FloatCell();
    private static final FloatCell autonomousRight = new FloatCell();

    public static void setup() throws ExtendedMotorFailureException {
        leftInput.attach(ZukoAzula.autonomous, autonomousLeft);
        rightInput.attach(ZukoAzula.autonomous, autonomousRight);
        leftInput.attach(ZukoAzula.teleop, driveLeftAxis.plus(driveRightTrigger.minus(driveLeftTrigger)));
        rightInput.attach(ZukoAzula.teleop, driveRightAxis.plus(driveRightTrigger.minus(driveLeftTrigger)));
        leftInput.attach(ZukoAzula.pit, FloatInput.zero);
        rightInput.attach(ZukoAzula.pit, FloatInput.zero);

        FloatOutput leftMotors = PowerManager.managePower(1, FloatOutput.combine(simpleAll(leftCANs, FRC.MOTOR_FORWARD)));
        FloatOutput rightMotors = PowerManager.managePower(1, FloatOutput.combine(simpleAll(rightCANs, FRC.MOTOR_REVERSE)));

        leftInput.send(leftMotors.addRamping(0.1f, FRC.constantPeriodic));
        rightInput.send(rightMotors.addRamping(0.1f, FRC.constantPeriodic));

        Cluck.publish("Drive Left Raw", driveLeftAxis);
        Cluck.publish("Drive Right Raw", driveRightAxis);
        Cluck.publish("Drive Forwards Raw", driveRightTrigger);
        Cluck.publish("Drive Backwards Raw", driveLeftTrigger);
        Cluck.publish("Drive Left Motors", leftInput);
        Cluck.publish("Drive Right Motors", rightInput);
    }

    private static FloatOutput[] simpleAll(ExtendedMotor[] cans, boolean reverse) throws ExtendedMotorFailureException {
        FloatOutput[] outs = new FloatOutput[cans.length];
        for (int i = 0; i < cans.length; i++) {
            outs[i] = cans[i].simpleControl(reverse);
        }
        return outs;
    }

    public static FloatOutput getLeftOutput() {
        return autonomousLeft;
    }

    public static FloatOutput getRightOutput() {
        return autonomousRight;
    }

    public static FloatInput getEncoder() {
        return driveEncoder;
    }
}
