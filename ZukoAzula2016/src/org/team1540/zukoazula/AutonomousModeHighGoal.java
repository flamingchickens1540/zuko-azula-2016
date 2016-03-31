package org.team1540.zukoazula;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;

import org.team1540.vision.Goal;
import org.team1540.vision.ImageProcessor;
import org.team1540.vision.WebcamReader;
import org.team1540.vision.WebcamThread;

import ccre.concurrency.ReporterThread;
import ccre.frc.FRC;
import ccre.instinct.AutonomousModeOverException;
import ccre.log.Logger;

public class AutonomousModeHighGoal extends AutonomousBase {

    private ImageProcessor processor = new ImageProcessor(100, 100);
    private final Object swapLock = new Object();
    private volatile BufferedImage swap;
    private final WebcamThread webcam = new WebcamThread((image) -> {
        synchronized (swapLock) {
            swap = image;
            swapLock.notifyAll();
        }
    }, (error) -> {
        Logger.finer("Error state of autonomous webcam: " + error);
    });

    public AutonomousModeHighGoal() {
        super("Drive and Shoot");
    }

    @Override
    protected void runAutonomous() throws InterruptedException, AutonomousModeOverException {
        webcam.setAddress("10.15.40.12");
        try {
            turnForTime(0.3f, -0.7f);
            synchronized (swapLock) {
                swap = null;
                while (swap == null) {
                    swapLock.wait();
                }
            }
            while (true) {
                waitForTime(1);
                BufferedImage currentImage = swap;
                processor = processor.useOrRealloc(currentImage.getWidth(), currentImage.getHeight());
                List<Goal> goals = processor.findGoals(currentImage,
                        205, // red target
                        20, // green target
                        10, // blue target
                        70, // red threshold
                        70, // green threshold
                        20, // blue threshold
                        50, // min goal pixel count
                        200.0f, // similarity threshold
                        3.2f, // goal aspect ratio
                        20.0f); // goal aspect ratio threshold

                Goal target = null;
                for (Goal g : goals) {
                    if (target == null || g.shape.getCount() > target.shape.getCount()) {
                        target = g;
                    }
                }

                if (target != null) {
                    float bottomAverageY = (target.ll.y + target.lr.y) / 2.0f;
                    float bottomAverageX = (target.ll.x + target.lr.x) / 2.0f;
                    float bottomDistance = (float) Math.sqrt((bottomAverageX - 245.0f) * (bottomAverageX - 245.0f) * 0.8f + (bottomAverageY - 0.0f) * (bottomAverageY - 0.0f));
                    float distance = 1.1f * (0.000103f * bottomDistance * bottomDistance - 0.012f * bottomDistance + 0.4211f);
                    float angle = (bottomAverageX - 245.0f) / (distance + 3.0f);

                    if (Math.abs(angle + 2.0f) < 3.0f) {
                        if (distance > 4.0f) {
                            driveForTime(0.3f, 0.3f);
                        } else if (distance > 1.0f) {
                            driveForTime(0.2f, 0.3f);
                            waitForTime(100);
                        } else {
                            // turnForTime(0.10f, 0.5f);
                            driveForTime(0.3f, 0.5f);
                            fire(1.5f);
                            break;
                        }
                    } else {
                        turnForTime(0.15f, Math.signum(angle) * 0.5f);
                        waitForTime(100);
                    }
                } else {
                    turnForTime(0.2f, 0.5f);
                    waitForTime(100);
                }
            }
        } finally {
            webcam.setAddress(null);
        }
    }
}
