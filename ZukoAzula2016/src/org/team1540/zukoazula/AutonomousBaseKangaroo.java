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

    public FloatInput forwardForwardTarget = ZukoAzula.mainTuning.getFloat("Kangaroo Forward Forward Target", -0.8f);
    public FloatInput forwardRotationalTarget = ZukoAzula.mainTuning.getFloat("Kangaroo Forward Rotational Target", 0.0f);
    public FloatInput upwardForwardTarget = ZukoAzula.mainTuning.getFloat("Kangaroo Upward Forward Target", 0.2f);
    public FloatInput upwardRotationalTarget = ZukoAzula.mainTuning.getFloat("Kangaroo Upward Rotational Target", 0.0f);
    
    public FloatInput forwardPixelToGyro = ZukoAzula.mainTuning.getFloat("Kangaroo Forward Pixel to Gyro", 0.181f*160.f);
    public FloatInput upwardPixelToGyro = ZukoAzula.mainTuning.getFloat("Kangaroo Upward Pixel to Gyro", 0.181f*160.f);
    
    public FloatInput forwardP = ZukoAzula.mainTuning.getFloat("Kangaroo Forward P", 0.25f);
    public FloatInput forwardI = ZukoAzula.mainTuning.getFloat("Kangaroo Forward I", 0.01f);
    public FloatInput forwardD = ZukoAzula.mainTuning.getFloat("Kangaroo Forward D", 0.01f);
    
    public FloatInput rotationalP = ZukoAzula.mainTuning.getFloat("Kangaroo Rotational P", 0.02f);
    public FloatInput rotationalI = ZukoAzula.mainTuning.getFloat("Kangaroo Rotational I", 0.01f);
    public FloatInput rotationalD = ZukoAzula.mainTuning.getFloat("Kangaroo Rotational D", 0.01f);

    public StateMachine controlSelector = new StateMachine("none", "none", "forward", "upward");
    
    public BooleanInput rotationallyAligned = controlSelector
            .selectByState(BooleanInput.alwaysFalse, Kangaroo.forwardCamera.centerX.multipliedBy(forwardPixelToGyro).plus(forwardRotationalTarget).absolute().atMost(3.0f),
                    Kangaroo.upwardCamera.centerX.multipliedBy(upwardPixelToGyro).plus(upwardRotationalTarget).absolute().atMost(3.0f));
    
    public BooleanInput horizontallyAligned = controlSelector
            .selectByState(BooleanInput.alwaysFalse, 
                    Kangaroo.forwardCamera.centerY.plus(forwardForwardTarget).absolute().atMost(0.06f),
                    Kangaroo.forwardCamera.centerY.plus(upwardForwardTarget).absolute().atMost(0.06f));
    
    public PIDController forwardPidController = new PIDController(controlSelector.selectByState(FloatInput.zero, Kangaroo.forwardCamera.centerY, Kangaroo.upwardCamera.centerY), 
            rotationallyAligned.toFloat(controlSelector.selectByState(FloatInput.zero, Kangaroo.forwardCamera.centerY, Kangaroo.upwardCamera.centerY),
                    controlSelector.selectByState(FloatInput.zero, 
                    forwardForwardTarget,
                    upwardForwardTarget)), 
            forwardP, forwardI, forwardD);
    
    public PIDController rotationalPidController = new PIDController(HeadingSensor.absoluteYaw, 
            rotationallyAligned.toFloat(controlSelector.selectByState(HeadingSensor.absoluteYaw.plus(FloatInput.zero), 
                    Kangaroo.forwardCamera.lastGyro.plus(Kangaroo.forwardCamera.centerX.multipliedBy(forwardPixelToGyro)).minus(forwardRotationalTarget),
                    Kangaroo.upwardCamera.lastGyro.plus(Kangaroo.upwardCamera.centerX.multipliedBy(upwardPixelToGyro)).minus(upwardRotationalTarget)), HeadingSensor.absoluteYaw), 
            rotationalP, rotationalI, rotationalD);
    
    public AutonomousBaseKangaroo(String modeName) {
        super(modeName);
        
        rotationalPidController.updateWhen(FRC.duringAuto.and(Kangaroo.upwardCamera.hasTarget.or(Kangaroo.forwardCamera.hasTarget)));
        forwardPidController.updateWhen(FRC.duringAuto.and(Kangaroo.upwardCamera.hasTarget.or(Kangaroo.forwardCamera.hasTarget)));
        
        Cluck.publish("Kangaroo Rotational PID", rotationalPidController.plus(0.0f));
        Cluck.publish("Kangaroo Forward PID", forwardPidController.plus(0.0f));
        
        Cluck.publish("Kangaroo Rotational Target", Kangaroo.upwardCamera.centerX.multipliedBy(upwardPixelToGyro).plus(upwardRotationalTarget));
        Cluck.publish("Kangaroo Upward Forward Aligned", horizontallyAligned);
        Cluck.publish("Kangaroo Upward Rotational Aligned", rotationallyAligned);
    }
    
    public void runKangarooAutonomous() throws InterruptedException, AutonomousModeOverException {    
        Kangaroo.upwardCamera.enabled.set(true);
        Kangaroo.forwardCamera.enabled.set(true);
        
        rotationalPidController.integralTotal.set(0);
        forwardPidController.integralTotal.set(0);
        
        while (!Kangaroo.node.hasLink("kangaroo")) {
            waitForTime(20);
        }
        
        long currentTime = System.nanoTime();
        long elapsedTime = 0;
        while (FRC.inAutonomousMode().get()) {            
            if (Kangaroo.upwardCamera.hasTarget.get()) {
                if (!controlSelector.getIsState("upward").get()) {
                    rotationalPidController.integralTotal.set(0);
                    forwardPidController.integralTotal.set(0);
                    controlSelector.setState("upward");
                }
            } else if (Kangaroo.forwardCamera.hasTarget.get()) {
                if (!controlSelector.getIsState("forward").get()) {
                    rotationalPidController.integralTotal.set(0);
                    forwardPidController.integralTotal.set(0);
                    controlSelector.setState("forward");
                }
            } else {
                controlSelector.setState("none");
                driveVelocity(0.0f);
                turnVelocity(0.4f);
            }
            
            if (!controlSelector.getIsState("none").get()) {
                if (!rotationallyAligned.get()) {
                    driveVelocity(0.0f);
                    turnVelocity(rotationalPidController.get());
                    elapsedTime = 0;
                } else if (!horizontallyAligned.get()) {
                    turnVelocity(0.0f);
                    driveVelocity(-forwardPidController.get());
                    elapsedTime = 0;
                } else if (controlSelector.getIsState("upward").get()){
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
