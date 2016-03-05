package org.team1540.zukoazula;

import ccre.channel.FloatInput;
import ccre.instinct.AutonomousModeOverException;

public class AutonomousModeDriveOverX extends AutonomousBase {

    @Tunable(0.5f)
    private FloatInput distance;

    @Tunable(0.5f)
    private FloatInput drivingSpeed;

    public AutonomousModeDriveOverX() {
        super("Drive Over Something");
    }

    @Override
    protected void runAutonomous() throws InterruptedException, AutonomousModeOverException {
        driveForTime(drivingSpeed.get(), distance.get());
    }
}