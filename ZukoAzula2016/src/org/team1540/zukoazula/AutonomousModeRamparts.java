package org.team1540.zukoazula;

import ccre.channel.FloatInput;
import ccre.cluck.Cluck;
import ccre.instinct.AutonomousModeOverException;

public class AutonomousModeRamparts extends AutonomousBase {

    @Tunable(-13f)
    private FloatInput desiredPitch;

    @Tunable(3f)
    private FloatInput timeout;

    @Tunable(1f)
    private FloatInput drivingSpeed;

    @Tunable(0.7f)
    private FloatInput extraDrive;

    public AutonomousModeRamparts() {
        super("Drive Over Ramparts");
    }

    @Override
    protected void runAutonomous() throws InterruptedException, AutonomousModeOverException {
        driveUntilPitchOrTimeout(drivingSpeed.get(), desiredPitch.get(), timeout.get());
        driveForTime(extraDrive.get(), drivingSpeed.get());
    }
}