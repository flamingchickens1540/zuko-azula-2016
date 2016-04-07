package org.team1540.zukoazula;

import ccre.channel.FloatInput;
import ccre.instinct.AutonomousModeOverException;

public class AutonomousModeLowBarShallAndShoot extends AutonomousBaseHighGoal {

    @Tunable(0.5f)
    private FloatInput drivingSpeed;

    @Tunable(.5f)
    private FloatInput backTime;

    @Tunable(90f)
    private FloatInput angle;

    public AutonomousModeLowBarShallAndShoot() {
        super("Drive Under Low Bar, Stall, and Shoot");
    }

    @Override
    protected void runAutonomous() throws InterruptedException, AutonomousModeOverException {
        setIntakeArm(-.5f);
        driveUntilStall(drivingSpeed.get());
        setIntakeArm(0);
        driveForTime(backTime.get(), -drivingSpeed.get());
        turnAngle(90, true);
        runVisionAutonomous();
    }
}
