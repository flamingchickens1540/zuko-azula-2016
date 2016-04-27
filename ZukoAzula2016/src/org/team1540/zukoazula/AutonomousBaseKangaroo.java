package org.team1540.zukoazula;

import ccre.channel.BooleanInput;
import ccre.channel.FloatInput;
import ccre.cluck.Cluck;
import ccre.ctrl.PIDController;
import ccre.ctrl.StateMachine;
import ccre.frc.FRC;
import ccre.instinct.AutonomousModeOverException;
import ccre.log.Logger;
import ccre.time.Time;

public abstract class AutonomousBaseKangaroo extends AutonomousBase {
    public AutonomousBaseKangaroo(String modeName) {
        super(modeName);
    }
    
    public void runKangarooAutonomous() throws InterruptedException, AutonomousModeOverException {    
        Kangaroo.upwardCamera.enabled.set(true);
        Kangaroo.forwardCamera.enabled.set(true);
        
        KangarooTargeting.rotationalPidController.integralTotal.set(0);
        KangarooTargeting.forwardPidController.integralTotal.set(0);
        
        while (!Kangaroo.node.hasLink("kangaroo")) {
            waitForTime(20);
        }
        
        long currentTime = System.nanoTime();
        long elapsedTime = 0;
        while (FRC.inAutonomousMode().get()) {            
            if (Kangaroo.upwardCamera.hasTarget.get()) {
                if (!KangarooTargeting.controlSelector.getIsState("upward").get()) {
                    KangarooTargeting.rotationalPidController.integralTotal.set(0);
                    KangarooTargeting.forwardPidController.integralTotal.set(0);
                    KangarooTargeting.controlSelector.setState("upward");
                }
            } else if (Kangaroo.forwardCamera.hasTarget.get()) {
                if (!KangarooTargeting.controlSelector.getIsState("forward").get()) {
                    KangarooTargeting.rotationalPidController.integralTotal.set(0);
                    KangarooTargeting.forwardPidController.integralTotal.set(0);
                    KangarooTargeting.controlSelector.setState("forward");
                }
            } else {
                KangarooTargeting.controlSelector.setState("none");
                driveVelocity(0.0f);
                turnVelocity(0.4f);
            }
            
            if (!KangarooTargeting.controlSelector.getIsState("none").get()) {
                if (!KangarooTargeting.rotationallyAligned.get()) {
                    driveVelocity(0.0f);
                    turnVelocity(KangarooTargeting.rotationalPidController.get());
                    elapsedTime = 0;
                } else if (!KangarooTargeting.forwardAligned.get()) {
                    turnVelocity(0.0f);
                    driveVelocity(-KangarooTargeting.forwardPidController.get());
                    elapsedTime = 0;
                } else if (KangarooTargeting.controlSelector.getIsState("upward").get()){
                    elapsedTime += System.nanoTime() - currentTime;
                    
                    turnVelocity(0.0f);
                    driveVelocity(0.0f);
                                        
                    currentTime = System.nanoTime(); 
                    
                    if (elapsedTime > 1.0*Time.NANOSECONDS_PER_SECOND) {
                        fire(4.0f);
                        break;
                    }
                } else {
                    elapsedTime = 0;
                }
            }
        }
        
        Kangaroo.upwardCamera.enabled.set(false);
        Kangaroo.forwardCamera.enabled.set(false);
    }
}
