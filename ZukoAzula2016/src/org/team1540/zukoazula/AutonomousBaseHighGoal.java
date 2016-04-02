package org.team1540.zukoazula;

import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Objects;

import org.team1540.vision.Goal;
import org.team1540.vision.ImageProcessor;
import org.team1540.vision.WebcamThread;

import ccre.instinct.AutonomousModeOverException;
import ccre.log.Logger;

public abstract class AutonomousBaseHighGoal extends AutonomousBase {
    private ImageProcessor processor = new ImageProcessor(100, 100);
    private final Object swapLock = new Object();
    private volatile BufferedImage swap;
    private String lastError;
    private final WebcamThread webcam = new WebcamThread((image) -> {
        synchronized (swapLock) {
            swap = image;
            swapLock.notifyAll();
        }
    }, (error) -> {
        if (!Objects.equals(error, lastError)) {
            lastError = error;
            Logger.finer("Error state of autonomous webcam: " + error);
        }
    });
    
    public AutonomousBaseHighGoal(String modeName) {
        super(modeName);
    }

    public void runVisionAutonomous() throws AutonomousModeOverException, InterruptedException {
        webcam.setAddress("10.15.40.13");
        try {
            turnForTime(0.1f, -0.7f);
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
                        (int) VisionConstants.targetRed.get(), // red target
                        (int) VisionConstants.targetGreen.get(), // green target
                        (int) VisionConstants.targetBlue.get(), // blue target
                        (int) VisionConstants.thresholdRed.get(), // red threshold
                        (int) VisionConstants.thresholdGreen.get(), // green threshold
                        (int) VisionConstants.thresholdBlue.get(), // blue threshold
                        (int) VisionConstants.minGoalPixCount.get(), // min goal pixel count
                        (int) VisionConstants.similarityThreshold.get(), // similarity threshold
                        (int) VisionConstants.aspectRatio.get(), // goal aspect ratio
                        (int) VisionConstants.aspectRatioThreshold.get()); // goal aspect ratio threshold

                Goal target = null;
                for (Goal g : goals) {
                    if (target == null || g.shape.getCount() > target.shape.getCount()) {
                        target = g;
                    }
                }
                
                if (target != null) {
                    float bottomX = (target.ll.x + target.lr.x) / 2.0f;
                    float bottomY = (target.ll.y + target.lr.y) / 2.0f;
                    float ppd = VisionConstants.pixelsPerDegree.get();
                    float yaw = (bottomX-currentImage.getWidth()) / ppd;
                    float pitch = (bottomY-currentImage.getHeight()) / ppd;
                    if (pitch > VisionConstants.minimumPitch.get()) {
                        /*if (Math.abs(yaw - VisionConstants.prelimAligningAngle.get()) >= VisionConstants.prelimAligningEpsilon.get()) {
                            Logger.fine("Turn angle");
                            turnAngle(yaw - VisionConstants.prelimAligningAngle.get(), true);
                            waitForTime((long)(VisionConstants.cameraSettleTime.get()*1000.0f));
                        } else */if (Math.abs(yaw - VisionConstants.prelimAligningAngle.get()) >= VisionConstants.movementAligningEpsilon.get()){
                            Logger.fine("Turn time");
                            turnForTime(VisionConstants.minuteRotationTime.get(), Math.signum(yaw - VisionConstants.prelimAligningAngle.get())*VisionConstants.minuteRotationSpeed.get());
                            waitForTime((long)(VisionConstants.cameraSettleTime.get()*1000.0f));
                        } else {
                            Logger.fine("Drive time");
                            spinup();
                            driveForTime(VisionConstants.movementTime.get(), VisionConstants.movementSpeed.get());
                            waitForTime((long)(VisionConstants.cameraSettleTime.get()*1000.0f));
                        }
                    } else {
                        // TODO: add spinup?
                        if (Math.abs(yaw - VisionConstants.postMovementTargetAngle.get()) >= VisionConstants.postMovementTargetEpsilon.get()){
                            Logger.fine("Turn time 2");
                            turnForTime(VisionConstants.postMovementRotationTime.get(), Math.signum(yaw - VisionConstants.postMovementTargetAngle.get())*VisionConstants.minuteRotationSpeed.get());
                            waitForTime((long)(VisionConstants.cameraSettleTime.get()*1000.0f));
                        } else {
                            Logger.fine("Fire");
                            spinup();
                            driveForTime(VisionConstants.fireDriveSeconds.get(), VisionConstants.fireDriveSpeed.get());
                            waitForTime((long) (VisionConstants.fireWaitSeconds.get() * 1000));
                            fire(2.5f);
                            break;
                        }
                    }
                } else {
                    Logger.fine("Turn scan");
                    turnForTime(VisionConstants.scanTickTime.get(), VisionConstants.scanTickSpeed.get());
                    waitForTime((long)(VisionConstants.cameraSettleTime.get()*1000.0f));
                }
            }
        } finally {
            webcam.setAddress(null);
        }
    }
}
