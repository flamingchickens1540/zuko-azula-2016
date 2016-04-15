package org.team1540.zukoazula;

import ccre.channel.FloatInput;
import ccre.cluck.Cluck;
import ccre.instinct.AutonomousModeOverException;

public class AutonomousModeRoughTerrainShoot extends AutonomousBaseHighGoal {

    @Tunable(30f)
    private FloatInput distance;

    @Tunable(8f)
    private FloatInput desiredPitch;

    @Tunable(3f)
    private FloatInput timeout;

    @Tunable(0.5f)
    private FloatInput drivingSpeed;

    public AutonomousModeRoughTerrainShoot() {
        super("Drive Over Rough Terrain and Shoot");
    }

    @Override
    protected void runAutonomous() throws InterruptedException, AutonomousModeOverException {
        float startAngle = HeadingSensor.absoluteYaw.get();
        driveUntilPitchOrTimeout(drivingSpeed.get(), desiredPitch.get(), timeout.get());
        turnAngle(startAngle - HeadingSensor.absoluteYaw.get() - 45, true);
        runVisionAutonomous();
    }
}