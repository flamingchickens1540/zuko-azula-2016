package org.team1540.zukoazula;

import ccre.channel.BooleanCell;
import ccre.channel.BooleanInput;
import ccre.channel.FloatCell;
import ccre.channel.FloatInput;
import ccre.cluck.Cluck;
import ccre.cluck.CluckNode;
import ccre.cluck.CluckPublisher;
import ccre.cluck.tcp.CluckTCPClient;
import ccre.cluck.tcp.CluckTCPServer;
import ccre.frc.FRC;

public class Kangaroo {
    public static final CluckNode node = new CluckNode();
    public static final CluckTCPClient client = new CluckTCPClient("10.15.40.14", 4002, node, "kangaroo", "robot");
    public static final CluckTCPServer server = new CluckTCPServer(node, 4003);
    
    public static FloatInput targetGyro = CluckPublisher.subscribeFI(node, "kangaroo/gyroTarget", true);
    public static BooleanInput autonomousRunning = FRC.inAutonomousMode();
    public static FloatCell pixelToGyro = new FloatCell();
    public static BooleanInput hasTarget = CluckPublisher.subscribeBI(node, "kangaroo/hasTarget", true);
    
    public static void setup() {
        client.start();
        server.start();
                
        Cluck.publish("Kangaroo Target Gyro", targetGyro);
        Cluck.publish("Kangaroo Pixel to Gyro", pixelToGyro);
        
        CluckPublisher.publish(node, "gyroCurrent", HeadingSensor.absoluteYaw);
        CluckPublisher.publish(node, "autonomousRunning", autonomousRunning.or(BooleanInput.alwaysTrue));
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
        CluckPublisher.publish(node, "pixelToGyro", pixelToGyro);
    }                                
}
