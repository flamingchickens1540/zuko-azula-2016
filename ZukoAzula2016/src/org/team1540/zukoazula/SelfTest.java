package org.team1540.zukoazula;

import java.util.ArrayList;

import ccre.channel.BooleanCell;
import ccre.channel.BooleanIO;
import ccre.channel.BooleanInput;
import ccre.channel.FloatInput;
import ccre.channel.FloatOutput;
import ccre.cluck.Cluck;
import ccre.ctrl.ExtendedMotor;
import ccre.ctrl.ExtendedMotor.OutputControlMode;
import ccre.ctrl.ExtendedMotorFailureException;
import ccre.drivers.ctre.talon.TalonExtendedMotor;
import ccre.frc.FRC;
import ccre.instinct.InstinctModule;
import ccre.log.Logger;
import ccre.rconf.RConf;
import ccre.rconf.RConfable;
import ccre.timers.StopwatchTimer;
import ccre.rconf.RConf.Entry;

public class SelfTest {
    private static class SelfTestEntry {
        public final String name;
        public final FloatOutput out;
        public final FloatInput encoder;
        public boolean passLast, stationaryLast, executed;

        public SelfTestEntry(String name, FloatInput enc, FloatOutput out) {
            this.name = name;
            this.encoder = enc;
            this.out = out;
        }
    }
    
    private static StopwatchTimer sinceLast = new StopwatchTimer();

    private static final BooleanIO selfTest = new InstinctModule() {
        @Override
        protected void autonomousMain() throws Throwable {
            Logger.info("Beginning self-test...");
            boolean failed = false;
            for (SelfTestEntry ent : ents) {
                ent.executed = ent.passLast = ent.stationaryLast = false;
            }
            for (SelfTestEntry ent : ents) {
                Logger.fine("Testing " + ent.name);
                float orig = ent.encoder.get();
                waitForTime(300);
                float change = ent.encoder.get();
                if (Math.abs(change - orig) > 0) {
                    failed = true;
                    Logger.warning("Stationary test failed for " + ent.name);
                } else {
                    ent.stationaryLast = true;
                }
                ent.out.set(0.5f);
                waitForTime(300);
                ent.out.set(0.0f);
                waitForTime(600);
                float later = ent.encoder.get();
                ent.out.set(-0.1f);
                waitForTime(100);
                ent.out.set(0.0f);
                boolean isShooter = ent.name.startsWith("shooter-");
                if (Math.abs(later - change) < (isShooter ? 2 : 40)) {
                    failed = true;
                    Logger.warning("Motion test failed for " + ent.name + ": " + Math.abs(later - change));
                } else {
                    ent.passLast = true;
                }
                waitForTime(isShooter ? 1300 : 300);
                ent.executed = true;
            }
            if (failed) {
                Logger.warning("!!! SELF TEST FAILED !!!");
            } else {
                Logger.info("+++ SELF TEST PASSED SUCESSFULLY +++");
            }
            sinceLast.reset();
            selfTest.set(false);
        }
    }.controlIO();
    static {
        Cluck.publishRConf("(PIT) Self Test", new RConfable() {
            @Override
            public Entry[] queryRConf() throws InterruptedException {
                ArrayList<Entry> rents = new ArrayList<>();
                rents.add(RConf.title("Self Test"));
                rents.add(RConf.button(selfTest.get() ? "End Test" : "Begin Test"));
                rents.add(RConf.autoRefresh(selfTest.get() ? 100 : 5000));
                boolean stale = !selfTest.get() && sinceLast.get() >= 30;
                boolean all = true, allexec = true, anyexec = false;
                for (SelfTestEntry ent : ents) {
                    allexec &= ent.executed;
                    anyexec |= ent.executed;
                    String status;
                    if (ent.executed) {
                        if (ent.passLast) {
                            status = (ent.stationaryLast ? (stale ? "STALE" : "PASS") : "EXTRANEOUS");
                        } else {
                            status = (ent.stationaryLast ? "FAILED" : "GLITCHED");
                        }
                    } else {
                        status = "pending";
                    }
                    if (ent.executed) {
                        all &= ent.passLast & ent.stationaryLast;
                    }
                    rents.add(RConf.string(ent.name + ": " + status + " (" + ent.encoder.get() + ")"));
                }
                String overall;
                if (allexec) {
                    overall = (all ? (stale ? "STALE" : "PASS") : "FAIL");
                } else if (anyexec) {
                    overall = (all ? "passing" : "failing");
                } else {
                    overall = "PENDING";
                }
                rents.add(2, RConf.title("OVERALL: " + overall));
                return rents.toArray(new Entry[rents.size()]);
            }

            @Override
            public boolean signalRConf(int field, byte[] data) throws InterruptedException {
                if (field == 1) {
                    selfTest.toggle();
                }
                return true;
            }
        });
    }
    private static final BooleanInput ignoreForSelfTest = selfTest.andNot(FRC.isOnFMS()).or(FRC.inTestMode());
    private static final ArrayList<SelfTestEntry> ents = new ArrayList<>();

