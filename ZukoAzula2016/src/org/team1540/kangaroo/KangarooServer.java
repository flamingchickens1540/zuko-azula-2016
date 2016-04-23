package org.team1540.kangaroo;

import ccre.channel.BooleanCell;
import ccre.channel.BooleanOutput;
import ccre.channel.FloatCell;
import ccre.channel.FloatInput;
import ccre.channel.FloatOutput;
import ccre.cluck.Cluck;

public class KangarooServer {
    public static String forwardCameraIp = "10.15.40.12";
    public static String upwardCameraIp = "10.15.40.11";
    
    public static void main(String[] args) {
        Cluck.setupClient("roboRIO-1540-FRC.local", "robot", "kangaroo");
        Cluck.setupServer();
        
        KangarooGoalServer.setup();
        
        FloatCell forwardCameraCenterX = new FloatCell();
        FloatCell forwardCameraCenterY = new FloatCell();
        BooleanCell forwardCameraHasTarget = new BooleanCell();
        KangarooGoalServer forwardCamera = new KangarooGoalServer(forwardCameraIp, 
                Cluck.subscribeBI("robot/forwardCameraEnabled", true), 
                forwardCameraCenterX.asOutput(),
                forwardCameraCenterY.asOutput(),
                forwardCameraHasTarget.asOutput());
        forwardCamera.start();
        Cluck.publish("forwardCameraCenterX", forwardCameraCenterX.asInput());
        Cluck.publish("forwardCameraCenterY", forwardCameraCenterX.asInput());
        Cluck.publish("forwardCameraHasTarget", forwardCameraHasTarget.asInput());
        
        FloatCell upwardCameraCenterX = new FloatCell();
        FloatCell upwardCameraCenterY = new FloatCell();
        BooleanCell upwardCameraHasTarget = new BooleanCell();
        KangarooGoalServer upwardCamera = new KangarooGoalServer(upwardCameraIp, 
                Cluck.subscribeBI("robot/upwardCameraEnabled", true), 
                upwardCameraCenterX.asOutput(),
                upwardCameraCenterY.asOutput(),
                upwardCameraHasTarget.asOutput());
        upwardCamera.start();
        Cluck.publish("upwardCameraCenterX", upwardCameraCenterX.asInput());
        Cluck.publish("upwardCameraCenterY", upwardCameraCenterX.asInput());
        Cluck.publish("upwardCameraHasTarget", upwardCameraHasTarget.asInput());
    }
}
