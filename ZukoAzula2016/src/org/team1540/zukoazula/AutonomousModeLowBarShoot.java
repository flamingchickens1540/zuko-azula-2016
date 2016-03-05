package org.team1540.zukoazula;

import ccre.channel.FloatInput;
import ccre.instinct.AutonomousModeOverException;

public class AutonomousModeLowBarShoot extends AutonomousBase {

    @Tunable(5000.0f)
    private FloatInput distanceToFirstTurn;

    @Tunable(45.0f)
    private FloatInput angleToTurn;

    @Tunable(5000.0f)
    private FloatInput distanceToShoot;

    @Tunable(3.0f)
    private FloatInput shootingTime;

    @Tunable(0.5f)
    private FloatInput drivingSpeed;

    public AutonomousModeLowBarShoot() {
        super("Drive Forward");
    }

    @Override
    protected void runAutonomous() throws InterruptedException, AutonomousModeOverException {
        driveDistance(distanceToFirstTurn.get(), drivingSpeed.get());
        turnAngle(angleToTurn.get(), true); // TODO Waiting on heading sensor
        driveDistance(distanceToShoot.get(), drivingSpeed.get());
        fire(shootingTime.get());
    }
}
