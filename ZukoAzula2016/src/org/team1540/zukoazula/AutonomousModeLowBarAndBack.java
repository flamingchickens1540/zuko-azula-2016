package org.team1540.zukoazula;

import ccre.channel.FloatInput;
import ccre.instinct.AutonomousModeOverException;

public class AutonomousModeLowBarAndBack extends AutonomousBase {

    @Tunable(35.0f)
    private FloatInput distance;

    @Tunable(0.5f)
    private FloatInput drivingSpeed;

    public AutonomousModeLowBarAndBack() {
        super("Drive Under Low Bar, Eject, and Return");
    }

    @Override
    protected void runAutonomous() throws InterruptedException, AutonomousModeOverException {
        setIntakeArm(-.8f);
        driveDistance(distance.get(), drivingSpeed.get());
        ejectWhileDrivingBackDistance(distance.get(), drivingSpeed.get());
    }
}
