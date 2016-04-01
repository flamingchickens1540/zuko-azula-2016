package org.team1540.zukoazula;

import java.util.List;

import org.team1540.vision.Goal;

import ccre.channel.FloatInput;

public class VisionConstants {
    public static final FloatInput targetRed = ZukoAzula.mainTuning.getFloat("Vision Target Red", 205);
    public static final FloatInput targetBlue = ZukoAzula.mainTuning.getFloat("Vision Target Blue", 20);
    public static final FloatInput targetGreen = ZukoAzula.mainTuning.getFloat("Vision Target Green", 10);
    public static final FloatInput thresholdRed = ZukoAzula.mainTuning.getFloat("Vision Threshold Red", 70);
    public static final FloatInput thresholdGreen = ZukoAzula.mainTuning.getFloat("Vision Threshold Green", 70);
    public static final FloatInput thresholdBlue = ZukoAzula.mainTuning.getFloat("Vision Threshold Blue", 20);
    public static final FloatInput minGoalPixCount = ZukoAzula.mainTuning.getFloat("Vision Min Goal Pixel Count", 50);
    public static final FloatInput similarityThreshold = ZukoAzula.mainTuning.getFloat("Vision Similarity Threshold", 0.05f);
    public static final FloatInput aspectRatio = ZukoAzula.mainTuning.getFloat("Vision Goal Aspect Ratio", 3.2f);
    public static final FloatInput aspectRatioThreshold = ZukoAzula.mainTuning.getFloat("Vision Goal Aspect Ratio Threshold", 2.0f);
    public static final FloatInput pixelsPerDegree = ZukoAzula.mainTuning.getFloat("Vision Pixels Per Degree", 1480.0f * (float) Math.PI / 180.0f);
    public static final FloatInput prelimAligningAngle = ZukoAzula.mainTuning.getFloat("Vision Prelim Aligning Angle", 0.0f);
    public static final FloatInput prelimAligningEpsilon = ZukoAzula.mainTuning.getFloat("Vision Prelim Aligning Epsilon", 10.0f);
    public static final FloatInput movementTime = ZukoAzula.mainTuning.getFloat("Vision Movement Time", 0.15f);
    public static final FloatInput movementSpeed = ZukoAzula.mainTuning.getFloat("Vision Movement Speed", 0.15f);
    public static final FloatInput movementAligningEpsilon = ZukoAzula.mainTuning.getFloat("Vision Movement Aligning Epsilon", 3.0f);
    public static final FloatInput scanTickTime = ZukoAzula.mainTuning.getFloat("Vision Scan Tick Time", 0.15f);
    public static final FloatInput scanTickSpeed = ZukoAzula.mainTuning.getFloat("Vision Scan Tick Speed", 0.3f);
    public static final FloatInput cameraSettleTime = ZukoAzula.mainTuning.getFloat("Vision Camera Settle Time", 0.1f);
    public static final FloatInput minuteRotationSpeed = ZukoAzula.mainTuning.getFloat("Vision Minute Rotation Speed", 0.4f);
    public static final FloatInput minuteRotationTime = ZukoAzula.mainTuning.getFloat("Vision Minute Rotation Time", 0.1f);
    public static final FloatInput minimumPitch = ZukoAzula.mainTuning.getFloat("Vision Minimum Pitch", 30.0f);
    public static final FloatInput postMovementTargetAngle = ZukoAzula.mainTuning.getFloat("Vision Post Movement Target Angle", -10.0f);
    public static final FloatInput postMovementTargetEpsilon = ZukoAzula.mainTuning.getFloat("Vision Post Movement Target Epsilon", 3.0f);

    public static void setup() {
        // do nothing
    }
}
