package org.team1540.zukoazula;

import ccre.channel.FloatInput;
import ccre.cluck.Cluck;
import ccre.instinct.AutonomousModeOverException;

public class AutonomousModeMoat extends AutonomousBase {

    @Tunable(-12f)
    private FloatInput desiredPitch;

    @Tunable(3f)
    private FloatInput timeout;

    @Tunable(1f)
    private FloatInput drivingSpeed;

    public AutonomousModeMoat() {
        super("Drive Over Moat");
    }

    @Override
    protected void runAutonomous() throws InterruptedException, AutonomousModeOverException {
        driveUntilPitchOrTimeout(drivingSpeed.get(), desiredPitch.get(), timeout.get());
    }
}