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
import ccre.drivers.ctre.talon.TalonExtendedMotor;
import ccre.frc.FRC;
import ccre.time.Time;
import ccre.timers.ExpirationTimer;
import ccre.timers.PauseTimer;

public class Shooter {
    private static final BehaviorArbitrator rollerArb = new BehaviorArbitrator("Rollers");
    private static final ArbitratedFloat rollerSpeed = rollerArb.addFloat();

    public static void setup() throws ExtendedMotorFailureException {
        rollerSpeed.send(FRC.talonSimpleCAN(8, FRC.MOTOR_FORWARD));

        BangBangTalon shooter = new BangBangTalon(makeLinkedTalons(), "Shooter");

        PauseTimer actuallyFiring = new PauseTimer(ZukoAzula.mainTuning.getFloat("Shooter Fire Delay", 0.5f));
        // TODO: take robot modes into account
        BooleanInput warmup = ZukoAzula.controlBinding.addBoolean("Shooter Warm-Up");
        BooleanInput fire = ZukoAzula.controlBinding.addBoolean("Shooter Fire");
        BooleanInput spinup = warmup.or(fire).or(actuallyFiring);

        FloatInput indexerIntakeSpeed = ZukoAzula.mainTuning.getFloat("Indexer Speed During Intake", 1f);

        FloatCell indexerNotShooting = new FloatCell();
        indexerNotShooting.setWhen(indexerIntakeSpeed, IntakeArm.intakeArmRollerForward);
        indexerNotShooting.setWhen(ZukoAzula.mainTuning.getFloat("Roller Passive Speed", 0.1f), IntakeArm.intakeArmRollerStop.or(spinup.onRelease()).or(FRC.startTele));
        indexerNotShooting.setWhen(indexerIntakeSpeed.negated(), IntakeArm.intakeArmRollerBackward);

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
