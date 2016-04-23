package org.team1540.kangaroo;

import ccre.channel.BooleanInput;
import ccre.channel.FloatInput;
import ccre.cluck.Cluck;
import ccre.cluck.CluckPublisher;

public class KangarooRobotStatus {
    public static FloatInput targetRed = Cluck.subscribeFI("robot/Vision Target Red", true);
    public static FloatInput targetBlue = Cluck.subscribeFI("robot/Vision Target Blue", true);
    public static FloatInput targetGreen = Cluck.subscribeFI("robot/Vision Target Green", true);
    public static FloatInput thresholdRed = Cluck.subscribeFI("robot/Vision Threshold Red", true);
    public static FloatInput thresholdGreen = Cluck.subscribeFI("robot/Vision Threshold Green", true);
    public static FloatInput thresholdBlue = Cluck.subscribeFI("robot/Vision Threshold Blue", true);
    public static FloatInput minGoalPixCount = Cluck.subscribeFI("robot/Vision Min Goal Pixel Count", true);
    public static FloatInput similarityThreshold = Cluck.subscribeFI("robot/Vision Similarity Threshold", true);
    public static FloatInput aspectRatio = Cluck.subscribeFI("robot/Vision Goal Aspect Ratio", true);
    public static FloatInput aspectRatioThreshold = Cluck.subscribeFI("robot/Vision Goal Aspect Ratio Threshold", true);
    
    public void setup() { }
}
