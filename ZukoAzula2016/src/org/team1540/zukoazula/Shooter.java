package org.team1540.zukoazula;

import ccre.behaviors.ArbitratedBoolean;
import ccre.behaviors.ArbitratedFloat;
import ccre.behaviors.Behavior;
import ccre.behaviors.BehaviorArbitrator;
import ccre.channel.BooleanCell;
import ccre.channel.BooleanInput;
import ccre.channel.DerivedEventInput;
import ccre.channel.DerivedFloatInput;
import ccre.channel.DerivedUpdate;
import ccre.channel.EventCell;
import ccre.channel.EventInput;
import ccre.channel.EventOutput;
import ccre.channel.FloatCell;
import ccre.channel.FloatInput;
import ccre.channel.FloatOperation;
import ccre.channel.FloatOutput;
import ccre.cluck.Cluck;
import ccre.ctrl.ExtendedMotor;
import ccre.ctrl.ExtendedMotor.OutputControlMode;
import ccre.ctrl.ExtendedMotorFailureException;
import ccre.ctrl.StateMachine;
import ccre.drivers.ctre.talon.TalonExtendedMotor;
import ccre.drivers.ctre.talon.TalonSRX;
import ccre.frc.FRC;
import ccre.frc.FRCImplementation;
import ccre.frc.FRCImplementationHolder;
import ccre.time.Time;
import ccre.timers.ExpirationTimer;
import ccre.timers.PauseTimer;
import ccre.timers.Ticker;

public class Shooter {

