package org.team1540.zukoazula;

import ccre.channel.FloatInput;
import ccre.instinct.AutonomousModeOverException;

public class AutonomousModePortcullis extends AutonomousBase {

    @Tunable(1f)
    private FloatInput distanceToPortcullis;

    @Tunable(2f)
    private FloatInput distanceThroughPortcullis;

    @Tunable(0.5f)
    private FloatInput drivingSpeed;

    @Tunable(0.5f)
    private FloatInput portcullisArmSpeed;

    public AutonomousModePortcullis() {
        super("Drive Through Portcullis");
    }

    @Override
    protected void runAutonomous() throws InterruptedException, AutonomousModeOverException {
        movePortcullisArmToPosition(0, portcullisArmSpeed.get());
        driveDistance(distanceToPortcullis.get(), drivingSpeed.get());
        movePortcullisArmToPosition(1, portcullisArmSpeed.get());
        driveDistance(distanceThroughPortcullis.get(), drivingSpeed.get());
    }
}
