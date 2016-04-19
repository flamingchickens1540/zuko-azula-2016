package org.team1540.zukoazula;

import java.awt.image.BufferedImage;
import java.util.List;

import org.team1540.vision.Goal;
import org.team1540.vision.ImageProcessor;
import org.team1540.vision.WebcamThread;

import ccre.channel.FloatInput;
import ccre.instinct.AutonomousModeOverException;
import ccre.log.Logger;

public class AutonomousModeHighGoal extends AutonomousBaseKangaroo {

    public AutonomousModeHighGoal() {
        super("Drive and Shoot");
    }

    @Override
    protected void runAutonomous() throws InterruptedException, AutonomousModeOverException {
        runKangarooAutonomous();
    }
}
