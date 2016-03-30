package org.team1540.zukoazula;

import ccre.channel.FloatInput;
import ccre.instinct.AutonomousModeOverException;

public class AutonomousModeLowBarAutoShoot extends AutonomousBase {

    @Tunable(0.5f)
    private FloatInput drivingSpeed;

    @Tunable(.5f)
    private FloatInput backTime;

    @Tunable(90f)
    private FloatInput angle;

    public AutonomousModeLowBarAutoShoot() {
        super("Drive Under Low Bar");
    }

    @Override
    protected void runAutonomous() throws InterruptedException, AutonomousModeOverException {
        driveUntilStall(drivingSpeed.get());
        driveForTime(backTime.get(), -drivingSpeed.get());
        turnAngle(90, true);
        // activate automatic shooting
    }
}