    public static FloatOutput wrapNamed(TalonExtendedMotor m, boolean reverse, String name) throws ExtendedMotorFailureException {
        FloatOutput o = m.simpleControl(reverse);
        FloatOutput filtered = o.filter(ignoreForSelfTest);
        ents.add(new SelfTestEntry(name, m.modEncoder().getEncoderPosition(), filtered));
        Cluck.publish("Test Mode (" + name + ")", filtered);
        return o.filterNot(ignoreForSelfTest);
    }

    public static FloatOutput[] wrapDrive(TalonExtendedMotor[] cans, boolean reverse, boolean isRight) throws ExtendedMotorFailureException {
        FloatOutput[] out = new FloatOutput[cans.length];
        FloatInput enc = cans[0].modEncoder().getEncoderPosition();
        for (int i = 0; i < cans.length; i++) {
            FloatOutput sc = cans[i].simpleControl(reverse);
            out[i] = sc.filterNot(ignoreForSelfTest);
            FloatOutput filtered = sc.filter(ignoreForSelfTest);
            ents.add(new SelfTestEntry("drive-" + (isRight ? "right" : "left") + "-" + i, enc, filtered));
            Cluck.publish("Test Mode Drive " + (isRight ? "Right" : "Left") + " " + i, filtered);
        }
        return out;
    }

    public static FloatOutput manageModes(TalonExtendedMotor tem, OutputControlMode mode, String name) throws ExtendedMotorFailureException {
        FloatOutput filtered = tem.asMode(OutputControlMode.VOLTAGE_FRACTIONAL).filter(ignoreForSelfTest);
        ents.add(new SelfTestEntry(name, tem.modEncoder().getEncoderPosition(), filtered));
        Cluck.publish("Test Mode (" + name + ")", filtered);
        ignoreForSelfTest.send((b) -> {
            try {
                if (b) {
                    tem.asMode(OutputControlMode.VOLTAGE_FRACTIONAL);
                } else {
                    tem.asMode(mode);
                }
            } catch (Throwable thr) {
                Logger.severe("Failed to switch control mode", thr);
            }
        });
        return tem.asMode(mode).filterNot(SelfTest.ignoreForSelfTest);
    }

    static void selectiveFollower(TalonExtendedMotor follower, TalonExtendedMotor followee, String name) throws ExtendedMotorFailureException {
        FloatOutput filtered = follower.simpleControl().filter(ignoreForSelfTest);
        ents.add(new SelfTestEntry(name, followee.modEncoder().getEncoderPosition(), filtered));
        Cluck.publish("Test Mode (" + name + ")", filtered);
        ignoreForSelfTest.send((b) -> {
            try {
                if (b) {
                    follower.asMode(OutputControlMode.VOLTAGE_FRACTIONAL);
                } else {
                    follower.modGeneralConfig().activateFollowerMode(followee);
                }
            } catch (Throwable thr) {
                Logger.severe("Failed to switch control mode", thr);
            }
        });
    }
}
