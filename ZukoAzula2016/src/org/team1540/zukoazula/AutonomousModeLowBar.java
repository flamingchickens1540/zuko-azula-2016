package org.team1540.zukoazula;

import ccre.channel.FloatInput;
import ccre.instinct.AutonomousModeOverException;

public class AutonomousModeLowBar extends AutonomousBase {

    @Tunable(5.0f)
    private FloatInput distance;

    @Tunable(0.5f)
    private FloatInput drivingSpeed;

    public AutonomousModeLowBar() {
        super("Drive Under Low Bar");
    }

    @Override
    protected void runAutonomous() throws InterruptedException, AutonomousModeOverException {
        setIntakeArm(-.8f);
        driveDistance(distance.get(), drivingSpeed.get());
    }
}
