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
    public static final BooleanInput reverseKangaroo = ZukoAzula.mainTuning.getBoolean("Kangaroo Auto Turn Left", false);

    public AutonomousBaseKangaroo(String modeName) {
        super(modeName);
    }

    public void runKangarooAutonomous() throws InterruptedException, AutonomousModeOverException {
        Kangaroo.upwardCamera.enabled.set(true);
        Kangaroo.forwardCamera.enabled.set(true);
        float direction = reverseKangaroo.get() ? -1 : 1;

        setIntakeArm(0.05f);

        KangarooTargeting.rotationalPidController.integralTotal.set(0);
        KangarooTargeting.forwardPidController.integralTotal.set(0);

        Logger.info("AUTO BEGIN KANGAROO: " + direction);

        while (!Kangaroo.node.hasLink("kangaroo")) {
            waitForTime(20);
        }

        Logger.info("AUTO acquire Kangaroo");

        long currentTime = System.nanoTime();
        long elapsedTime = 0;
        int cycle = 0;
        while (FRC.inAutonomousMode().get()) {
            ++cycle;
            Logger.fine("[LOCAL] AUTO cycle " + cycle + " " + KangarooTargeting.controlSelector.getStateName());
            if (Kangaroo.upwardCamera.hasTarget.get()) {
                if (!KangarooTargeting.controlSelector.isState("upward")) {
                    Logger.fine("[LOCAL] AUTO state upward and spinup");
                    KangarooTargeting.rotationalPidController.integralTotal.set(0);
                    KangarooTargeting.forwardPidController.integralTotal.set(0);
                    KangarooTargeting.controlSelector.setState("upward");
                    spinup();
                }
            } else if (Kangaroo.forwardCamera.hasTarget.get()) {
                if (!KangarooTargeting.controlSelector.isState("forward")) {
                    Logger.fine("[LOCAL] AUTO state forward");
                    KangarooTargeting.rotationalPidController.integralTotal.set(0);
                    KangarooTargeting.forwardPidController.integralTotal.set(0);
                    KangarooTargeting.controlSelector.setState("forward");
                }
            } else {
                Logger.fine("[LOCAL] AUTO no target and rotate");
                KangarooTargeting.controlSelector.setState("none");
                driveVelocity(0.0f);
                turnVelocity(0.4f * direction);
            }

            if (!KangarooTargeting.controlSelector.isState("none")) {
                if (!KangarooTargeting.rotationallyAligned.get()) {
                    Logger.fine("[LOCAL] AUTO turn towards align");
                    driveVelocity(0.0f);
                    turnVelocity(KangarooTargeting.rotationalPidController.get() * direction);
                    elapsedTime = 0;
                } else if (!KangarooTargeting.forwardAligned.get()) {
                    Logger.fine("[LOCAL] AUTO forwards for align");
                    turnVelocity(0.0f);
                    driveVelocity(-KangarooTargeting.forwardPidController.get());
                    elapsedTime = 0;
                } else if (KangarooTargeting.controlSelector.isState("upward")) {
                    elapsedTime += System.nanoTime() - currentTime;

                    turnVelocity(0.0f);
                    driveVelocity(0.0f);

                    currentTime = System.nanoTime();

                    if (elapsedTime > 1.0 * Time.NANOSECONDS_PER_SECOND) {
                        Logger.fine("[LOCAL] AUTO fire");
                        fire(4.0f);
                        break;
                    } else {
                        Logger.fine("[LOCAL] AUTO pause before fire");
                    }
                } else {
                    Logger.fine("[LOCAL] AUTO reset selector");
                    elapsedTime = 0;
                }
            }
            Thread.sleep(5);
        }

        Kangaroo.upwardCamera.enabled.set(false);
        Kangaroo.forwardCamera.enabled.set(false);
    }
}
