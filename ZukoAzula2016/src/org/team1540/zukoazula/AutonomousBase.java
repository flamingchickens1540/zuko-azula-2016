package org.team1540.zukoazula;

import java.lang.reflect.Field;
import java.util.ArrayList;

import ccre.channel.BooleanInput;
import ccre.channel.FloatInput;
import ccre.channel.FloatOutput;
import ccre.cluck.Cluck;
import ccre.instinct.AutonomousModeOverException;
import ccre.instinct.InstinctModeModule;
import ccre.log.Logger;
import ccre.rconf.RConf;
import ccre.rconf.RConf.Entry;
import ccre.rconf.RConfable;
import ccre.time.Time;
import ccre.tuning.TuningContext;

public abstract class AutonomousBase extends InstinctModeModule {

    private static final FloatOutput allMotors = DriveCode.getLeftOutput().combine(DriveCode.getRightOutput());
    private static final FloatOutput turnMotors = DriveCode.getLeftOutput().combine(DriveCode.getRightOutput().negate());

    public AutonomousBase(String modeName) {
        super(modeName);
    }

    @Override
    protected void autonomousMain() throws InterruptedException, AutonomousModeOverException {
        try {
            runAutonomous();
        } finally {
            allMotors.set(0);
            IntakeArm.getArmOutput().set(0);
            Shooter.stopEvent();
        }
    }

    protected abstract void runAutonomous() throws InterruptedException, AutonomousModeOverException;

    protected void waitSeconds(float seconds) throws InterruptedException, AutonomousModeOverException {
        waitForTime((long) (seconds * Time.MILLISECONDS_PER_SECOND));
    }

    protected void driveForTime(float seconds, float speed) throws AutonomousModeOverException, InterruptedException {
        allMotors.set(speed);
        waitSeconds(seconds);
        allMotors.set(0);
    }

    protected void driveDistance(float dist, float speed) throws AutonomousModeOverException, InterruptedException {
        float start = DriveCode.getEncoder().get();
        allMotors.set(dist > 0 ? speed : -speed);
        if (dist > 0) {
            waitUntilAtLeast(DriveCode.getEncoder(), start + dist);
        } else {
            waitUntilAtMost(DriveCode.getEncoder(), start + dist);
        }
    }

    protected void turnForTime(float seconds, int speed) throws AutonomousModeOverException, InterruptedException {
        turnMotors.set(speed);
        waitSeconds(seconds);
        allMotors.set(0);
    }

    protected void startWarmup() throws AutonomousModeOverException, InterruptedException {
        Shooter.warmupEvent();
    }

    protected void fire(float time) throws AutonomousModeOverException, InterruptedException {
        Shooter.warmupEvent();
        waitUntil(Shooter.isAbleToFire());
        Shooter.fireEvent();
        waitSeconds(time);
        Shooter.stopEvent();
    }

    protected void intake(float time) throws AutonomousModeOverException, InterruptedException {
        Shooter.intakeEvent();
        waitSeconds(time);
        Shooter.stopEvent();
    }

    protected void eject(float time, float driveBackSpeed) throws AutonomousModeOverException, InterruptedException {
        Shooter.ejectEvent();
        driveForTime(time, -Math.abs(driveBackSpeed));
        Shooter.stopEvent();
    }

    // speed > 0 raises arm, speed < 0 lowers arm
    protected void setIntakeArm(float speed) throws AutonomousModeOverException, InterruptedException {
        IntakeArm.getArmOutput().set(speed);
    }

    protected void waitUntilArmStopped() throws AutonomousModeOverException, InterruptedException {
        waitUntil(IntakeArm.armIsStopped());
    }

    @Override
    public void loadSettings(TuningContext ctx) {
        ArrayList<String> settings = new ArrayList<>();
        for (Field f : this.getClass().getDeclaredFields()) {
            Tunable annot = f.getAnnotation(Tunable.class);
            if (annot != null) {
                f.setAccessible(true);
                try {
                    String name = "Auto Mode " + getModeName() + " " + toTitleCase(f.getName()) + " +A";
                    if (f.getType() == FloatInput.class) {
                        f.set(this, ctx.getFloat(name, annot.value()));
                    } else if (f.getType() == BooleanInput.class) {
                        f.set(this, ctx.getBoolean(name, annot.valueBoolean()));
                    } else {
                        Logger.severe("Invalid application of @Tunable to " + f.getType());
                        continue;
                    }
                    settings.add(name);
                } catch (Exception e) {
                    Logger.severe("Could not load autonomous configuration for " + this.getClass().getName() + "." + f.getName(), e);
                }
            }
        }
        Cluck.publishRConf("Auto Mode " + getModeName() + " Settings", new RConfable() {
            public boolean signalRConf(int field, byte[] data) throws InterruptedException {
                if (field == 1) {
                    Autonomous.mainModule.setActiveMode(AutonomousBase.this);
                    return true;
                }
                return false;
            }

            public Entry[] queryRConf() throws InterruptedException {
                ArrayList<Entry> entries = new ArrayList<>();
                entries.add(RConf.title("Settings for " + getModeName()));
                if (Autonomous.mainModule.getActiveMode() == AutonomousBase.this) {
                    entries.add(RConf.string("Activate"));
                } else {
                    entries.add(RConf.button("Activate"));
                }
                for (String setting : settings) {
                    entries.add(RConf.cluckRef(setting));
                }
                entries.add(RConf.autoRefresh(10000));
                return entries.toArray(new Entry[entries.size()]);
            }
        });
    }

    private String toTitleCase(String name) {
        StringBuilder sb = new StringBuilder();
        int lastStart = 0;
        for (int i = 1; i < name.length(); i++) {
            if (Character.isUpperCase(name.charAt(i)) || (Character.isDigit(name.charAt(i)) && !Character.isDigit(name.charAt(i - 1)))) {
                sb.append(name.substring(lastStart, i)).append(' ');
                lastStart = i;
            }
        }
        sb.append(name.substring(lastStart));
        sb.setCharAt(0, Character.toUpperCase(sb.charAt(0)));
        return sb.toString();
    }
}
