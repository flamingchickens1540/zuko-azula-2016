package org.team1540.zukoazula;

import org.team1540.kangaroo.KangarooServer;

import ccre.channel.BooleanCell;
import ccre.channel.BooleanInput;
import ccre.channel.FloatCell;
import ccre.channel.FloatInput;
import ccre.ctrl.PIDController;
import ccre.ctrl.StateMachine;
import ccre.frc.FRC;

public class KangarooTargeting {
    public static FloatInput forwardForwardTarget = ZukoAzula.mainTuning.getFloat("Kangaroo Forward Forward Target", 0.7f);
    public static FloatInput forwardRotationalTarget = ZukoAzula.mainTuning.getFloat("Kangaroo Forward Rotational Target", 5.0f);
    public static FloatInput upwardForwardTarget = ZukoAzula.mainTuning.getFloat("Kangaroo Forward Forward Target", 0.5f);
    public static FloatInput upwardRotationalTarget = ZukoAzula.mainTuning.getFloat("Kangaroo Forward Rotational Target", 0.0f);
    
    private static FloatInput forwardPixelToGyro = ZukoAzula.mainTuning.getFloat("Kangaroo Forward Pixel to Gyro", 0.181f*160.f);
    private static FloatInput upwardPixelToGyro = ZukoAzula.mainTuning.getFloat("Kangaroo Upward Pixel to Gyro", 0.181f*160.f);
    
    private static FloatInput forwardP = ZukoAzula.mainTuning.getFloat("Kangaroo Forward P", 0.02f);
    private static FloatInput forwardI = ZukoAzula.mainTuning.getFloat("Kangaroo Forward I", 0.01f);
    private static FloatInput forwardD = ZukoAzula.mainTuning.getFloat("Kangaroo Forward D", 0.01f);
    
    private static FloatInput rotationalP = ZukoAzula.mainTuning.getFloat("Kangaroo Rotational P", 0.02f);
    private static FloatInput rotationalI = ZukoAzula.mainTuning.getFloat("Kangaroo Rotational I", 0.01f);
    private static FloatInput rotationalD = ZukoAzula.mainTuning.getFloat("Kangaroo Rotational D", 0.01f);

    private static StateMachine controlSelector = new StateMachine("none", "none", "forward", "upward");
    
    private static PIDController forwardPidController = new PIDController(controlSelector.selectByState(FloatInput.zero, Kangaroo.forwardCamera.centerY, Kangaroo.upwardCamera.centerY), 
            controlSelector.selectByState(FloatInput.zero, 
                    forwardForwardTarget,
                    upwardForwardTarget), 
            forwardP, forwardI, forwardD);
    
    private static PIDController rotationalPidController = new PIDController(HeadingSensor.absoluteYaw, 
            controlSelector.selectByState(HeadingSensor.absoluteYaw.plus(FloatInput.zero), 
                    HeadingSensor.absoluteYaw.plus(Kangaroo.forwardCamera.centerX.multipliedBy(forwardPixelToGyro)).plus(forwardRotationalTarget),
                    HeadingSensor.absoluteYaw.plus(Kangaroo.upwardCamera.centerX.multipliedBy(upwardPixelToGyro)).plus(upwardRotationalTarget)), 
            rotationalP, rotationalI, rotationalD);
        
    public static void setup() {
        controlSelector.onExitState("none").send(() -> {
            forwardPidController.reset();
            rotationalPidController.reset();
        });
        
        controlSelector.selectByState(FloatInput.zero, forwardPidController, rotationalPidController)
                .send(DriveCode.getLeftOutput().combine(DriveCode.getRightOutput()));
        
        forwardPidController.updateWhen(FRC.constantPeriodic.and(controlSelector.getIsState("forward")));
        rotationalPidController.updateWhen(FRC.constantPeriodic.and(controlSelector.getIsState("upward")));
        
        BooleanInput autoAlign = ZukoAzula.controlBinding.addBoolean("Autonomously Align");
        autoAlign.onPress(KangarooTargeting::upwardCamera);
        autoAlign.onRelease(KangarooTargeting::disable);
    }
    
    public static void disable() {
        controlSelector.setState("none");
    }
    
    public static void forwardCamera() {
        controlSelector.setState("forward");
    }
    
    public static void upwardCamera() {
        controlSelector.setState("upward");
    }
}
