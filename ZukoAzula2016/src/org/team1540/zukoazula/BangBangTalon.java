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

public class BangBangTalon {
    private final TalonExtendedMotor tem;
    private final String name;
    public final FloatInput velocity, speed;
    public final BooleanInput isStopped, isUpToSpeed;
    private final FloatInput targetSpeed;

    public BangBangTalon(TalonExtendedMotor tem, String name) {
        this.tem = tem;
        this.name = name;

        velocity = tem.modEncoder().getEncoderVelocity();
        Cluck.publish(name + " Velocity", velocity);
        speed = velocity.absolute();
        isStopped = this.speed.atMost(ZukoAzula.mainTuning.getFloat(name + " Maximum Stop Speed", 0.1f));
        Cluck.publish(name + " Is Stopped", isStopped);

        this.targetSpeed = ZukoAzula.mainTuning.getFloat(name + " Target Speed", 150f);
        FloatInput allowedVariance = ZukoAzula.mainTuning.getFloat(name + " Allowed Variance", 0.1f);

        isUpToSpeed = getThreeState(velocity.atLeast(targetSpeed), velocity.atMost(targetSpeed.minus(allowedVariance.absolute())));
        Cluck.publish(name + " At Speed", isUpToSpeed);
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
        BooleanInput isBelowVelocity = velocity.atMost(targetSpeed);
        FloatInput control = activate.and(isBelowVelocity).toFloat(0.0f, ZukoAzula.mainTuning.getFloat(name + " Maximum Voltage", 12.0f));
        control = wrapForTests(name + " Motor", control);
        FloatOutput output = tem.asMode(OutputControlMode.VOLTAGE_FIXED);
        if (output == null) {
            throw new ExtendedMotorFailureException("VOLTAGE_FIXED mode not supported!");
        }
        tem.enable();
        control.send(output);
    }
}
