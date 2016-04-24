package org.team1540.zukoazula;

import ccre.channel.BooleanInput;
import ccre.channel.FloatCell;
import ccre.channel.FloatInput;
import ccre.channel.FloatOutput;
import ccre.cluck.Cluck;
import ccre.ctrl.PIDController;
import ccre.ctrl.StateMachine;
import ccre.frc.FRC;
import ccre.timers.PauseTimer;

public class KangarooTargeting {
//    private static FloatOutput split(BooleanInput cond, FloatOutput t, FloatOutput f) {
//        return (a) -> {
//            if (cond.get()) {
//                f.set(a);
//            } else {
//                t.set(a);
//            }
//        };
//    }
//    
//    public static BooleanInput autoAlign = ZukoAzula.controlBinding.addBoolean("Autonomously Align");
//    
//    public static FloatInput forwardForwardTarget = ZukoAzula.mainTuning.getFloat("Kangaroo Forward Forward Target", 0.0f);
//    public static FloatInput forwardRotationalTarget = ZukoAzula.mainTuning.getFloat("Kangaroo Forward Rotational Target", 0.0f);
//    public static FloatInput upwardForwardTarget = ZukoAzula.mainTuning.getFloat("Kangaroo Upward Forward Target", 0.0f);
//    public static FloatInput upwardRotationalTarget = ZukoAzula.mainTuning.getFloat("Kangaroo Upward Rotational Target", 0.0f);
//    
//    public static FloatInput forwardPixelToGyro = ZukoAzula.mainTuning.getFloat("Kangaroo Forward Pixel to Gyro", 0.181f*160.f);
//    public static FloatInput upwardPixelToGyro = ZukoAzula.mainTuning.getFloat("Kangaroo Upward Pixel to Gyro", 0.181f*160.f);
//    
//    public static FloatInput forwardP = ZukoAzula.mainTuning.getFloat("Kangaroo Forward P", 0.25f);
//    public static FloatInput forwardI = ZukoAzula.mainTuning.getFloat("Kangaroo Forward I", 0.01f);
//    public static FloatInput forwardD = ZukoAzula.mainTuning.getFloat("Kangaroo Forward D", 0.01f);
//    
//    public static FloatInput rotationalP = ZukoAzula.mainTuning.getFloat("Kangaroo Rotational P", 0.02f);
//    public static FloatInput rotationalI = ZukoAzula.mainTuning.getFloat("Kangaroo Rotational I", 0.01f);
//    public static FloatInput rotationalD = ZukoAzula.mainTuning.getFloat("Kangaroo Rotational D", 0.01f);
//
//    public static StateMachine controlSelector = new StateMachine("none", "none", "forward", "upward");
//    
//    public static BooleanInput rotationallyAligned = controlSelector
//            .selectByState(BooleanInput.alwaysFalse, Kangaroo.forwardCamera.centerX.multipliedBy(forwardPixelToGyro).plus(forwardRotationalTarget).absolute().atMost(2.0f),
//                    Kangaroo.upwardCamera.centerX.multipliedBy(upwardPixelToGyro).plus(upwardRotationalTarget).absolute().atMost(2.0f));
//    
//    public static BooleanInput horizontallyAligned = controlSelector
//            .selectByState(BooleanInput.alwaysFalse, 
//                    Kangaroo.forwardCamera.centerY.plus(forwardForwardTarget).absolute().atMost(0.1f),
//                    Kangaroo.forwardCamera.centerY.plus(upwardForwardTarget).absolute().atMost(0.1f));
//    
//    public static FloatCell leftMotor = new FloatCell();
//    public static FloatCell rightMotor = new FloatCell();
//    
//    public static PIDController forwardPidController = new PIDController(controlSelector.selectByState(FloatInput.zero, Kangaroo.forwardCamera.centerY, Kangaroo.upwardCamera.centerY), 
//            controlSelector.selectByState(FloatInput.zero, 
//                    forwardForwardTarget,
//                    upwardForwardTarget), 
//            forwardP, forwardI, forwardD);
//    
//    public static PIDController rotationalPidController = new PIDController(HeadingSensor.absoluteYaw, 
//            controlSelector.selectByState(HeadingSensor.absoluteYaw.plus(FloatInput.zero), 
//                    HeadingSensor.absoluteYaw.plus(Kangaroo.forwardCamera.centerX.multipliedBy(forwardPixelToGyro)).minus(forwardRotationalTarget),
//                    HeadingSensor.absoluteYaw.plus(Kangaroo.upwardCamera.centerX.multipliedBy(upwardPixelToGyro)).minus(upwardRotationalTarget)), 
//            rotationalP, rotationalI, rotationalD);
//    
//    public static void setup() {
//        controlSelector.onExitState("none").send(() -> {
//            forwardPidController.integralTotal.set(0);
//            rotationalPidController.integralTotal.set(0);
//        });
//        
//        rotationallyAligned.toFloat(controlSelector.selectByState(FloatInput.zero, rotationalPidController, 
//                rotationalPidController), controlSelector.selectByState(FloatInput.zero, forwardPidController,
//                forwardPidController)).send(split(rotationallyAligned, leftMotor.combine(rightMotor.negate()), leftMotor.combine(rightMotor).negate()));
//        
//        forwardPidController.updateWhen(FRC.constantPeriodic.and(controlSelector.getIsState("upward").or(controlSelector.getIsState("forward"))));
//        rotationalPidController.updateWhen(FRC.constantPeriodic.and(controlSelector.getIsState("upward").or(controlSelector.getIsState("forward"))));
//        
//        autoAlign.onPress(KangarooTargeting::upwardCamera);
//        autoAlign.onRelease(KangarooTargeting::disable);
//        
//        Cluck.publish("Kangaroo PID Output", controlSelector.selectByState(FloatInput.zero, forwardPidController, forwardPidController));
//        Cluck.publish("Kangaroo Current Yaw", HeadingSensor.absoluteYaw);
//        
//        Cluck.publish("Kangaroo Target Yaw", controlSelector.selectByState(HeadingSensor.absoluteYaw.plus(FloatInput.zero), 
//                    HeadingSensor.absoluteYaw.plus(Kangaroo.forwardCamera.centerX.multipliedBy(forwardPixelToGyro)).plus(forwardRotationalTarget),
//                    HeadingSensor.absoluteYaw.plus(Kangaroo.upwardCamera.centerX.multipliedBy(upwardPixelToGyro)).plus(upwardRotationalTarget)));
//        
//        PauseTimer pt = new PauseTimer(1000);
//        pt.triggerAtEnd(() -> {
//            if (rotationallyAligned.and(horizontallyAligned).get()) {
//                Shooter.fireEvent();
//            } else {
//                Shooter.stopEvent();
//            }
//        });
//        
//        rotationallyAligned.and(horizontallyAligned).onPress(pt.combine(Shooter::warmupEvent));
//        
//        Cluck.publish("Kangaroo Rotationally Aligned", rotationallyAligned);
//        Cluck.publish("Kangaroo Horizontally Aligned", horizontallyAligned);
//    }
//    
//    public static void disable() {
//        controlSelector.setState("none");
//    }
//    
//    public static void forwardCamera() {
//        controlSelector.setState("forward");
//    }
//    
//    public static void upwardCamera() {
//        controlSelector.setState("upward");
//    }
}
