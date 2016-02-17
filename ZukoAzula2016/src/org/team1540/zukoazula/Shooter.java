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
        
        StateMachine shooterStates = new StateMachine(0, 
                "passive", // do nothing
                "ejecting", // when the ball is being ejected from the intake mechanism
                "intaking", // when the intake mechanism is intaking a ball
                "loaded", // when a ball is loaded in the intake mechanism
                "cocking", // when a ball is being cocked so that it is not in contact w/ the flywheel 
                "spinup",  // when the flywheel is spinning up to maximum speed
                "firing"); // when the ball is firing
        
        FloatInput flywheelLowSpeed = ZukoAzula.mainTuning.getFloat("Shooter Flywheel Target Low Speed", -80.0f);
        FloatInput flywheelHighSpeed = ZukoAzula.mainTuning.getFloat("Shooter Flywheel Target High Speed", 2750.0f);
        FloatInput flywheelTargetVelocity = shooterStates.selectByState(FloatInput.zero, // passive
                FloatInput.zero, // ejecting
                flywheelLowSpeed, // intaking
                FloatInput.zero,
                FloatInput.zero,
                flywheelHighSpeed,
                flywheelHighSpeed);
        
        PIDTalon flywheelTalon = new PIDTalon(makeLinkedTalons(), "Shooter Flywheel", flywheelTargetVelocity.withRamping(15.0f, FRC.constantPeriodic));
        flywheelTalon.setup(BooleanInput.alwaysTrue);
        
        BooleanInput inhaleButton = ZukoAzula.controlBinding.addBoolean("Shooter Intake");
        BooleanInput exhaleButton = ZukoAzula.controlBinding.addBoolean("Shooter Eject");
        BooleanInput prefireButton = ZukoAzula.controlBinding.addBoolean("Shooter Spinup");
        BooleanInput fireButton = ZukoAzula.controlBinding.addBoolean("Shooter Fire");
        BooleanInput cancelShooterButton = ZukoAzula.controlBinding.addBoolean("Shooter Cancel Action");
        
        // Behavior
        shooterStates.setStateWhen("passive", cancelShooterButton.onPress());
        shooterStates.setStateWhen("ejecting", exhaleButton.onPress());
        shooterStates.setStateWhen("intaking", inhaleButton.onPress());
        shooterStates.transitionStateWhen("intaking", "loaded", flywheelTalon.speed.atMost(ZukoAzula.mainTuning.getFloat("Shooter Flywheel Maximum Off Speed", 10.0f)).onPress());
        shooterStates.setStateWhen("cocking", prefireButton.onPress());
        shooterStates.setStateWhen("firing", fireButton.onPress()
                .and(flywheelTalon.speed.atLeast(ZukoAzula.mainTuning.getFloat("Shooter Flywheel Minimum High Speed", 2300.0f))));
       
        shooterStates.setStateWhen("passive", FRC.startDisabled);
        shooterStates.setStateWhen("passive", FRC.startTele);
        shooterStates.setStateWhen("passive", FRC.startAuto);
        shooterStates.setStateWhen("passive", FRC.startTest);
        
        // turn off cocking after timer expires
        PauseTimer preloadingTimer = new PauseTimer(ZukoAzula.mainTuning.getFloat("Shooter Cocking Timer", 0.12f));
        preloadingTimer.triggerAtEnd(shooterStates.getStateTransitionEvent("cocking", "spinup"));   
        shooterStates.onEnterState("cocking", preloadingTimer);
        
        shooterStates.selectByState(FloatInput.zero, // passive
                ZukoAzula.mainTuning.getFloat("Shooter Eject Speed", 1.0f), // ejecting
                ZukoAzula.mainTuning.getFloat("Shooter Intake Speed", -1.0f), // intaking
                FloatInput.zero, // loaded,
                FloatInput.always(1.0f), // cocking
                FloatInput.zero, // spinup
                FloatInput.always(1.0f)).send(FRC.talonSimpleCAN(7, FRC.MOTOR_FORWARD).combine(FRC.talonSimpleCAN(8, FRC.MOTOR_FORWARD))); // firing
    }

    private static TalonExtendedMotor makeLinkedTalons() {
        TalonExtendedMotor talonRight = FRC.talonCAN(11);
        TalonExtendedMotor talonLeft = FRC.talonCAN(10);
        talonLeft.modGeneralConfig().configureReversed(false, true);
        talonLeft.modGeneralConfig().activateFollowerMode(talonRight);
        return talonRight;
    }
}
