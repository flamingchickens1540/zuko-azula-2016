package org.team1540.zukoazula;

import ccre.instinct.AutonomousModeOverException;

public class AutonomousModeTest extends AutonomousBase {

    public AutonomousModeTest() {
        super("Test Jake's Code");
    }

    @Override
    protected void runAutonomous() throws InterruptedException, AutonomousModeOverException {
        driveDistance(6.0f, 0.6f, true);
    }

}
