package org.team1540.zukoazula;

import org.bouncycastle.crypto.digests.EncodableDigest;

import ccre.channel.BooleanCell;
import ccre.channel.BooleanInput;
import ccre.channel.FloatInput;
import ccre.cluck.Cluck;
import ccre.ctrl.PIDController;
import ccre.ctrl.StateMachine;
import ccre.frc.FRC;

public class KangarooTargeting {

    public static BooleanInput enableAutocorrect = ZukoAzula.controlBinding.addBoolean("Autocorrect Alignment").and(FRC.inTeleopMode());
    
    // TODO: set the following four FloatInputs to either the default (as it is now) or what is selected by the box
    // on PoultryInspector, depending on a switch that can be set on PoultryInspector. If the box does not exist, make
    // sure it is set to the default.
    public static FloatInput forwardForwardTarget = ZukoAzula.mainTuning.getFloat("Kangaroo Forward Forward Target", -0.7f);
    public static FloatInput forwardRotationalTarget = ZukoAzula.mainTuning.getFloat("Kangaroo Forward Rotational Target", 0.0f);
    public static FloatInput upwardForwardTarget = ZukoAzula.mainTuning.getFloat("Kangaroo Upward Forward Target", 0.18f);
    public static FloatInput upwardRotationalTarget = ZukoAzula.mainTuning.getFloat("Kangaroo Upward Rotational Target", 0.05f);
    
    public static FloatInput forwardPixelToGyro = ZukoAzula.mainTuning.getFloat("Kangaroo Forward Pixel to Gyro", 0.181f*160.f);

    public static FloatInput upwardPixelToGyro = ZukoAzula.mainTuning.getFloat("Kangaroo Upward Pixel to Gyro", 0.181f*160.f);

    public static FloatInput forwardRotationallyAlignedThreshold = ZukoAzula.mainTuning.getFloat("Kangaroo Forward Rotationally Aligned Threshold", 4.0f);
    public static FloatInput upwardRotationallyAlignedThreshold = ZukoAzula.mainTuning.getFloat("Kangaroo Upward Rotationally Aligned Threshold", 3.0f);
    public static FloatInput forwardForwardAlignedThreshold = ZukoAzula.mainTuning.getFloat("Kangaroo Forward Forward Aligned Threshold", 0.06f);
    public static FloatInput upwardForwardAlignedThreshold = ZukoAzula.mainTuning.getFloat("Kangaroo Upward Forward Aligned Threshold", 0.1f);
    
    public static FloatInput forwardP = ZukoAzula.mainTuning.getFloat("Kangaroo Forward P", 0.25f);
    public static FloatInput forwardI = ZukoAzula.mainTuning.getFloat("Kangaroo Forward I", 0.01f);
    public static FloatInput forwardD = ZukoAzula.mainTuning.getFloat("Kangaroo Forward D", 0.01f);

    public static FloatInput rotationalP = ZukoAzula.mainTuning.getFloat("Kangaroo Rotational P", 0.02f);
    public static FloatInput rotationalI = ZukoAzula.mainTuning.getFloat("Kangaroo Rotational I", 0.01f);
    public static FloatInput rotationalD = ZukoAzula.mainTuning.getFloat("Kangaroo Rotational D", 0.01f);
    
    public static FloatInput forwardRotationalThreshold = ZukoAzula.mainTuning.getFloat("Kangaroo Forward Rotational Threshold", 5.0f);
    public static FloatInput upwardRotationalThreshold = ZukoAzula.mainTuning.getFloat("Kangaroo Upward Rotational Threshold", 2.0f);

    public static StateMachine controlSelector = new StateMachine("none", "none", "forward", "upward");
    

    public static BooleanInput rotationallyAligned = controlSelector
            .selectByState(BooleanInput.alwaysFalse, 
                    Kangaroo.forwardCamera.centerX.minus(forwardRotationalTarget).multipliedBy(forwardPixelToGyro).absolute().atMost(forwardRotationallyAlignedThreshold).and(Kangaroo.forwardCamera.hasTarget),
                    Kangaroo.upwardCamera.centerX.minus(upwardRotationalTarget).multipliedBy(upwardPixelToGyro).absolute().atMost(upwardRotationallyAlignedThreshold).and(Kangaroo.upwardCamera.hasTarget));
    
