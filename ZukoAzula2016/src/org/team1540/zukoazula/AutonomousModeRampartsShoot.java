package org.team1540.zukoazula;

import ccre.channel.FloatInput;
import ccre.cluck.Cluck;
import ccre.instinct.AutonomousModeOverException;

public class AutonomousModeRampartsShoot extends AutonomousBaseKangaroo {

    @Tunable(-13f)
    private FloatInput desiredPitch;

    @Tunable(3f)
    private FloatInput timeout;

    @Tunable(1f)
    private FloatInput drivingSpeed;

    @Tunable(0.7f)
    private FloatInput extraDrive;

    public AutonomousModeRampartsShoot() {
        super("Drive Over Ramparts and Shoot");
    }

    @Override
    protected void runAutonomous() throws InterruptedException, AutonomousModeOverException {
        float startAngle = HeadingSensor.absoluteYaw.get();
        driveUntilPitchOrTimeout(drivingSpeed.get(), desiredPitch.get(), timeout.get());
        driveForTime(extraDrive.get(), drivingSpeed.get());
        turnAngle(startAngle - HeadingSensor.absoluteYaw.get() - 45, true);
        runKangarooAutonomous();
    }
}