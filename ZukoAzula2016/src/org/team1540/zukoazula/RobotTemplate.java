package org.team1540.zukoazula;

import ccre.channel.BooleanCell;
import ccre.channel.BooleanOutput;
import ccre.channel.FloatOutput;
import ccre.ctrl.Drive;
import ccre.ctrl.ExtendedMotorFailureException;
import ccre.frc.FRC;
import ccre.frc.FRCApplication;
import ccre.log.Logger;

public class RobotTemplate implements FRCApplication {

    public static final int TEAM_NUMBER = 1540;

    @Override
    public void setupRobot() throws ExtendedMotorFailureException {
        Logger.info("🐣 CHEEP CHEEP 🐣");

        DriveCode.setupDrive();
    }
}
