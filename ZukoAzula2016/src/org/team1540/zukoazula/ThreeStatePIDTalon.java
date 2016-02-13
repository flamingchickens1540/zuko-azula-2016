package org.team1540.zukoazula;

import ccre.channel.BooleanInput;
import ccre.channel.DerivedBooleanInput;
import ccre.channel.DerivedFloatInput;
import ccre.channel.FloatCell;
import ccre.channel.FloatInput;
import ccre.channel.FloatOutput;
import ccre.cluck.Cluck;
import ccre.ctrl.ExtendedMotor.OutputControlMode;
import ccre.ctrl.ExtendedMotorFailureException;
import ccre.ctrl.StateMachine;
import ccre.drivers.ctre.talon.TalonExtendedMotor;
import ccre.frc.FRC;

public class ThreeStatePIDTalon {
    private final TalonExtendedMotor tem;
    private final String name;
    public final FloatInput velocity, speed;
    public final BooleanInput isStopped, isUpToSpeed;
    private final FloatInput targetSpeed;
    private final StateMachine states;
    private final FloatInput lowSpeed;
    private final FloatInput highSpeed;

    public ThreeStatePIDTalon(TalonExtendedMotor tem, String name, StateMachine states, String high, String low, float defaultHighSpeed, float defaultLowSpeed) {
        this.tem = tem;
        this.name = name;
        this.states = states;
        
        lowSpeed = ZukoAzula.mainTuning.getFloat(name + " Target Low Speed", defaultLowSpeed);
        highSpeed = ZukoAzula.mainTuning.getFloat(name + " Target High Speed", defaultHighSpeed);
        this.targetSpeed = new DerivedFloatInput(this.states.getStateEnterEvent(), lowSpeed, highSpeed) {
            @Override
            public float apply() {
                if (ThreeStatePIDTalon.this.states.getIsState(low).get()) {
                    return ThreeStatePIDTalon.this.lowSpeed.get();
                }
                
                if (ThreeStatePIDTalon.this.states.getIsState(high).get()) {
                    return ThreeStatePIDTalon.this.highSpeed.get();
                }
                
                return 0.0f; // stopped
            }
        };

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

    private static BooleanInput getThreeState(BooleanInput forceTrue, BooleanInput forceFalse) {
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

    public void setup(BooleanInput activate) throws ExtendedMotorFailureException {
        FloatInput control = activate.toFloat(0.0f, this.targetSpeed);
        control = wrapForTests(name + " Motor", control);
        FloatOutput speed = tem.asMode(OutputControlMode.SPEED_FIXED);
        if (speed == null) {
            throw new ExtendedMotorFailureException();
        }
        tem.enable();
        Cluck.publish(name + " Expected Speed", control);
        control.send(speed);
    }
}