    public static BooleanInput forwardAligned = controlSelector
            .selectByState(BooleanInput.alwaysFalse, 
                    Kangaroo.forwardCamera.centerY.plus(forwardForwardTarget).absolute().atMost(forwardForwardAlignedThreshold).and(Kangaroo.forwardCamera.hasTarget), // plus is intentional, camera y is reversed
                    Kangaroo.forwardCamera.centerY.plus(upwardForwardTarget).absolute().atMost(upwardForwardAlignedThreshold)).and(Kangaroo.upwardCamera.hasTarget);
    
    public static PIDController forwardPidController = new PIDController(controlSelector.selectByState(FloatInput.zero, Kangaroo.forwardCamera.centerY, Kangaroo.upwardCamera.centerY), 
            rotationallyAligned.toFloat(controlSelector.selectByState(FloatInput.zero, Kangaroo.forwardCamera.centerY, Kangaroo.upwardCamera.centerY),
                    controlSelector.selectByState(FloatInput.zero, 
                    forwardForwardTarget,
                    upwardForwardTarget)), 
            forwardP, forwardI, forwardD);

    private static final FloatInput forwardPIDInput = Kangaroo.forwardCamera.lastGyro.plus(Kangaroo.forwardCamera.centerX.minus(forwardRotationalTarget).multipliedBy(forwardPixelToGyro));
    private static final FloatInput upwardPIDInput = Kangaroo.upwardCamera.lastGyro.plus(Kangaroo.upwardCamera.centerX.minus(upwardRotationalTarget).multipliedBy(upwardPixelToGyro));
    
    public static PIDController rotationalPidController = new PIDController(HeadingSensor.absoluteYaw, 
            rotationallyAligned.toFloat(controlSelector.selectByState(HeadingSensor.absoluteYaw, 
                    forwardPIDInput,
                    upwardPIDInput), HeadingSensor.absoluteYaw), 
            rotationalP, rotationalI, rotationalD);
    
    public static FloatInput leftMotor = Kangaroo.upwardCamera.hasTarget.and(enableAutocorrect).toFloat(0, rotationallyAligned.toFloat(rotationalPidController, forwardPidController.negated()));
    public static FloatInput rightMotor = Kangaroo.upwardCamera.hasTarget.and(enableAutocorrect).toFloat(0, rotationallyAligned.toFloat(rotationalPidController.negated(), forwardPidController.negated()));
    
    public static void setup() {
        rotationalPidController.updateWhen(FRC.duringAuto.and(Kangaroo.upwardCamera.hasTarget.or(Kangaroo.forwardCamera.hasTarget))
                .or(FRC.duringTele.and(Kangaroo.upwardCamera.hasTarget).and(enableAutocorrect)).andNot(rotationallyAligned));
        forwardPidController.updateWhen(FRC.duringAuto.and(Kangaroo.upwardCamera.hasTarget.or(Kangaroo.forwardCamera.hasTarget))
                .or(FRC.duringTele.and(Kangaroo.upwardCamera.hasTarget).and(enableAutocorrect)).and(rotationallyAligned));
        
        rotationalPidController.setOutputBounds(1.0f);
        forwardPidController.setOutputBounds(1.0f);
        
        controlSelector.setStateWhen("none", FRC.inTeleopMode().andNot(enableAutocorrect.and(Kangaroo.upwardCamera.hasTarget)).onPress());
        controlSelector.setStateWhen("upward", FRC.inTeleopMode().and(enableAutocorrect.and(Kangaroo.upwardCamera.hasTarget)).onPress());
        
        enableAutocorrect.onPress(ZukoAzula.split(Kangaroo.upwardCamera.hasTarget, FRC.joystick1.rumbleLeft().eventSet(0.6f), FRC.joystick1.rumbleRight().eventSet(0.6f))
                .combine(rotationalPidController.integralTotal.eventSet(0))
                .combine(forwardPidController.integralTotal.eventSet(0)));
        enableAutocorrect.onRelease(ZukoAzula.split(Kangaroo.upwardCamera.hasTarget, FRC.joystick1.rumbleLeft().eventSet(0.0f), FRC.joystick1.rumbleRight().eventSet(0.0f)));
        
        Cluck.publish("Kangaroo Rotational PID Output", (FloatInput) rotationalPidController);
        Cluck.publish("Kangaroo Forward PID Output", (FloatInput) forwardPidController);
        Cluck.publish("Kangaroo Forward Rotational Gyro Target", forwardPIDInput);
        Cluck.publish("Kangaroo Upward Rotational Gyro Target", upwardPIDInput);
        Cluck.publish("Kangaroo Upward Forward Aligned", forwardAligned);
        Cluck.publish("Kangaroo Upward Rotational Aligned", rotationallyAligned);
    }
}
