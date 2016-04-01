package org.team1540.zukoazula;

import java.awt.image.BufferedImage;
import java.util.List;

import org.team1540.vision.Goal;
import org.team1540.vision.ImageProcessor;
import org.team1540.vision.WebcamThread;

import ccre.channel.FloatInput;
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
        webcam.setAddress("10.15.40.13");
        try {
            turnForTime(0.3f, -0.7f);
            synchronized (swapLock) {
                swap = null;
                while (swap == null) {
                    swapLock.wait();
                }
            }
            
            FloatInput targetRed = ZukoAzula.mainTuning.getFloat("Vision Target Red", 205);
            FloatInput targetBlue = ZukoAzula.mainTuning.getFloat("Vision Target Blue", 20);
            FloatInput targetGreen = ZukoAzula.mainTuning.getFloat("Vision Target Green", 10);
            FloatInput thresholdRed = ZukoAzula.mainTuning.getFloat("Vision Threshold Red", 70);
            FloatInput thresholdGreen = ZukoAzula.mainTuning.getFloat("Vision Threshold Green", 70);
            FloatInput thresholdBlue = ZukoAzula.mainTuning.getFloat("Vision Threshold Blue", 20);
            FloatInput minGoalPixCount = ZukoAzula.mainTuning.getFloat("Vision Min Goal Pixel Count", 50);
            FloatInput similarityThreshold = ZukoAzula.mainTuning.getFloat("Vision Similarity Threshold", 0.05f);
            FloatInput aspectRatio = ZukoAzula.mainTuning.getFloat("Vision Goal Aspect Ratio", 3.2f);
            FloatInput aspectRatioThreshold = ZukoAzula.mainTuning.getFloat("Vision Goal Aspect Ratio Threshold", 2.0f);
            FloatInput pixelsPerDegree = ZukoAzula.mainTuning.getFloat("Vision Pixels Per Degree", 1480.0f*(float)Math.PI/180.0f);
            FloatInput prelimAligningAngle = ZukoAzula.mainTuning.getFloat("Vision Prelim Aligning Angle", 0.0f);
            FloatInput prelimAligningEpsilon = ZukoAzula.mainTuning.getFloat("Vision Prelim Aligning Epsilon", 10.0f);
            FloatInput movementTime = ZukoAzula.mainTuning.getFloat("Vision Movement Time", 0.15f);
            FloatInput movementSpeed = ZukoAzula.mainTuning.getFloat("Vision Movement Speed", 0.15f);
            FloatInput movementAligningEpsilon = ZukoAzula.mainTuning.getFloat("Vision Movement Aligning Epsilon", 3.0f);
            FloatInput scanTickTime = ZukoAzula.mainTuning.getFloat("Vision Scan Tick Time", 0.15f);
            FloatInput scanTickSpeed = ZukoAzula.mainTuning.getFloat("Vision Scan Tick Speed", 0.3f);
            FloatInput cameraSettleTime = ZukoAzula.mainTuning.getFloat("Vision Camera Settle Time", 0.1f);
            FloatInput minuteRotationSpeed = ZukoAzula.mainTuning.getFloat("Vision Minute Rotation Speed", 0.4f);
            FloatInput minuteRotationTime = ZukoAzula.mainTuning.getFloat("Vision Minute Rotation Time", 0.1f);
            FloatInput minimumPitch = ZukoAzula.mainTuning.getFloat("Vision Minimum Pitch", 30.0f);
            FloatInput postMovementTargetAngle = ZukoAzula.mainTuning.getFloat("Vision Post Movement Target Angle", -10.0f);
            FloatInput postMovementTargetEpsilon = ZukoAzula.mainTuning.getFloat("Vision Post Movement Target Epsilon", 3.0f);
            
            while (true) {
                waitForTime(1);
                BufferedImage currentImage = swap;
                processor = processor.useOrRealloc(currentImage.getWidth(), currentImage.getHeight());
                List<Goal> goals = processor.findGoals(currentImage,
                        (int) targetRed.get(), // red target
                        (int) targetBlue.get(), // green target
                        (int) targetGreen.get(), // blue target
                        (int) thresholdRed.get(), // red threshold
                        (int) thresholdGreen.get(), // green threshold
                        (int) thresholdBlue.get(), // blue threshold
                        (int) minGoalPixCount.get(), // min goal pixel count
                        (int) similarityThreshold.get(), // similarity threshold
                        (int) aspectRatio.get(), // goal aspect ratio
                        (int) aspectRatioThreshold.get()); // goal aspect ratio threshold

                Goal target = null;
                for (Goal g : goals) {
                    if (target == null || g.shape.getCount() > target.shape.getCount()) {
                        target = g;
                    }
                }
                
                if (target != null) {
                    float bottomX = (target.ll.x + target.lr.x) / 2.0f;
                    float bottomY = (target.ll.y + target.lr.y) / 2.0f;
                    float ppd = pixelsPerDegree.get();
                    float yaw = (bottomX-currentImage.getWidth()) / ppd;
                    float pitch = (bottomY-currentImage.getHeight()) / ppd;
                    if (pitch < minimumPitch.get()) {
                        if (Math.abs(yaw - prelimAligningAngle.get()) < prelimAligningEpsilon.get()) {
                            turnAngle(yaw - prelimAligningAngle.get(), true);
                            waitForTime((long)(cameraSettleTime.get()*1000.0f));
                        } else if (Math.abs(yaw - prelimAligningAngle.get()) < movementAligningEpsilon.get()){
                            turnForTime(minuteRotationTime.get(), Math.signum(yaw - prelimAligningAngle.get())*minuteRotationSpeed.get());
                            waitForTime((long)(cameraSettleTime.get()*1000.0f));
                        } else {
                            driveForTime(movementTime.get(), movementSpeed.get());
                            waitForTime((long)(cameraSettleTime.get()*1000.0f));
                        }
                    } else {
                        // TODO: add spinup?
                        if (Math.abs(yaw - postMovementTargetAngle.get()) < postMovementTargetEpsilon.get()){
                            turnForTime(minuteRotationTime.get(), Math.signum(yaw - postMovementTargetAngle.get())*minuteRotationSpeed.get());
                            waitForTime((long)(cameraSettleTime.get()*1000.0f));
                        } else {
                            fire(2.5f);
                        }
                    }
                } else {
                    turnForTime(scanTickTime.get(), scanTickSpeed.get());
                    waitForTime((long)(cameraSettleTime.get()*1000.0f));
                }
            }
        } finally {
            webcam.setAddress(null);
        }
    }
}
