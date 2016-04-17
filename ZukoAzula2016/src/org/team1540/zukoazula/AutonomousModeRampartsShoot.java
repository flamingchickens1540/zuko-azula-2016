package org.team1540.zukoazula;

import ccre.channel.FloatInput;
import ccre.cluck.Cluck;
import ccre.instinct.AutonomousModeOverException;

public class AutonomousModeRampartsShoot extends AutonomousBaseHighGoal {

    @Tunable(-13f)
    private FloatInput desiredPitch;

    @Tunable(3f)
    private FloatInput timeout;

    @Tunable(1f)
    private FloatInput drivingSpeed;

    public AutonomousModeRampartsShoot() {
        super("Drive Over Ramparts and Shoot");
    }

    @Override
    protected void runAutonomous() throws InterruptedException, AutonomousModeOverException {
        float startAngle = HeadingSensor.absoluteYaw.get();
        driveUntilPitchOrTimeout(drivingSpeed.get(), desiredPitch.get(), timeout.get());
        turnAngle(startAngle - HeadingSensor.absoluteYaw.get() - 45, true);
        runVisionAutonomous();
    }
}