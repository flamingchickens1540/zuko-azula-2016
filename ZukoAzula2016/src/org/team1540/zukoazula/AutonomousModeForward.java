package org.team1540.zukoazula;

import ccre.channel.FloatInput;
import ccre.instinct.AutonomousModeOverException;

public class AutonomousModeForward extends AutonomousBase {

    @Tunable(3.0f)
    private FloatInput timeToDriveForward;
    
    @Tunable(0.5f)
    private FloatInput drivingSpeed;
    
    public AutonomousModeForward() {
        super("Drive Forward");
    }

    @Override
    protected void runAutonomous() throws InterruptedException, AutonomousModeOverException {
        if (timeToDriveForward.get() > 0.0f) {
            driveForTime(timeToDriveForward.get(), drivingSpeed.get());
        }
    }
}
