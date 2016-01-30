package org.team1540.zukoazula;

import ccre.behaviors.ArbitratedBoolean;
import ccre.behaviors.ArbitratedFloat;
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

public class Shooter {
    private static final ExtendedMotor shooterLeftCAN = FRC.talonCAN(10);
    private static final ExtendedMotor shooterRightCAN = FRC.talonCAN(11);
    private static FloatInput encoder = FRC.encoder(0, 1, false, FRC.startTele);

    private static ArbitratedBoolean shooterTrigger = ZukoAzula.behaviors.addBoolean();

    public static void setup() throws ExtendedMotorFailureException {
        shooterTrigger.attach(ZukoAzula.teleop, ZukoAzula.controlBinding.addBoolean("Shoot"));
        FloatOutput shooterMotors = shooterLeftCAN.simpleControl(FRC.MOTOR_FORWARD).combine(shooterRightCAN.simpleControl(FRC.MOTOR_REVERSE));
        bangBangControl(encoder, shooterTrigger, ZukoAzula.mainTuning.getFloat("Shooter Velocity", 1), 0, 1).send(shooterMotors);
        Cluck.publish("Shooter Motors", shooterMotors);
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
