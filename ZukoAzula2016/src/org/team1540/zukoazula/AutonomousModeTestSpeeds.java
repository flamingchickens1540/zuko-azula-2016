package org.team1540.zukoazula;

import ccre.channel.FloatInput;
import ccre.cluck.Cluck;
import ccre.instinct.AutonomousModeOverException;
import ccre.log.Logger;

public class AutonomousModeTestSpeeds extends AutonomousBase {

    @Tunable(1f)
    private FloatInput timeout;

    @Tunable(1f)
    private FloatInput drivingSpeed;

    public AutonomousModeTestSpeeds() {
        super("Test");
    }

    @Override
    protected void runAutonomous() throws InterruptedException, AutonomousModeOverException {
        driveForTime(timeout.get(), drivingSpeed.get());
        Logger.info("Position at end of drive: " + DriveCode.getEncoder().get());
    }
}