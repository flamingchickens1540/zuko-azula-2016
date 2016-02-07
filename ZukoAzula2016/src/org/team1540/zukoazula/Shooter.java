package org.team1540.zukoazula;

import ccre.behaviors.ArbitratedBoolean;
import ccre.behaviors.ArbitratedFloat;
import ccre.behaviors.Behavior;
import ccre.behaviors.BehaviorArbitrator;
import ccre.channel.BooleanCell;
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
import ccre.ctrl.StateMachine;
import ccre.drivers.ctre.talon.TalonExtendedMotor;
import ccre.frc.FRC;
import ccre.time.Time;
import ccre.timers.ExpirationTimer;
import ccre.timers.PauseTimer;

public class Shooter {
    private static final BehaviorArbitrator rollerArb = new BehaviorArbitrator("Rollers");
    private static final ArbitratedFloat rollerSpeed = rollerArb.addFloat();

    private static final TalonExtendedMotor intakeArmRollerCAN = FRC.talonCAN(7);

    public static void setup() throws ExtendedMotorFailureException {
        rollerSpeed.send(FRC.talonSimpleCAN(8, FRC.MOTOR_FORWARD));

        BangBangTalon shooter = new BangBangTalon(makeLinkedTalons(), "Shooter");

        PauseTimer actuallyFiring = new PauseTimer(ZukoAzula.mainTuning.getFloat("Shooter Fire Delay", 0.5f));
        // TODO: take robot modes into account
        BooleanInput warmup = ZukoAzula.controlBinding.addBoolean("Shooter Warm-Up");
        BooleanInput fire = ZukoAzula.controlBinding.addBoolean("Shooter Fire");
        BooleanInput spinup = warmup.or(fire).or(actuallyFiring);

        EventInput intakeArmRollerForward = ZukoAzula.controlBinding.addEvent("Intake Arm Rollers Forward");
        EventInput intakeArmRollerBackward = ZukoAzula.controlBinding.addEvent("Intake Arm Rollers Backward");
        EventInput intakeArmRollerStop = ZukoAzula.controlBinding.addEvent("Intake Arm Rollers Stop");

        StateMachine states = new StateMachine(0, "passive", "forward", "backward");
        states.setStateWhen("passive", intakeArmRollerStop.or(spinup.onRelease()).or(FRC.startTele));
        states.setStateWhen("forward", intakeArmRollerForward);
        states.setStateWhen("backward", intakeArmRollerBackward);

        FloatOutput rollerOutput = intakeArmRollerCAN.simpleControl();
        rollerOutput.setWhen(1, states.onEnterState("forward"));
        rollerOutput.setWhen(0, states.onEnterState("passive"));
        rollerOutput.setWhen(-1, states.onEnterState("backward"));

        FloatInput indexerIntakeSpeed = ZukoAzula.mainTuning.getFloat("Indexer Speed During Intake", 1f);
        FloatInput indexerPassiveSpeed = ZukoAzula.mainTuning.getFloat("Roller Passive Speed", 0.1f);

        FloatCell indexerNotShooting = new FloatCell(indexerPassiveSpeed.get());
        indexerNotShooting.setWhen(indexerIntakeSpeed, states.onEnterState("forward"));
        indexerNotShooting.setWhen(indexerPassiveSpeed, states.onEnterState("passive"));
        indexerNotShooting.setWhen(indexerIntakeSpeed.negated(), states.onEnterState("backward"));

        rollerSpeed.attach(rollerArb.addBehavior("Not Shooting", FRC.inTeleopMode().and(shooter.isStopped)), indexerNotShooting);

        BooleanInput shouldSpinUp = setupRollersForSpinup(spinup, actuallyFiring);

        shooter.setup(shouldSpinUp);

        fire.and(shooter.isUpToSpeed).onPress().send(actuallyFiring);
    }

    private static TalonExtendedMotor makeLinkedTalons() {
        TalonExtendedMotor talonLeft = FRC.talonCAN(10);

        TalonExtendedMotor talonRight = FRC.talonCAN(11);
        talonRight.modGeneralConfig().configureReversed(false, true);
        talonRight.modGeneralConfig().activateFollowerMode(talonLeft);

        return talonLeft;
    }

    private static BooleanInput setupRollersForSpinup(BooleanInput spinup, BooleanInput fire) throws ExtendedMotorFailureException {
        PauseTimer rollbackInterlude = new PauseTimer(ZukoAzula.mainTuning.getFloat("Roller Rollback Duration", 0.1f));
        spinup.onPress().send(rollbackInterlude);

        // TODO: what if race condition?
        Behavior spinupBehavior = rollerArb.addBehavior("Spin Up", spinup);
        Behavior fireBehavior = rollerArb.addBehavior("Fire", fire);
        Behavior rollbackBehavior = rollerArb.addBehavior("Interlude", rollbackInterlude);
        rollerSpeed.attach(spinupBehavior, FloatInput.zero);
        rollerSpeed.attach(fireBehavior, ZukoAzula.mainTuning.getFloat("Roller Fire Speed", 1f));
        rollerSpeed.attach(rollbackBehavior, ZukoAzula.mainTuning.getFloat("Roller Rollback Speed", -0.1f));

        return rollerArb.getIsActive(spinupBehavior);
    }
}
