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
    private static final FloatInput encoder = intakeArmCAN.modEncoder().getEncoderPosition();

    private static final FloatInput intakeArmAxis = ZukoAzula.controlBinding.addFloat("Intake Arm Axis").deadzone(0.2f).negated();
    private static final EventInput intakeArmRollerForward = ZukoAzula.controlBinding.addEvent("Intake Arm Rollers Forward");
    private static final EventInput intakeArmRollerBackward = ZukoAzula.controlBinding.addEvent("Intake Arm Rollers Backward");
    private static final EventInput intakeArmRollerStop = ZukoAzula.controlBinding.addEvent("Intake Arm Rollers Stop");

    public static void setup() throws ExtendedMotorFailureException {
        intakeArmAxis.send(intakeArmCAN.simpleControl());
        FloatOutput rollerOutput = intakeArmRollerCAN.simpleControl();
        rollerOutput.setWhen(1, intakeArmRollerForward);
        rollerOutput.setWhen(0, intakeArmRollerStop);
        rollerOutput.setWhen(-1, intakeArmRollerBackward);
    }
}