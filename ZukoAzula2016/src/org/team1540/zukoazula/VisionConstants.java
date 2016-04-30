package org.team1540.zukoazula;

import java.util.ArrayList;
import java.util.List;

import org.team1540.vision.Goal;

import ccre.channel.FloatIO;
import ccre.channel.FloatInput;
import ccre.cluck.Cluck;
import ccre.rconf.RConfable;
import ccre.rconf.RConf;
import ccre.rconf.RConf.Entry;
import ccre.storage.StorageSegment;

public class VisionConstants {
    public static FloatInput targetRed;
    public static FloatInput targetBlue;
    public static FloatInput targetGreen;
    public static FloatInput thresholdRed;
    public static FloatInput thresholdGreen;
    public static FloatInput thresholdBlue;
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
    public static final FloatInput scanTickAngle = ZukoAzula.mainTuning.getFloat("Vision Scan Tick Angle", 15f);
    public static final FloatInput cameraSettleTime = ZukoAzula.mainTuning.getFloat("Vision Camera Settle Time", 0.1f);
    public static final FloatInput minuteRotationSpeed = ZukoAzula.mainTuning.getFloat("Vision Minute Rotation Speed", 0.4f);
    public static final FloatInput postMovementRotationTime = ZukoAzula.mainTuning.getFloat("Vision Post Movement Rotation Time", 0.1f);
    public static final FloatInput minuteRotationTime = ZukoAzula.mainTuning.getFloat("Vision Minute Rotation Time", 0.1f);
    public static final FloatInput minimumPitch = ZukoAzula.mainTuning.getFloat("Vision Minimum Pitch", 30.0f);
    public static final FloatInput postMovementTargetAngle = ZukoAzula.mainTuning.getFloat("Vision Post Movement Target Angle", -10.0f);
    public static final FloatInput postMovementTargetEpsilon = ZukoAzula.mainTuning.getFloat("Vision Post Movement Target Epsilon", 3.0f);
    public static final FloatInput fireDriveSpeed = ZukoAzula.mainTuning.getFloat("Vision Fire Drive Speed", 0.5f);
    public static final FloatInput fireDriveSeconds = ZukoAzula.mainTuning.getFloat("Vision Fire Drive Seconds", 0.5f);
    public static final FloatInput fireWaitSeconds = ZukoAzula.mainTuning.getFloat("Vision Fire Wait Seconds", 0.4f);
    public static final FloatInput minuteRotationAngle = ZukoAzula.mainTuning.getFloat("Vision Minute Rotation Angle", 10.0f);
    public static final FloatInput minuteRotationAngle2 = ZukoAzula.mainTuning.getFloat("Vision Minute Rotation Angle Two", 10.0f);

    public static void setup() {
        FloatIO targetRed = ZukoAzula.mainTuning.getFloat("Vision Target Red", 255);
        VisionConstants.targetRed = targetRed;
        FloatIO targetGreen = ZukoAzula.mainTuning.getFloat("Vision Target Green", 160);
        VisionConstants.targetGreen = targetGreen;
        FloatIO targetBlue = ZukoAzula.mainTuning.getFloat("Vision Target Blue", 119);
        VisionConstants.targetBlue = targetBlue;
        FloatIO thresholdRed = ZukoAzula.mainTuning.getFloat("Vision Threshold Red", 150);
        VisionConstants.thresholdRed = thresholdRed;
        FloatIO thresholdGreen = ZukoAzula.mainTuning.getFloat("Vision Threshold Green", 84);
        VisionConstants.thresholdGreen = thresholdGreen;
        FloatIO thresholdBlue = ZukoAzula.mainTuning.getFloat("Vision Threshold Blue", 130);
        VisionConstants.thresholdBlue = thresholdBlue;
        FloatIO[] ios = new FloatIO[] { targetRed, targetBlue, targetGreen, thresholdRed, thresholdGreen, thresholdBlue };
        StorageSegment segment = ZukoAzula.mainTuning.getSegment();
        for (int preset : new int[] { 1, 2 }) {
            Cluck.publishRConf("Vision Preset " + preset, new RConfable() {
                @Override
                public boolean signalRConf(int field, byte[] data) throws InterruptedException {
                    if (field == 2) { // save
                        float[] vals = new float[ios.length];
                        for (int i = 0; i < vals.length; i++) {
                            vals[i] = ios[i].get();
                        }
                        save(vals);
                    } else if (field == 3) { // load
                        float[] vals = load();
                        if (vals != null) {
                            for (int i = 0; i < ios.length; i++) {
                                ios[i].set(vals[i]);
                            }
                            return true;
                        }
                    }
                    return false;
                }

                private void save(float[] vals) {
                    StringBuilder sb = new StringBuilder();
                    for (float f : vals) {
                        if (sb.length() != 0) {
                            sb.append(":");
                        }
                        sb.append(f);
                    }
                    segment.setStringForKey("saved-vision-preset-" + preset, sb.toString());
                }

                private float[] load() {
                    String data = segment.getStringForKey("saved-vision-preset-" + preset);
                    String[] out = data.split(":");
                    float[] vals = new float[ios.length];
                    if (out.length != vals.length) {
                        return null;
                    }
                    for (int i = 0; i < out.length; i++) {
                        try {
                            vals[i] = Float.parseFloat(out[i]);
                        } catch (NumberFormatException ex) {
                            return null;
                        }
                    }
                    return vals;
                }

                @Override
                public Entry[] queryRConf() throws InterruptedException {
                    ArrayList<Entry> ents = new ArrayList<>();
                    ents.add(RConf.title("Vision Preset " + preset));
                    ents.add(RConf.autoRefresh(10000));
                    float[] data = load();
                    ents.add(RConf.button("Save to slot")); // slot 2
                    if (data != null) {
                        ents.add(RConf.button("Load from slot")); // slot 3
                        ents.add(RConf.string("Values:"));
                        for (int i = 0; i < ios.length; i++) {
                            ents.add(RConf.string("Saved " + data[i] + " current " + ios[i].get()));
                        }
                    } else {
                        ents.add(RConf.string("[cannot load; no data]"));
                    }
                    return ents.toArray(new Entry[ents.size()]);
                }
            });
        }
    }
}
