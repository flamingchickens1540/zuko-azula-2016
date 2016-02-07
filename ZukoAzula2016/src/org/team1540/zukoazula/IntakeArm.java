package org.team1540.zukoazula;

import ccre.channel.FloatInput;
import ccre.ctrl.ExtendedMotorFailureException;
import ccre.drivers.ctre.talon.TalonExtendedMotor;
import ccre.frc.FRC;

public class IntakeArm {
    private static final TalonExtendedMotor intakeArmCAN = FRC.talonCAN(9);
    private static final FloatInput encoder = intakeArmCAN.modEncoder().getEncoderPosition();

    private static final FloatInput intakeArmAxis = ZukoAzula.controlBinding.addFloat("Intake Arm Axis").deadzone(0.2f).negated();

    public static void setup() throws ExtendedMotorFailureException {
        intakeArmAxis.send(intakeArmCAN.simpleControl());
    }
}
