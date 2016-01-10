package org.team1540.zukoazula;

import ccre.channel.BooleanCell;
import ccre.channel.BooleanOutput;
import ccre.channel.FloatOutput;
import ccre.ctrl.Drive;
import ccre.frc.FRC;
import ccre.frc.FRCApplication;
import ccre.log.Logger;

public class RobotTemplate implements FRCApplication {

    public static final int TEAM_NUMBER = 1540;

    @Override
    public void setupRobot() {
        Logger.info("CHEEP CHEEP");

        // Currently for Valkyrie
        FloatOutput left = FRC.talon(4, FRC.MOTOR_FORWARD).combine(FRC.talon(5, FRC.MOTOR_REVERSE)).combine(FRC.talon(6, FRC.MOTOR_FORWARD));
        FloatOutput right = FRC.talon(1, FRC.MOTOR_REVERSE).combine(FRC.talon(2, FRC.MOTOR_REVERSE)).combine(FRC.talon(3, FRC.MOTOR_REVERSE));
        Drive.tank(FRC.joystick1.axisY().negated(), FRC.joystick1.axis(6).negated(), left, right);
        new BooleanCell(FRC.solenoid(1)).toggleWhen(FRC.joystick1.onPress(1));
    }
}
