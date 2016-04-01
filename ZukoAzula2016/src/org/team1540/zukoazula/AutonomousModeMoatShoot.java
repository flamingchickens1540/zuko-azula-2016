package org.team1540.zukoazula;

import ccre.channel.FloatInput;
import ccre.cluck.Cluck;
import ccre.instinct.AutonomousModeOverException;

public class AutonomousModeMoatShoot extends AutonomousBaseHighGoal {

    @Tunable(-12f)
    private FloatInput desiredPitch;

    @Tunable(3f)
    private FloatInput timeout;

    @Tunable(1f)
    private FloatInput drivingSpeed;

    public AutonomousModeMoatShoot() {
        super("Drive Over Moat and Shoot");
    }

    @Override
    protected void runAutonomous() throws InterruptedException, AutonomousModeOverException {
        float startAngle = HeadingSensor.absoluteYaw.get();
        driveUntilPitchOrTimeout(drivingSpeed.get(), desiredPitch.get(), timeout.get());
        turnAngle(startAngle - HeadingSensor.absoluteYaw.get(), true);
        runVisionAutonomous();
    }
}