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
import ccre.channel.FloatOutput;
import ccre.cluck.Cluck;
import ccre.ctrl.ExtendedMotor;
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
        states.setStateWhen("passive", intakeArmRollerStop.or(spinup.onPress()).or(FRC.startTele));
        states.setStateWhen("forward", intakeArmRollerForward.andNot(spinup));
        states.setStateWhen("backward", intakeArmRollerBackward.andNot(spinup));

        states.getIsState("passive").toFloat(states.getIsState("forward").toFloat(-1, 1), 0).send(intakeArmRollerCAN.simpleControl());

        FloatInput indexerIntakeSpeed = ZukoAzula.mainTuning.getFloat("Indexer Speed During Intake", 1f);
        FloatInput indexerPassiveSpeed = ZukoAzula.mainTuning.getFloat("Roller Passive Speed", 0.1f);

        rollerSpeed.attach(rollerArb.addBehavior("Not Shooting", FRC.inTeleopMode().and(shooter.isStopped)),
                states.getIsState("passive").toFloat(indexerIntakeSpeed.negatedIf(states.getIsState("backward")), indexerPassiveSpeed));

        BooleanInput shouldSpinUp = setupRollersForSpinup(spinup, actuallyFiring);

        shooter.setup(shouldSpinUp);

        fire.and(shooter.isUpToSpeed).onPress().send(actuallyFiring);
    }

    private static TalonExtendedMotor makeLinkedTalons() {
        TalonExtendedMotor talonRight = FRC.talonCAN(11);

        TalonExtendedMotor talonLeft = FRC.talonCAN(10);
        talonLeft.modGeneralConfig().configureReversed(false, true);
        talonLeft.modGeneralConfig().activateFollowerMode(talonRight);

        return talonRight;
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
