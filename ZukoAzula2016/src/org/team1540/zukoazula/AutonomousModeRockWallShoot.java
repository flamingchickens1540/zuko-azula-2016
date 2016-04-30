package org.team1540.zukoazula;

import ccre.channel.FloatInput;
import ccre.cluck.Cluck;
import ccre.instinct.AutonomousModeOverException;

public class AutonomousModeRockWallShoot extends AutonomousBaseKangaroo {

    @Tunable(10f)
    private FloatInput desiredPitch;

    @Tunable(3f)
    private FloatInput timeout;

    @Tunable(.5f)
    private FloatInput afterTime;

    @Tunable(0.8f)
    private FloatInput drivingSpeed;

    public AutonomousModeRockWallShoot() {
        super("Drive Over Rock Wall and Shoot");
    }

    @Override
    protected void runAutonomous() throws InterruptedException, AutonomousModeOverException {
        float startAngle = HeadingSensor.absoluteYaw.get();
        driveUntilPitchOrTimeout(drivingSpeed.get(), desiredPitch.get(), timeout.get());
        driveForTime(afterTime.get(), drivingSpeed.get());
        turnAngle(startAngle - HeadingSensor.absoluteYaw.get() - 45, true);
        runKangarooAutonomous();
    }
}