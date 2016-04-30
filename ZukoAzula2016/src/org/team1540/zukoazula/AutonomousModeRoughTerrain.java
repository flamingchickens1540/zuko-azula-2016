package org.team1540.zukoazula;

import ccre.channel.FloatInput;
import ccre.cluck.Cluck;
import ccre.instinct.AutonomousModeOverException;

public class AutonomousModeRoughTerrain extends AutonomousBase {

    @Tunable(30f)
    private FloatInput distance;

    @Tunable(8f)
    private FloatInput desiredPitch;

    @Tunable(3f)
    private FloatInput timeout;

    @Tunable(0.5f)
    private FloatInput drivingSpeed;

    @Tunable(1f)
    private FloatInput extraDrive;

    public AutonomousModeRoughTerrain() {
        super("Drive Over Rough Terrain");
    }

    @Override
    protected void runAutonomous() throws InterruptedException, AutonomousModeOverException {
        driveUntilPitchOrTimeout(drivingSpeed.get(), desiredPitch.get(), timeout.get());
        driveForTime(extraDrive.get(), drivingSpeed.get());
    }
}