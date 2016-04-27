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

    // TODO: set the following four FloatInputs to either the default (as it is
    // now) or what is selected by the box
    // on PoultryInspector, depending on a switch that can be set on
    // PoultryInspector. If the box does not exist, make
    // sure it is set to the default.
    public static FloatInput forwardForwardTarget = ZukoAzula.mainTuning.getFloat("Kangaroo Forward Forward Target", -0.7f);
    public static FloatInput forwardRotationalTarget = ZukoAzula.mainTuning.getFloat("Kangaroo Forward Rotational Target", 0.0f);
    public static FloatInput upwardForwardTarget = ZukoAzula.mainTuning.getFloat("Kangaroo Upward Forward Target", 0.18f);
    public static FloatInput upwardRotationalTarget = ZukoAzula.mainTuning.getFloat("Kangaroo Upward Rotational Target", 0.05f);
    
    public static FloatInput forwardPixelToGyro = ZukoAzula.mainTuning.getFloat("Kangaroo Forward Pixel to Gyro", 0.181f*160.f);
    public static FloatInput upwardPixelToGyro = ZukoAzula.mainTuning.getFloat("Kangaroo Upward Pixel to Gyro", 0.181f*160.f);
    
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
                    Kangaroo.forwardCamera.centerX.minus(forwardRotationalTarget).multipliedBy(forwardPixelToGyro).absolute().atMost(3.0f).and(Kangaroo.forwardCamera.hasTarget),
                    Kangaroo.upwardCamera.centerX.minus(upwardRotationalTarget).multipliedBy(upwardPixelToGyro).absolute().atMost(3.0f).and(Kangaroo.upwardCamera.hasTarget));
    
    public static BooleanInput horizontallyAligned = controlSelector
            .selectByState(BooleanInput.alwaysFalse, 
                    Kangaroo.forwardCamera.centerY.plus(forwardForwardTarget).absolute().atMost(0.06f).and(Kangaroo.forwardCamera.hasTarget), // plus is intentional, camera y is reversed
                    Kangaroo.forwardCamera.centerY.plus(upwardForwardTarget).absolute().atMost(0.06f)).and(Kangaroo.upwardCamera.hasTarget);
    
    public static PIDController forwardPidController = new PIDController(controlSelector.selectByState(FloatInput.zero, Kangaroo.forwardCamera.centerY, Kangaroo.upwardCamera.centerY), 
            rotationallyAligned.toFloat(controlSelector.selectByState(FloatInput.zero, Kangaroo.forwardCamera.centerY, Kangaroo.upwardCamera.centerY),
                    controlSelector.selectByState(FloatInput.zero, 
                    forwardForwardTarget,
                    upwardForwardTarget)), 
            forwardP, forwardI, forwardD);
    
    public static PIDController rotationalPidController = new PIDController(HeadingSensor.absoluteYaw, 
            rotationallyAligned.toFloat(controlSelector.selectByState(HeadingSensor.absoluteYaw, 
                    Kangaroo.forwardCamera.lastGyro.plus(Kangaroo.forwardCamera.centerX.minus(forwardRotationalTarget).multipliedBy(forwardPixelToGyro)),
                    Kangaroo.upwardCamera.lastGyro.plus(Kangaroo.upwardCamera.centerX.minus(upwardRotationalTarget).multipliedBy(upwardPixelToGyro))), HeadingSensor.absoluteYaw), 
            rotationalP, rotationalI, rotationalD);
    
    static {
        rotationalPidController.updateWhen(FRC.duringAuto.and(Kangaroo.upwardCamera.hasTarget.or(Kangaroo.forwardCamera.hasTarget)));
        forwardPidController.updateWhen(FRC.duringAuto.and(Kangaroo.upwardCamera.hasTarget.or(Kangaroo.forwardCamera.hasTarget)));

        Cluck.publish("Kangaroo Rotational PID", rotationalPidController.plus(0.0f));
        Cluck.publish("Kangaroo Forward PID", forwardPidController.plus(0.0f));
        Cluck.publish("Kangaroo Upward Rotational Gyro Target", Kangaroo.forwardCamera.lastGyro.plus(Kangaroo.forwardCamera.centerX.minus(forwardRotationalTarget).multipliedBy(forwardPixelToGyro)));
        Cluck.publish("Kangaroo Forward Rotational Gyro Target", Kangaroo.upwardCamera.lastGyro.plus(Kangaroo.upwardCamera.centerX.minus(upwardRotationalTarget).multipliedBy(upwardPixelToGyro)));

        Cluck.publish("Kangaroo Upward Forward Aligned", horizontallyAligned);
        Cluck.publish("Kangaroo Upward Rotational Aligned", rotationallyAligned);
    }
    
    public AutonomousBaseKangaroo(String modeName) {
        super(modeName);
    }

    public void runKangarooAutonomous() throws InterruptedException, AutonomousModeOverException {
        Kangaroo.upwardCamera.enabled.set(true);
        Kangaroo.forwardCamera.enabled.set(true);

        try {
            rotationalPidController.integralTotal.set(0);
            forwardPidController.integralTotal.set(0);

            while (!Cluck.getNode().hasLink("kangaroo")) {
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
                    } else if (controlSelector.getIsState("upward").get()) {
                        elapsedTime += System.nanoTime() - currentTime;

                        turnVelocity(0.0f);
                        driveVelocity(0.0f);

                        currentTime = System.nanoTime();

                        if (elapsedTime > 1.0 * Time.NANOSECONDS_PER_SECOND) {
                            fire(4.0f);
                            break;
                        }
                    } else {
                        elapsedTime = 0;
                    }
                }
            }

        } finally {
            Kangaroo.upwardCamera.enabled.set(false);
            Kangaroo.forwardCamera.enabled.set(false);
        }
    }
}
