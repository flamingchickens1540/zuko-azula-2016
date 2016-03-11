package org.team1540.zukoazula;

import ccre.channel.FloatInput;
import ccre.instinct.AutonomousModeOverException;

public class AutonomousModeLowBarEject extends AutonomousBase {

    @Tunable(35.0f)
    private FloatInput distance;

    @Tunable(0.5f)
    private FloatInput drivingSpeed;

    public AutonomousModeLowBarEject() {
        super("Drive Under Low Bar and Back");
    }

    @Override
    protected void runAutonomous() throws InterruptedException, AutonomousModeOverException {
        setIntakeArm(-.8f);
        driveDistance(distance.get(), drivingSpeed.get(), false);
        driveDistance(-distance.get(), drivingSpeed.get(), false);
    }
}
