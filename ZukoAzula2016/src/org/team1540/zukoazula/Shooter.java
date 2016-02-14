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
        
        StateMachine flywheelTarget = new StateMachine(0, "off", "low", "high");
        StateMachine flywheelActual = new StateMachine(0, "off", "low", "high");
        
        StateMachine shooterStates = new StateMachine(0, 
                "passive", // do nothing
                "ejecting", // when the ball is being ejected from the intake mechanism
                "intaking", // when the intake mechanism is intaking a ball
                "loaded", // when a ball is loaded in the intake mechanism
                "cocking", // when a ball is being cocked so that it is not in contact w/ the flywheel 
                "spinup",  // when the flywheel is spinning up to maximum speed
                "firing"); // when the ball is firing
        
        TalonExtendedMotor flywheel = makeLinkedTalons();
        flywheel.modEncoder().configureEncoderCodesPerRev(125*15);
        FloatInput flywheelSpeed = flywheel.modEncoder().getEncoderVelocity().absolute();
        FloatOutput flywheelSimple = flywheel.simpleControl().addRamping(ZukoAzula.mainTuning.getFloat("Shooter Flywheel Ramping", 0.005f).get(), 
                FRC.constantPeriodic);
        
        BooleanInput inhaleButton = ZukoAzula.controlBinding.addBoolean("Shooter Intake");
        BooleanInput exhaleButton = ZukoAzula.controlBinding.addBoolean("Shooter Eject");
        BooleanInput prefireButton = ZukoAzula.controlBinding.addBoolean("Shooter Spinup");
        BooleanInput fireButton = ZukoAzula.controlBinding.addBoolean("Shooter Fire");
        BooleanInput cancelShooterButton = ZukoAzula.controlBinding.addBoolean("Shooter Cancel Action");
        
        // Behavior
        
        flywheelSimple.setWhen(0.0f, FRC.duringTele.and(flywheelTarget.getIsState("off")));
        flywheelSimple.setWhen(-0.068f, FRC.duringTele.and(flywheelTarget.getIsState("low")));
        flywheelSimple.setWhen(1.0f, FRC.duringTele.and(flywheelTarget.getIsState("high")));
        
        flywheelActual.setStateWhen("off", flywheelSpeed.atMost(ZukoAzula.mainTuning.getFloat("Shooter Flywheel Maximum Off Speed", 10.0f)).onPress());
        flywheelActual.setStateWhen("low", flywheelSpeed.inRange(ZukoAzula.mainTuning.getFloat("Shooter Flywheel Minimum Low Speed", 10.0f), 
                ZukoAzula.mainTuning.getFloat("Shooter Flywheel Maximum Low Speed", 1000.0f)).onPress());
        flywheelActual.setStateWhen("high", flywheelSpeed.atLeast(ZukoAzula.mainTuning.getFloat("Shooter Flywheel Minimum High Speed", 2000.0f)).onPress());
        
        BooleanCell ballLoaded = new BooleanCell(false);
                
        shooterStates.setStateWhen("passive", FRC.startTele);
        shooterStates.setStateWhen("passive", cancelShooterButton.onPress());
        shooterStates.setStateWhen("ejecting", exhaleButton.onPress());
        shooterStates.setStateWhen("intaking", inhaleButton.onPress());
        shooterStates.setStateWhen("loaded", ballLoaded.onPress());
        shooterStates.setStateWhen("cocking", prefireButton.onPress());
        PauseTimer preloadingTimer = new PauseTimer(ZukoAzula.mainTuning.getFloat("Shooter Cocking Timer", 0.15f));
        preloadingTimer.triggerAtEnd(shooterStates.getStateSetEvent("spinup"));
        shooterStates.setStateWhen("firing", fireButton.onPress().and(flywheelActual.getIsState("high")));
        
        flywheelTarget.setStateWhen("off", shooterStates.onExitState("intaking").or(shooterStates.onEnterState("passive")));
        flywheelTarget.setStateWhen("passive", shooterStates.onEnterState("intaking"));
        flywheelTarget.setStateWhen("high", shooterStates.onEnterState("spinup"));
        
        ballLoaded.setTrueWhen(flywheelActual.onEnterState("off").and(shooterStates.getIsState("intaking")));
        ballLoaded.setFalseWhen(shooterStates.getIsState("ejecting")
                .or(shooterStates.getIsState("firing"))
                .or(shooterStates.getIsState("intaking")).onPress());
        
        FloatOutput intake = FRC.talonSimpleCAN(7, FRC.MOTOR_FORWARD).combine(FRC.talonSimpleCAN(8, FRC.MOTOR_FORWARD));
        
        shooterStates.onEnterState("ejecting", intake.eventSet(ZukoAzula.mainTuning.getFloat("Shooter Eject Speed", 1.0f)));
        shooterStates.onEnterState("intaking", intake.eventSet(ZukoAzula.mainTuning.getFloat("Shooter Intake Speed", 1.0f).negated()));
        shooterStates.onEnterState("loaded", intake.eventSet(0.0f));
        shooterStates.onEnterState("passive", intake.eventSet(0.0f));
        shooterStates.onEnterState("cocking", intake.eventSet(1.0f).combine(preloadingTimer));
        shooterStates.onEnterState("firing", intake.eventSet(-1.0f));
        shooterStates.onEnterState("spinup", intake.eventSet(0.0f));
        
        shooterStates.setStateWhen("passive", FRC.startDisabled);
        flywheelTarget.setStateWhen("off", FRC.startDisabled);
        shooterStates.setStateWhen("passive", FRC.startTele);
        flywheelTarget.setStateWhen("off", FRC.startTele);
        
        // Publishing
        
        Cluck.publish("Shooter State Passive", shooterStates.getIsState("passive"));
        Cluck.publish("Shooter State Ejecting", shooterStates.getIsState("ejecting"));
        Cluck.publish("Shooter State Intaking", shooterStates.getIsState("intaking"));
        Cluck.publish("Shooter State Cocking", shooterStates.getIsState("cocking"));
        Cluck.publish("Shooter State Loaded", shooterStates.getIsState("loaded"));
        Cluck.publish("Shooter State Spining Up", shooterStates.getIsState("spinup"));
        Cluck.publish("Shooter State Firing", shooterStates.getIsState("firing"));
        Cluck.publish("Shooter State Ball Loaded", ballLoaded);
        
        Cluck.publish("Shooter Flywheel State O", flywheelActual.getIsState("off"));
        Cluck.publish("Shooter Flywheel State L", flywheelActual.getIsState("low"));
        Cluck.publish("Shooter Flywheel State H", flywheelActual.getIsState("high"));
        Cluck.publish("Shooter Flywheel State Target O", flywheelTarget.getIsState("off"));
        Cluck.publish("Shooter Flywheel State Target L", flywheelTarget.getIsState("low"));
        Cluck.publish("Shooter Flywheel State Target H", flywheelTarget.getIsState("high"));
        Cluck.publish("Shooter Flywheel Speed", flywheelSpeed);
    }

    private static TalonExtendedMotor makeLinkedTalons() {
        TalonExtendedMotor talonRight = FRC.talonCAN(11);

        TalonExtendedMotor talonLeft = FRC.talonCAN(10);
        talonLeft.modGeneralConfig().configureReversed(false, true);
        talonLeft.modGeneralConfig().activateFollowerMode(talonRight);

        return talonRight;
    }
}
