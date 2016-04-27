package org.team1540.zukoazula;

import ccre.channel.BooleanInput;
import ccre.channel.EventCell;
import ccre.channel.EventInput;
import ccre.channel.EventOutput;
import ccre.channel.FloatIO;
import ccre.channel.FloatInput;
import ccre.channel.FloatOutput;
import ccre.cluck.Cluck;
import ccre.ctrl.ExtendedMotorFailureException;
import ccre.ctrl.StateMachine;
import ccre.ctrl.ExtendedMotor.OutputControlMode;
import ccre.drivers.ctre.talon.TalonExtendedMotor;
import ccre.frc.FRC;
import ccre.log.Logger;
import ccre.timers.PauseTimer;

public class Shooter {

    private static final EventCell autonomousWarmup = new EventCell();
    private static final EventCell autonomousFire = new EventCell();
    private static final EventCell autonomousIntake = new EventCell();
    private static final EventCell autonomousEject = new EventCell();
    private static final EventCell autonomousStop = new EventCell();
    public static BooleanInput upToSpeed;
    public static BooleanInput shouldLowerArm;

    public static void setup() throws ExtendedMotorFailureException {
        StateMachine shooterStates = new StateMachine(0,
                "passive", // do nothing
                "ejecting", // when the ball is being ejected from the intake mechanism
                "intaking", // when the intake mechanism is intaking a ball
                "loaded", // when a ball is loaded in the intake mechanism
                "cocking", // when a ball is being cocked so that it is not in contact w/ the flywheel
                "spinup",  // when the flywheel is spinning up to maximum speed
                "firing"); // when the ball is firing

        FloatInput flywheelLowSpeed = ZukoAzula.mainTuning.getFloat("Shooter Flywheel Target Low Speed", -100.0f);
        FloatIO flywheelHighSpeed = ZukoAzula.mainTuning.getFloat("Shooter Flywheel Target High Speed", 2700.0f);

        FloatInput incrementIncrement = ZukoAzula.mainTuning.getFloat("Shooter Flywheel Tune Increment", 50.0f);
        EventInput incrementHighSpeed = ZukoAzula.controlBinding.addEvent("Increment Shooter High Speed");
        flywheelHighSpeed.accumulateWhen(incrementHighSpeed, incrementIncrement);
        EventInput decrementHighSpeed = ZukoAzula.controlBinding.addEvent("Decrement Shooter High Speed");
        flywheelHighSpeed.accumulateWhen(decrementHighSpeed, incrementIncrement.negated());

        FloatInput flywheelTargetVelocity = shooterStates.selectByState(
                FloatInput.zero, // passive
                FloatInput.zero, // ejecting
                flywheelLowSpeed, // intaking
                FloatInput.zero, // loaded
                FloatInput.zero, // cocking
                flywheelHighSpeed, // spinup
                flywheelHighSpeed); // firing
        
        shouldLowerArm = shooterStates.selectByState(
                BooleanInput.alwaysFalse,
                BooleanInput.alwaysFalse,
                BooleanInput.alwaysFalse,
                BooleanInput.alwaysFalse,
                BooleanInput.alwaysTrue,
                BooleanInput.alwaysTrue,
                BooleanInput.alwaysTrue);

        FloatInput flywheelRampingConstant = shooterStates.selectByState(
                FloatInput.always(20.0f), // passive
                FloatInput.always(10.0f), // ejecting
                FloatInput.always(60.0f), // intaking
                FloatInput.always(60.0f), // loaded
                FloatInput.always(60.0f), // cocking
                FloatInput.always(60.0f), // spinup
                FloatInput.always(60.0f)); // firing

        PIDTalon flywheelTalon = new PIDTalon(makeLinkedTalons(), "Shooter Flywheel", flywheelTargetVelocity.withRamping(flywheelRampingConstant, FRC.constantPeriodic), 4);
        flywheelTalon.setup();

        BooleanInput intakeButton = ZukoAzula.controlBinding.addBoolean("Shooter Intake");
        BooleanInput ejectButton = ZukoAzula.controlBinding.addBoolean("Shooter Eject");
        BooleanInput cockAndSpinButton = ZukoAzula.controlBinding.addBoolean("Shooter Spinup");
        BooleanInput fireButton = ZukoAzula.controlBinding.addBoolean("Shooter Fire");
        BooleanInput cancelShooterButton = ZukoAzula.controlBinding.addBoolean("Shooter Cancel Action");

        intakeButton.onPress(ZukoAzula.split(shooterStates.getIsState("intaking"), shooterStates.getStateSetEvent("passive"), shooterStates.getStateSetEvent("intaking")));
        ejectButton.onPress(ZukoAzula.split(shooterStates.getIsState("ejecting"), shooterStates.getStateSetEvent("passive"), shooterStates.getStateSetEvent("ejecting")));
        cockAndSpinButton.onPress(ZukoAzula.split(shooterStates.getIsState("cocking"), shooterStates.getStateSetEvent("passive"), shooterStates.getStateSetEvent("cocking")));
        upToSpeed = flywheelTalon.speed.atLeast(flywheelHighSpeed.minus(ZukoAzula.mainTuning.getFloat("Shooter Flywheel Minimum High Speed Rel", 100.0f)));
        fireButton.and(upToSpeed).onPress(ZukoAzula.split(shooterStates.getIsState("firing"), shooterStates.getStateSetEvent("passive"), shooterStates.getStateSetEvent("firing")));

        autonomousStop.and(FRC.inAutonomousMode()).send(shooterStates.getStateSetEvent("passive"));
        autonomousIntake.and(FRC.inAutonomousMode()).send(shooterStates.getStateSetEvent("intaking"));
        autonomousEject.and(FRC.inAutonomousMode()).send(shooterStates.getStateSetEvent("ejecting"));
        autonomousWarmup.and(FRC.inAutonomousMode()).andNot(shooterStates.getIsState("spinup")).send(shooterStates.getStateSetEvent("cocking"));
        autonomousFire.and(FRC.inAutonomousMode()).and(upToSpeed).send(shooterStates.getStateSetEvent("firing"));

        PauseTimer buzzShooterChange = new PauseTimer(ZukoAzula.mainTuning.getFloat("Joystick Tune Buzz Duration", 0.7f));
        incrementHighSpeed.or(decrementHighSpeed).send(buzzShooterChange);
        PauseTimer buzzRight = new PauseTimer(ZukoAzula.mainTuning.getFloat("Joystick Load Buzz Duration", 0.5f));

        BooleanInput doBuzzRight = shooterStates.getIsState("spinup").or(shooterStates.getIsState("firing")).or(buzzShooterChange);
        BooleanInput doBuzzLeft = shooterStates.getIsState("ejecting").or(shooterStates.getIsState("intaking")).or(buzzShooterChange);

        doBuzzRight.and(FRC.inTeleopMode()).toFloat(0, upToSpeed.toFloat(0.3f, 1.0f)).send(FRC.joystick2.rumbleRight());
        doBuzzLeft.or(buzzRight).and(FRC.inTeleopMode()).toFloat(0, 1.0f).send(FRC.joystick2.rumbleLeft());
        buzzRight.and(FRC.inTeleopMode()).toFloat(0, 1.0f).send(FRC.joystick1.rumbleLeft().combine(FRC.joystick2.rumbleLeft()));
        shooterStates.onEnterState("loaded", buzzRight);

        // Behavior
        shooterStates.setStateWhen("passive", cancelShooterButton.onPress());
        shooterStates.transitionStateWhen("intaking", "loaded", flywheelTalon.speed.atMost(ZukoAzula.mainTuning.getFloat("Shooter Flywheel Maximum Off Speed", 10.0f)).onPress());

        shooterStates.setStateWhen("passive", FRC.startDisabled.or(FRC.startTele).or(FRC.startAuto).or(FRC.startTest));

        // turn off cocking after timer expires
        PauseTimer preloadingTimer = new PauseTimer(ZukoAzula.mainTuning.getFloat("Shooter Cocking Timer", 0.13f));
        preloadingTimer.triggerAtEnd(shooterStates.getStateTransitionEvent("cocking", "spinup"));
        shooterStates.onEnterState("cocking", preloadingTimer);

        FloatOutput intakeRollers = PowerManager.managePower(3, FRC.talonCANControl(7, FRC.MOTOR_FORWARD).combine(FRC.talonCANControl(8, FRC.MOTOR_FORWARD)));
        shooterStates.selectByState(
                FloatInput.zero, // passive
                ZukoAzula.mainTuning.getFloat("Shooter Eject Speed", 1.0f), // ejecting
                ZukoAzula.mainTuning.getFloat("Shooter Intake Speed", -1.0f), // intaking
                FloatInput.zero, // loaded,
                FloatInput.always(1.0f), // cocking
                FloatInput.zero, // spinup
                FloatInput.always(-1.0f)).withRamping(0.1f, FRC.constantPeriodic).send(intakeRollers); // firing

        Cluck.publish("Shooter Flywheel Target Vel", flywheelTargetVelocity);
        Cluck.publish("Shooter State Intaking", shooterStates.getIsState("intaking"));
        Cluck.publish("Shooter Up To Speed", upToSpeed);
    }

    private static TalonExtendedMotor makeLinkedTalons() throws ExtendedMotorFailureException {
        TalonExtendedMotor talonRight = FRC.talonCAN(11);
        TalonExtendedMotor talonLeft = FRC.talonCAN(10);
        talonLeft.modGeneralConfig().configureReversed(false, false);
        SelfTest.selectiveFollower(talonLeft, talonRight, "shooter-secondary");
        return talonRight;
    }

    public static void warmupEvent() {
        autonomousWarmup.event();
    }

    public static void fireEvent() {
        autonomousFire.event();
    }

    public static void intakeEvent() {
        autonomousIntake.event();
    }

    public static void ejectEvent() {
        autonomousEject.event();
    }

    public static void stopEvent() {
        autonomousStop.event();
    }

    public static BooleanInput isAbleToFire() {
        return upToSpeed;
    }
}
