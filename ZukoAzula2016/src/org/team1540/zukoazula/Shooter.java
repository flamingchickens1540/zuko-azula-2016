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
import ccre.drivers.ctre.talon.TalonExtendedMotor;
import ccre.frc.FRC;
import ccre.time.Time;

public class Shooter {
    public static void setup() throws ExtendedMotorFailureException {
        TalonExtendedMotor talons = makeTalons();

        FloatOutput motors = talons.simpleControl();
        Cluck.publish("Shooter Motors", motors);

        FloatInput velocity = talons.modEncoder().getEncoderVelocity();
        Cluck.publish("Shooter Velocity", velocity);

        bangBangControl(velocity, makeTrigger(), "Shooter").send(motors);
    }

    private static TalonExtendedMotor makeTalons() {
        TalonExtendedMotor talonLeft = FRC.talonCAN(10);

        TalonExtendedMotor talonRight = FRC.talonCAN(11);
        talonRight.modGeneralConfig().configureReversed(false, true);
        talonRight.modGeneralConfig().activateFollowerMode(talonLeft);

        return talonLeft;
    }

    private static BooleanInput makeTrigger() {
        BooleanInput trigger = ZukoAzula.controlBinding.addBoolean("Shoot");
        ArbitratedBoolean shooterTrigger = ZukoAzula.behaviors.addBoolean();
        shooterTrigger.attach(ZukoAzula.teleop, trigger);
        shooterTrigger.attach(ZukoAzula.pit, trigger);
        return shooterTrigger;
    }

    private static FloatInput bangBangControl(FloatInput velocity, BooleanInput trigger, String name) {
        BooleanInput isBelowVelocity = velocity.atMost(ZukoAzula.mainTuning.getFloat(name + " Target Velocity", 1));
        return trigger.and(isBelowVelocity).toFloat(0.0f, ZukoAzula.mainTuning.getFloat(name + " Maximum Voltage", 1));
    }
}
