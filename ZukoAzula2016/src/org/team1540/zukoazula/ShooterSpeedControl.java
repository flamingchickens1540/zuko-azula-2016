package org.team1540.zukoazula;

import ccre.channel.BooleanInput;
import ccre.channel.DerivedFloatInput;
import ccre.channel.EventCell;
import ccre.channel.EventInput;
import ccre.channel.FloatCell;
import ccre.channel.FloatInput;
import ccre.channel.FloatOutput;
import ccre.cluck.Cluck;
import ccre.ctrl.ExtendedMotor;
import ccre.ctrl.ExtendedMotorFailureException;
import ccre.frc.FRC;
import ccre.time.Time;

public class ShooterSpeedControl {
    private static final ExtendedMotor shooterCAN = FRC.talonCAN(7);
    private static BooleanInput shooting;
    private static FloatOutput shooterMotor;
    private static FloatInput gearTooth;

    public static void setup() throws ExtendedMotorFailureException {
        shooting = ZukoAzula.controlBinding.addBoolean("Shoot");
        shooterMotor = shooterCAN.simpleControl();
        gearTooth = FRC.counter(0, 1, FRC.startTele);
        bangBangControl(gearTooth, shooting, ZukoAzula.mainTuning.getFloat("Shooter Velocity Target", 1), 0, 1).send(shooterMotor);
        Cluck.publish("Shooter Motor", shooterMotor);
    }

    public static FloatInput bangBangControl(FloatInput input, BooleanInput trigger, FloatInput target, float low, float high) {
        FloatInput velocity = new DerivedFloatInput(input, trigger.onPress()) {
            float lastTicks = input.get();
            long lastTime = Time.currentTimeNanos();

            @Override
            protected float apply() {
                long currentTime = Time.currentTimeNanos();
                float currentTicks = input.get();
                float result = (currentTicks - lastTicks) / ((currentTime - lastTime) / (float) Time.NANOSECONDS_PER_SECOND);
                lastTime = currentTime;
                lastTicks = currentTicks;
                return result;
            }
        };
        return trigger.toFloat(0, velocity.atMost(target).toFloat(low, high));
    }
}