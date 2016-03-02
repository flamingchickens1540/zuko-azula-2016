package org.team1540.zukoazula;

import ccre.channel.BooleanCell;
import ccre.channel.BooleanInput;
import ccre.channel.DerivedBooleanInput;
import ccre.channel.FloatCell;
import ccre.channel.FloatInput;
import ccre.channel.FloatOutput;
import ccre.cluck.Cluck;
import ccre.ctrl.ExtendedMotor.OutputControlMode;
import ccre.ctrl.ExtendedMotorFailureException;
import ccre.drivers.ctre.talon.TalonExtendedMotor;
import ccre.frc.FRC;

public class PIDTalon {
    private final int priority;
    private final TalonExtendedMotor tem;
    private final String name;
    public final FloatInput velocity, speed;
    public final BooleanInput isStopped, isUpToSpeed;
    private final FloatInput targetSpeed;

    public PIDTalon(TalonExtendedMotor tem, String name, FloatInput targetSpeed, int priority) {
        this.tem = tem;
        this.name = name;
        this.targetSpeed = targetSpeed;
        this.priority = priority;
        
        velocity = tem.modEncoder().getEncoderVelocity();
        Cluck.publish(name + " Velocity", velocity);
        speed = velocity.absolute();
        isStopped = this.speed.atMost(ZukoAzula.mainTuning.getFloat(name + " Maximum Stop Speed", 0.1f));
        Cluck.publish(name + " Is Stopped", isStopped);

        FloatInput allowedVariance = ZukoAzula.mainTuning.getFloat(name + " Allowed Variance", 0.1f);

        isUpToSpeed = getThreeState(velocity.atLeast(targetSpeed), velocity.atMost(targetSpeed.minus(allowedVariance.absolute())));
        Cluck.publish(name + " At Speed", isUpToSpeed);


        Cluck.publish(name + " PID P", tem.modPID().getP());
        Cluck.publish(name + " PID I", tem.modPID().getI());
        Cluck.publish(name + " PID D", tem.modPID().getD());
        Cluck.publish(name + " PID F", tem.modPID().getF());
        Cluck.publish(name + " PID I Bounds", tem.modPID().getIntegralBounds());
        Cluck.publish(name + " PID I Accum", tem.modPID().getIAccum());
        
        tem.modEncoder().configureEncoderCodesPerRev(125 * 15); // at the encoder's location. x2 for drive train, x? for arm.

        tem.modGeneralConfig().configureMaximumOutputVoltage(12.0f, -12.0f);
        Cluck.publish(name + " Closed Loop Error", tem.modFeedback().getClosedLoopError());
        Cluck.publish(name + " Output Voltage", tem.modFeedback().getOutputVoltage());
        Cluck.publish(name + " Output Throttle", tem.modFeedback().getThrottle());
        Cluck.publish(name + " Position", tem.modEncoder().getEncoderPosition());
    }

    public static BooleanInput getThreeState(BooleanInput forceTrue, BooleanInput forceFalse) {
        return new DerivedBooleanInput(forceTrue, forceFalse) {
            private boolean state;

            @Override
            protected synchronized boolean apply() {
                if (forceTrue.get()) {
                    state = true;
                }
                if (forceFalse.get()) {
                    state = false;
                }
                return state;
            }
        };
    }

    private static FloatInput wrapForTests(String name, FloatInput normal) {
        Cluck.publish(name + " Feedback", normal);
        FloatCell testControl = new FloatCell();
        Cluck.publish(name + " Override", testControl);
        testControl.setWhen(0, FRC.inTestMode().onPress());
        return FRC.inTestMode().toFloat(normal, testControl);
    }

    public void setup() throws ExtendedMotorFailureException {
        FloatInput control = this.targetSpeed;
        control = wrapForTests(name + " Motor", control);
        FloatOutput speed = PowerManager.managePower(this.priority, tem.asMode(OutputControlMode.SPEED_FIXED));
        if (speed == null) {
            throw new ExtendedMotorFailureException();
        }
        tem.enable();
        Cluck.publish(name + " Expected Speed", control);
        control.send(speed);
    }
}
