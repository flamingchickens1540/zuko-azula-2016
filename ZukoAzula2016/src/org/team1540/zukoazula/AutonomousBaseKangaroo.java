package org.team1540.zukoazula;

import ccre.channel.FloatCell;
import ccre.channel.FloatInput;
import ccre.ctrl.PIDController;
import ccre.frc.FRC;
import ccre.instinct.AutonomousModeOverException;

public abstract class AutonomousBaseKangaroo extends AutonomousBase {
    private FloatInput input = HeadingSensor.absoluteYaw;
    private FloatInput setPoint = Kangaroo.targetGyro;
    private FloatInput P = ZukoAzula.mainTuning.getFloat("Kangaroo P", 0.02f); // < 1
    private FloatInput I = ZukoAzula.mainTuning.getFloat("Kangaroo I", 0.0f); // 0
    private FloatInput D = ZukoAzula.mainTuning.getFloat("Kangaroo D", 0.0f); // 0
    private PIDController pidController = new PIDController(input, setPoint, P, I, D);
    private FloatCell rotationSpeed = new FloatCell();
    private FloatInput targetError = input.minus(Kangaroo.targetGyro);
    
    public AutonomousBaseKangaroo(String modeName) {
        super(modeName);
        
        pidController.updateWhen(FRC.duringAuto.and(Kangaroo.hasTarget));
        pidController.send(rotationSpeed);
    }
    
    public void runKangarooAutonomous() throws InterruptedException, AutonomousModeOverException {
        while (!Kangaroo.node.hasLink("kangaroo")) {
            waitForTime(100);
        }
        
        while (true) {
            if (Kangaroo.hasTarget.get()) {
                turnVelocity(rotationSpeed.get());
                waitForTime(5);
            } else {
                turnVelocity(0.3f);
            }
        }
    }
}
