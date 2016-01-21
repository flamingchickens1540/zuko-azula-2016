package org.team1540.zukoazula;

import ccre.channel.BooleanInput;
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

    private static final FloatCell velocity = new FloatCell();

    public static void setup() throws ExtendedMotorFailureException {
        shooting = RobotTemplate.controlBinding.addBoolean("Shoot");
        shooterMotor = shooterCAN.simpleControl();
        EventInput shootingPressed = shooting.onPress();
        gearTooth = FRC.encoder(0, 0, false, shootingPressed);
        velocityOf(gearTooth, shootingPressed).send(velocity);
        shooting.toFloat(0, (bangBangControl(velocity, RobotTemplate.mainTuning.getFloat("Shooter Velocity Target", 1), 0, 1))).send(shooterMotor);
        Cluck.publish("Shooter Velocity", velocity.asInput());
        Cluck.publish("Shooter Motor", shooterMotor);
    }

    public static FloatInput velocityOf(FloatInput input, EventInput start) {
        FloatCell lastTime = new FloatCell(Time.currentTimeNanos());
        FloatCell lastTicks = new FloatCell();
        FloatCell velocity = new FloatCell();
        start.send(() -> {
            lastTicks.set(gearTooth.get());
            lastTime.set(Time.currentTimeNanos());
        });
        input.onChange().send(() -> {
            long currentTime = Time.currentTimeNanos();
            float currentTicks = input.get();
            velocity.set((currentTicks - lastTicks.get()) / ((currentTime - lastTime.get()) / Time.NANOSECONDS_PER_SECOND));
            lastTime.set(currentTime);
            lastTicks.set(currentTicks);
        });
        return velocity;
    }

    public static FloatInput bangBangControl(FloatInput input, FloatInput target, float low, float high) {
        return velocity.atMost(target).toFloat(low, high);
    }
}