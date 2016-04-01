package org.team1540.zukoazula;

import ccre.channel.FloatInput;
import ccre.cluck.Cluck;

public class VisionConstantsSub {
    private static FloatInput sub(String name) {
        return Cluck.subscribeFIO("robot/" + name, true);
    }

    public static final FloatInput targetRed = sub("Vision Target Red");
    public static final FloatInput targetBlue = sub("Vision Target Blue");
    public static final FloatInput targetGreen = sub("Vision Target Green");
    public static final FloatInput thresholdRed = sub("Vision Threshold Red");
    public static final FloatInput thresholdGreen = sub("Vision Threshold Green");
    public static final FloatInput thresholdBlue = sub("Vision Threshold Blue");
    public static final FloatInput minGoalPixCount = sub("Vision Min Goal Pixel Count");
    public static final FloatInput similarityThreshold = sub("Vision Similarity Threshold");
    public static final FloatInput aspectRatio = sub("Vision Goal Aspect Ratio");
    public static final FloatInput aspectRatioThreshold = sub("Vision Goal Aspect Ratio Threshold");
    public static final FloatInput pixelsPerDegree = sub("Vision Pixels Per Degree");
    public static final FloatInput prelimAligningAngle = sub("Vision Prelim Aligning Angle");
    public static final FloatInput prelimAligningEpsilon = sub("Vision Prelim Aligning Epsilon");
    public static final FloatInput movementTime = sub("Vision Movement Time");
    public static final FloatInput movementSpeed = sub("Vision Movement Speed");
    public static final FloatInput movementAligningEpsilon = sub("Vision Movement Aligning Epsilon");
    public static final FloatInput scanTickTime = sub("Vision Scan Tick Time");
    public static final FloatInput scanTickSpeed = sub("Vision Scan Tick Speed");
    public static final FloatInput cameraSettleTime = sub("Vision Camera Settle Time");
    public static final FloatInput minuteRotationSpeed = sub("Vision Minute Rotation Speed");
    public static final FloatInput minuteRotationTime = sub("Vision Minute Rotation Time");
    public static final FloatInput minimumPitch = sub("Vision Minimum Pitch");
    public static final FloatInput postMovementTargetAngle = sub("Vision Post Movement Target Angle");
    public static final FloatInput postMovementTargetEpsilon = sub("Vision Post Movement Target Epsilon");

    public static void setup() throws InterruptedException {
        Cluck.getNode().notifyNetworkModified();
        Thread.sleep(500);
        System.out.println(targetRed.get());
    }
}
