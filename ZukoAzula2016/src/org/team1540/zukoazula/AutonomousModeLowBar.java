package org.team1540.zukoazula;

import ccre.channel.FloatInput;
import ccre.instinct.AutonomousModeOverException;

public class AutonomousModeLowBar extends AutonomousBase {

    @Tunable(3f)
    private FloatInput time;

    @Tunable(0.5f)
    private FloatInput drivingSpeed;

    public AutonomousModeLowBar() {
        super("Drive Under Low Bar");
    }

    @Override
    protected void runAutonomous() throws InterruptedException, AutonomousModeOverException {
        setIntakeArm(-.5f);
        driveForTime(time.get(), drivingSpeed.get());
        setIntakeArm(0);
    }
}
