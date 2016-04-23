package org.team1540.zukoazula;

import ccre.channel.FloatCell;
import ccre.channel.FloatInput;
import ccre.cluck.Cluck;
import ccre.ctrl.PIDController;
import ccre.frc.FRC;
import ccre.instinct.AutonomousModeOverException;

public abstract class AutonomousBaseKangaroo /*extends AutonomousBase*/ {
//    private FloatInput forwardPixelToGyro = ZukoAzula.mainTuning.getFloat("Kangaroo Forward Pixel to Gyro", 0.181f*160.f);
//    private FloatInput upwardPixelToGyro = ZukoAzula.mainTuning.getFloat("Kangaroo Upward Pixel to Gyro", 0.181f*160.f);
//    
//    private FloatInput forwardP = ZukoAzula.mainTuning.getFloat("Kangaroo Forward P", 0.02f);
//    private FloatInput forwardI = ZukoAzula.mainTuning.getFloat("Kangaroo Forward I", 0.01f);
//    private FloatInput forwardD = ZukoAzula.mainTuning.getFloat("Kangaroo Forward D", 0.01f);
//    
//    private FloatInput upwardP = ZukoAzula.mainTuning.getFloat("Kangaroo Upward P", 0.02f);
//    private FloatInput upwardI = ZukoAzula.mainTuning.getFloat("Kangaroo Upward I", 0.01f);
//    private FloatInput upwardD = ZukoAzula.mainTuning.getFloat("Kangaroo Upward D", 0.01f);
//    
//    private PIDController forwardPidController = new PIDController(HeadingSensor.absoluteYaw, 
//            HeadingSensor.absoluteYaw.plus(Kangaroo.upwardCamera.centerX.multipliedBy(forwardPixelToGyro)), forwardP, forwardI, forwardD);
//    private FloatCell rotationSpeed = new FloatCell();
//    private FloatCell forwardSpeed = new FloatCell();
//    
//    public AutonomousBaseKangaroo(String modeName) {
//        super(modeName);
//        
//        pidController.updateWhen(FRC.duringAuto.and(Kangaroo.hasTarget));
//        pidController.send(rotationSpeed);
//        
//        Cluck.publish("Kangaroo Target Error", targetError);
//        Cluck.publish("Kangaroo PID Input", pidController.minus(0));
//    }
//    
//    public void runKangarooAutonomous() throws InterruptedException, AutonomousModeOverException {        
//        pidController.reset();
//        boolean lockedTarget = false;
//        
//        while (!Kangaroo.node.hasLink("kangaroo")) {
//            waitForTime(100);
//        }
//        
//        while (FRC.inAutonomousMode().get()) {
//            if (Kangaroo.hasTarget.get()) {
//                if (Math.abs(targetError.get()) > 4.0f) {
//                    turnVelocity(pidController.get());
//                } else {
//                    lockedTarget = true;
//                    driveVelocity(0.3f);
//                }
//            } else if (!lockedTarget) {
//                turnVelocity(0.5f);
//            } else {
////                driveVelocity(0.0f);
////                driveDistance(3.0f, 0.5f, true);
//                break;
//            }
//        }
//    }
}
