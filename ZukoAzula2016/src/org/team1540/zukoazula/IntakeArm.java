package org.team1540.zukoazula;

import ccre.channel.BooleanIO;
import ccre.channel.BooleanInput;
import ccre.channel.EventInput;
import ccre.channel.FloatCell;
import ccre.channel.FloatInput;
import ccre.channel.FloatOutput;
import ccre.cluck.Cluck;
import ccre.ctrl.ExtendedMotor;
import ccre.ctrl.ExtendedMotorFailureException;
import ccre.drivers.ctre.talon.TalonExtendedMotor;
import ccre.frc.FRC;

public class IntakeArm {
    private static final TalonExtendedMotor intakeArmCAN = FRC.talonCAN(9);
    private static final FloatInput encoder = intakeArmCAN.modEncoder().getEncoderPosition();

    private static final FloatInput intakeArmAxis = ZukoAzula.controlBinding.addFloat("Intake Arm Axis").deadzone(0.2f).negated();

    public static void setup() throws ExtendedMotorFailureException {
        FloatCell armLow = ZukoAzula.mainTuning.getFloat("Intake Arm Low Position", 0f);
        FloatCell armHigh = ZukoAzula.mainTuning.getFloat("Intake Arm High Position", 1f);
        FloatInput armPosition = encoder.normalize(armLow, armHigh);
        BooleanInput stop = armPosition.atLeast(1).and(intakeArmAxis.atLeast(0)).or(armPosition.atMost(0).and(intakeArmAxis.atMost(0)));
        stop.toFloat(intakeArmAxis.multipliedBy(ZukoAzula.mainTuning.getFloat("Intake Arm Speed", .25f)), 0f).send(intakeArmCAN.simpleControl());

        Cluck.publish("Intake Arm Set Low Position", () -> armLow.set(encoder.get()));
        Cluck.publish("Intake Arm Set High Position", () -> armHigh.set(encoder.get()));
    }
}
