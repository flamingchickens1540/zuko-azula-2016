package org.team1540.zukoazula;

import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import org.team1540.vision.Goal;
import org.team1540.vision.ImageProcessor;
import org.team1540.vision.WebcamThread;

import ccre.instinct.AutonomousModeOverException;
import ccre.log.Logger;

public abstract class AutonomousBaseHighGoal extends AutonomousBase {
    private static ImageProcessor processor = new ImageProcessor(100, 100);
    private static final Object swapLock = new Object();
    private static volatile BufferedImage swap;
    private static String lastError;
    private static final WebcamThread webcam = new WebcamThread((image) -> {
        synchronized (swapLock) {
            swap = image;
            swapLock.notifyAll();
        }
        System.gc();
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
            
            while (true) {
                System.gc();
                waitForTime(1);
                Logger.fine("Begin cycle wait...");
                synchronized (swapLock) {
                    swap = null;
                    while (swap == null) {
                        swapLock.wait();
                    }
                }
                Logger.fine("End cycle wait...");
                Logger.fine("(0) Begin cycle");
                BufferedImage currentImage = swap;
                processor = processor.useOrRealloc(currentImage.getWidth(), currentImage.getHeight());
                Logger.fine("(1) Find goals");
                List<Goal> goals = processor.findGoals(currentImage,
                        (int) VisionConstants.targetRed.get(), // red target
                        (int) VisionConstants.targetGreen.get(), // green target
                        (int) VisionConstants.targetBlue.get(), // blue target
                        (int) VisionConstants.thresholdRed.get(), // red threshold
                        (int) VisionConstants.thresholdGreen.get(), // green threshold
                        (int) VisionConstants.thresholdBlue.get(), // blue threshold
                        (int) VisionConstants.minGoalPixCount.get(), // min goal pixel count
                        VisionConstants.similarityThreshold.get(), // similarity threshold
                        VisionConstants.aspectRatio.get(), // goal aspect ratio
                        VisionConstants.aspectRatioThreshold.get()); // goal aspect ratio threshold
                Logger.fine("(2) Found goals: " + goals.size() + " -> " + goals);

                Goal target = null;
                for (Goal g : goals) {
                    if (target == null || g.shape.getCount() > target.shape.getCount()) {
                        target = g;
                    }
                }
                
                Logger.fine("(3) Found target, if any: " + target);
                
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
                            Logger.fine("(4a) Turn time");
//                            turnForTime(VisionConstants.minuteRotationTime.get(), Math.signum(yaw - VisionConstants.prelimAligningAngle.get())*VisionConstants.minuteRotationSpeed.get());
                            turnAngle(Math.signum(yaw - VisionConstants.prelimAligningAngle.get())*VisionConstants.minuteRotationAngle.get(), false);
                            Logger.fine("(5a) Angle complete");
                            waitForTime((long)(VisionConstants.cameraSettleTime.get()*1000.0f));
                            Logger.fine("(6a) End cycle");
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
                            turnAngle(Math.signum(yaw - VisionConstants.postMovementTargetAngle.get())*VisionConstants.minuteRotationAngle2.get(), false);
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
                    Logger.fine("(4b) Turn scan");
                    turnAngle(VisionConstants.scanTickAngle.get(), true);
                    Logger.fine("(5b) Angle complete");
                    waitForTime((long)(VisionConstants.cameraSettleTime.get()*1000.0f));
                    Logger.fine("(6b) End angle");
                }
            }
        } finally {
            webcam.setAddress(null);
        }
    }
}