    public static void setup() throws ExtendedMotorFailureException {
        
        StateMachine flywheelTarget = new StateMachine(0, "off", "passive", "high");
        StateMachine flywheelActual = new StateMachine(0, "off", "passive", "high");
        
        StateMachine shooterStates = new StateMachine(0, 
                "passive",
                "exhaling",
                "inhaling",
                "preloading",
                "loaded",
                "prefiring",
                "firing");
        
        TalonExtendedMotor flywheel = makeLinkedTalons();
        flywheel.modEncoder().configureEncoderCodesPerRev(125*15);
        FloatInput flywheelSpeed = flywheel.modEncoder().getEncoderVelocity().absolute();
        FloatOutput flywheelSimple = flywheel.simpleControl().addRamping(0.005f, FRC.constantPeriodic);
        
        BooleanInput inhaleButton = ZukoAzula.controlBinding.addBoolean("Shooter Inhale");
        BooleanInput exhaleButton = ZukoAzula.controlBinding.addBoolean("Shooter Exhale");
        BooleanInput prefireButton = ZukoAzula.controlBinding.addBoolean("Shooter Prefire");
        BooleanInput fireButton = ZukoAzula.controlBinding.addBoolean("Shooter Fire");
        BooleanInput cancelShooterButton = ZukoAzula.controlBinding.addBoolean("Shooter Cancel Inhale/Exhale/Prefire");
        
        // Behavior
        
        flywheelSimple.setWhen(0.0f, FRC.duringTele.and(flywheelTarget.getIsState("off")));
        flywheelSimple.setWhen(-0.068f, FRC.duringTele.and(flywheelTarget.getIsState("passive")));
        flywheelSimple.setWhen(1.0f, FRC.duringTele.and(flywheelTarget.getIsState("high")));
        
        flywheelActual.setStateWhen("off", flywheelSpeed.atMost(10.0f).onPress());
        flywheelActual.setStateWhen("passive", flywheelSpeed.inRange(10.0f, 1000.0f).onPress());
        flywheelActual.setStateWhen("high", flywheelSpeed.atLeast(1500.0f).onPress());
        
        BooleanCell ballLoaded = new BooleanCell(false);
                
        shooterStates.setStateWhen("passive", FRC.startTele);
        shooterStates.setStateWhen("passive", cancelShooterButton.onPress());
        shooterStates.setStateWhen("exhaling", exhaleButton.onPress());
        shooterStates.setStateWhen("inhaling", inhaleButton.onPress());
        shooterStates.setStateWhen("loaded", ballLoaded.onPress());
        shooterStates.setStateWhen("preloading", prefireButton.onPress());
        PauseTimer preloadingTimer = new PauseTimer(ZukoAzula.mainTuning.getFloat("Shooter Preloading Timer", 0.15f));
        preloadingTimer.triggerAtEnd(shooterStates.getStateSetEvent("prefiring"));
        shooterStates.setStateWhen("firing", fireButton.onPress().and(flywheelActual.getIsState("high")));
        
        flywheelTarget.setStateWhen("off", shooterStates.onExitState("inhaling").or(shooterStates.onEnterState("passive")));
        flywheelTarget.setStateWhen("passive", shooterStates.onEnterState("inhaling"));
        flywheelTarget.setStateWhen("high", shooterStates.onEnterState("prefiring"));
        
        ballLoaded.setTrueWhen(flywheelActual.onEnterState("off").and(shooterStates.getIsState("inhaling")));
        ballLoaded.setFalseWhen(shooterStates.getIsState("exhaling")
                .or(shooterStates.getIsState("firing"))
                .or(shooterStates.getIsState("inhaling")).onPress());
        
        FloatOutput intake = FRC.talonSimpleCAN(7, FRC.MOTOR_FORWARD).combine(FRC.talonSimpleCAN(8, FRC.MOTOR_FORWARD));
        
        shooterStates.onEnterState("exhaling", intake.eventSet(ZukoAzula.mainTuning.getFloat("Shooter Exhale Speed", 1.0f)));
        shooterStates.onEnterState("inhaling", intake.eventSet(ZukoAzula.mainTuning.getFloat("Shooter Inhale Speed", 1.0f).negated()));
        shooterStates.onEnterState("loaded", intake.eventSet(0.0f));
        shooterStates.onEnterState("passive", intake.eventSet(0.0f));
        shooterStates.onEnterState("preloading", intake.eventSet(1.0f).combine(preloadingTimer));
        shooterStates.onEnterState("firing", intake.eventSet(-1.0f));
        shooterStates.onEnterState("prefiring", intake.eventSet(0.0f));
        
        shooterStates.setStateWhen("passive", FRC.startDisabled);
        flywheelTarget.setStateWhen("off", FRC.startDisabled);
        shooterStates.setStateWhen("passive", FRC.startTele);
        flywheelTarget.setStateWhen("off", FRC.startTele);
        
        // Publishing
        
        Cluck.publish("Shooter State Passive", shooterStates.getIsState("passive"));
        Cluck.publish("Shooter State Exhaling", shooterStates.getIsState("exhaling"));
        Cluck.publish("Shooter State Inhaling", shooterStates.getIsState("inhaling"));
        Cluck.publish("Shooter State Preloading", shooterStates.getIsState("preloading"));
        Cluck.publish("Shooter State Loaded", shooterStates.getIsState("loaded"));
        Cluck.publish("Shooter State Prefiring", shooterStates.getIsState("prefiring"));
        Cluck.publish("Shooter State Firing", shooterStates.getIsState("firing"));
        Cluck.publish("Shooter State Ball", ballLoaded);
        
        Cluck.publish("Flywheel State O", flywheelActual.getIsState("off"));
        Cluck.publish("Flywheel State L", flywheelActual.getIsState("passive"));
        Cluck.publish("Flywheel State H", flywheelActual.getIsState("high"));
        Cluck.publish("Flywheel State Target O", flywheelTarget.getIsState("off"));
        Cluck.publish("Flywheel State Target L", flywheelTarget.getIsState("passive"));
        Cluck.publish("Flywheel State Target H", flywheelTarget.getIsState("high"));
        Cluck.publish("Flywheel Speed", flywheelSpeed);
    }

    private static TalonExtendedMotor makeLinkedTalons() {
        TalonExtendedMotor talonRight = FRC.talonCAN(11);

        TalonExtendedMotor talonLeft = FRC.talonCAN(10);
        talonLeft.modGeneralConfig().configureReversed(false, true);
        talonLeft.modGeneralConfig().activateFollowerMode(talonRight);

        return talonRight;
    }
}
