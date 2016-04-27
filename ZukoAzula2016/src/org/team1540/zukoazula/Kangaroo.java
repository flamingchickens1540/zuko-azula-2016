package org.team1540.zukoazula;

import org.team1540.kangaroo.KangarooGoalClient;

import ccre.channel.FloatInput;
import ccre.cluck.Cluck;
import ccre.cluck.CluckNode;
import ccre.cluck.CluckPublisher;
import ccre.cluck.tcp.CluckTCPClient;

public class Kangaroo {
    public static final CluckNode node = new CluckNode();
    public static final CluckTCPClient client = new CluckTCPClient("10.15.40.14:4001", node, "kangaroo", "robot");

    public static final KangarooGoalClient forwardCamera = new KangarooGoalClient("forwardCamera");
    public static final KangarooGoalClient upwardCamera = new KangarooGoalClient("upwardCamera");
    
    public static void setup() {
        client.start();
        CluckPublisher.publish(node, "Vision Target Red", VisionConstants.targetRed);
        CluckPublisher.publish(node, "Vision Target Blue", VisionConstants.targetBlue);
        CluckPublisher.publish(node, "Vision Target Green", VisionConstants.targetGreen);
        CluckPublisher.publish(node, "Vision Threshold Red", VisionConstants.thresholdRed);
        CluckPublisher.publish(node, "Vision Threshold Green", VisionConstants.thresholdGreen);
        CluckPublisher.publish(node, "Vision Threshold Blue", VisionConstants.thresholdBlue);
        CluckPublisher.publish(node, "Vision Min Goal Pixel Count", VisionConstants.minGoalPixCount);
        CluckPublisher.publish(node, "Vision Similarity Threshold", VisionConstants.similarityThreshold);
        CluckPublisher.publish(node, "Vision Goal Aspect Ratio", VisionConstants.aspectRatio);
        CluckPublisher.publish(node, "Vision Goal Aspect Ratio Threshold", VisionConstants.aspectRatioThreshold);
        
        CluckPublisher.publish(node, "currentGyro", HeadingSensor.absoluteYaw);

        Cluck.publish("Kangaroo Forward Camera X", forwardCamera.centerX);
        Cluck.publish("Kangaroo Forward Camera Y", forwardCamera.centerY);
        Cluck.publish("Kangaroo Forward Camera Has Target", forwardCamera.hasTarget);
        Cluck.publish("Kangaroo Forward Camera Enabled", forwardCamera.enabled);

        Cluck.publish("Kangaroo Upward Camera X", upwardCamera.centerX);
        Cluck.publish("Kangaroo Upward Camera Y", upwardCamera.centerY);
        Cluck.publish("Kangaroo Upward Camera Has Target", upwardCamera.hasTarget);
        Cluck.publish("Kangaroo Upward Camera Enabled", upwardCamera.enabled);
    }
}
