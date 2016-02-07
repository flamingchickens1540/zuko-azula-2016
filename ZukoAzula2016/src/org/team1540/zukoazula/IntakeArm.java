package org.team1540.zukoazula;

import ccre.channel.BooleanIO;
import ccre.channel.BooleanInput;
import ccre.channel.EventInput;
import ccre.channel.FloatInput;
import ccre.channel.FloatOutput;
import ccre.ctrl.ExtendedMotor;
import ccre.ctrl.ExtendedMotorFailureException;
import ccre.drivers.ctre.talon.TalonExtendedMotor;
import ccre.frc.FRC;

public class IntakeArm {
    private static final TalonExtendedMotor intakeArmCAN = FRC.talonCAN(9);
    private static final TalonExtendedMotor intakeArmRollerCAN = FRC.talonCAN(11);
    private static final FloatInput encoder = intakeArmCAN.modEncoder().getEncoderVelocity();

    private static final FloatInput intakeArmAxis = ZukoAzula.controlBinding.addFloat("Intake Arm Axis").deadzone(0.2f).negated();
    private static final BooleanInput intakeArmRollerToggle = ZukoAzula.controlBinding.addToggleButton("Intake Arm Rollers Enable");
    private static final BooleanInput intakeArmRollerDirectionToggle = ZukoAzula.controlBinding.addToggleButton("Intake Arm Rollers Direction");
    
    public static void setup() throws ExtendedMotorFailureException {
        intakeArmAxis.send(intakeArmCAN.simpleControl());
        intakeArmRollerToggle.toFloat(0, 1).multipliedBy(intakeArmRollerDirectionToggle.toFloat(1, -1)).send(intakeArmRollerCAN.simpleControl());
    }
}