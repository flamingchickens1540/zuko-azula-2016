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
    public static final ExtendedMotor shooterCAN = FRC.talonCAN(7);
    public static BooleanInput shooting;
    public static FloatOutput shooterMotor;
    public static FloatInput gearTooth;

    private static FloatCell velocity = new FloatCell();
    private static float lastTicks = 0.0f;
    private static long lastTime = Time.currentTimeNanos();

    public static void setup() throws ExtendedMotorFailureException {
        shooting = RobotTemplate.controlBinding.addBoolean("Shoot");
        shooterMotor = shooterCAN.simpleControl();
        EventInput shootingPressed = shooting.onPress();
        gearTooth = FRC.encoder(0, 0, false, shootingPressed);
        shootingPressed.send(() -> {
            lastTicks = gearTooth.get();
            lastTime = Time.currentTimeNanos();
        });
        gearTooth.onChange().send(() -> {
            long currentTime = Time.currentTimeNanos();
            float currentTicks = gearTooth.get();
            velocity.set((currentTicks - lastTicks) / ((currentTime - lastTime) / (1000000000.0f)));
            lastTime = currentTime;
            lastTicks = currentTicks;
        });
        shooting.and(velocity.atMost(1)).toFloat(0, 1).send(shooterMotor);
        Cluck.publish("Shooter Velocity", velocity.asInput());
        Cluck.publish("Shooter Motor", shooterMotor);
    }
}