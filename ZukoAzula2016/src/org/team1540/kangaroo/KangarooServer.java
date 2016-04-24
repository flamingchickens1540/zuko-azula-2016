package org.team1540.kangaroo;

import ccre.channel.BooleanCell;
import ccre.channel.BooleanInput;
import ccre.channel.BooleanOutput;
import ccre.channel.FloatCell;
import ccre.channel.FloatInput;
import ccre.channel.FloatOutput;
import ccre.cluck.Cluck;

public class KangarooServer {
    public static String forwardCameraIp = "10.15.40.12";
    public static String upwardCameraIp = "10.15.40.11";
    
    public static FloatInput currentGyro;
    
    public static void main(String[] args) throws InterruptedException {
        Cluck.setupClient("roboRIO-1540-FRC.local", "robot", "kangaroo");
        Cluck.setupServer(4001);
        
        currentGyro = Cluck.subscribeFI("robot/currentGyro", true);
                        
        FloatCell forwardCameraCenterX = new FloatCell();
        FloatCell forwardCameraCenterY = new FloatCell();
        BooleanCell forwardCameraHasTarget = new BooleanCell();
        FloatCell forwardCameraLastGyro = new FloatCell();
        KangarooGoalServer forwardCamera = new KangarooGoalServer(forwardCameraIp, 
                Cluck.subscribeBI("robot/forwardCameraEnabled", true), 
                forwardCameraCenterX.asOutput(),
                forwardCameraCenterY.asOutput(),
                forwardCameraHasTarget.asOutput(),
                forwardCameraLastGyro.asOutput());
        forwardCamera.start();
        Cluck.publish("forwardCameraCenterX", forwardCameraCenterX.asInput());
        Cluck.publish("forwardCameraCenterY", forwardCameraCenterY.asInput());
        Cluck.publish("forwardCameraHasTarget", forwardCameraHasTarget.asInput());
        Cluck.publish("forwardCameraLastGyro", forwardCameraLastGyro.asInput());
        
        FloatCell upwardCameraCenterX = new FloatCell();
        FloatCell upwardCameraCenterY = new FloatCell();
        BooleanCell upwardCameraHasTarget = new BooleanCell();
        FloatCell upwardCameraLastGyro = new FloatCell();
        KangarooGoalServer upwardCamera = new KangarooGoalServer(upwardCameraIp, 
                Cluck.subscribeBI("robot/upwardCameraEnabled", true), 
                upwardCameraCenterX.asOutput(),
                upwardCameraCenterY.asOutput(),
                upwardCameraHasTarget.asOutput(),
                upwardCameraLastGyro.asOutput());
        upwardCamera.start();
        Cluck.publish("upwardCameraCenterX", upwardCameraCenterX.asInput());
        Cluck.publish("upwardCameraCenterY", upwardCameraCenterY.asInput());
        Cluck.publish("upwardCameraHasTarget", upwardCameraHasTarget.asInput());
        Cluck.publish("upwardCameraLastGyro", upwardCameraLastGyro.asInput());
    }
}
