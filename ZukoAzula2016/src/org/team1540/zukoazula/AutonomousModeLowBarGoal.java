package org.team1540.zukoazula;

import ccre.channel.FloatInput;
import ccre.instinct.AutonomousModeOverException;

public class AutonomousModeLowBarGoal extends AutonomousBaseKangaroo {

    @Tunable(3f)
    private FloatInput time;

    @Tunable(0.5f)
    private FloatInput drivingSpeed;

    public AutonomousModeLowBarGoal() {
        super("Drive Under Low Bar and Shoot");
    }

    @Override
    protected void runAutonomous() throws InterruptedException, AutonomousModeOverException {
        float startAngle = HeadingSensor.absoluteYaw.get();
        setIntakeArm(-.5f);
        driveForTime(time.get(), drivingSpeed.get());
        setIntakeArm(0);
        setIntakeArm(0.5f);
        waitForTime(200);
        arcAngle(startAngle - HeadingSensor.absoluteYaw.get() + 30, true);
        //turnForTime(0.3f, 1f);
        setIntakeArm(0);
        runKangarooAutonomous();
    }
}
