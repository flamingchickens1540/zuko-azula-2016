package org.team1540.zukoazula;

import ccre.channel.FloatInput;
import ccre.cluck.Cluck;
import ccre.instinct.AutonomousModeOverException;

public class AutonomousModeRockWall extends AutonomousBase {

    @Tunable(10f)
    private FloatInput desiredPitch;

    @Tunable(3f)
    private FloatInput timeout;

    @Tunable(.2f)
    private FloatInput afterTime;

    @Tunable(0.9f)
    private FloatInput drivingSpeed;

    public AutonomousModeRockWall() {
        super("Drive Over Rock Wall");
    }

    @Override
    protected void runAutonomous() throws InterruptedException, AutonomousModeOverException {
        driveUntilPitchOrTimeout(drivingSpeed.get(), desiredPitch.get(), timeout.get());
        driveForTime(afterTime.get(), drivingSpeed.get());
    }
}
