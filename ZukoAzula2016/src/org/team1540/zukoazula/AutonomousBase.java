package org.team1540.zukoazula;

import java.lang.reflect.Field;
import java.util.ArrayList;

import ccre.channel.BooleanInput;
import ccre.channel.FloatInput;
import ccre.cluck.Cluck;
import ccre.instinct.AutonomousModeOverException;
import ccre.instinct.InstinctModeModule;
import ccre.log.Logger;
import ccre.rconf.RConf;
import ccre.rconf.RConf.Entry;
import ccre.rconf.RConfable;
import ccre.tuning.TuningContext;

public abstract class AutonomousBase extends InstinctModeModule {

    public AutonomousBase(String modeName) {
        super(modeName);
    }

    @Override
    protected void autonomousMain() throws InterruptedException, AutonomousModeOverException {
        try {
            runAutonomous();
        } finally {
            Autonomous.allMotors.set(0);
            Autonomous.fire.set(false);
            Autonomous.warmup.set(false);
            Autonomous.rollersStop.event();
            Autonomous.intakeArm.set(0);
        }
    }

    protected abstract void runAutonomous() throws InterruptedException, AutonomousModeOverException;

    protected void driveForTime(long time, float speed) throws AutonomousModeOverException, InterruptedException {
        try {
            Autonomous.allMotors.set(speed);
            waitForTime(Math.abs(time));
        } finally {
            Autonomous.allMotors.set(0);
        }
    }

    protected void driveDistance(float dist, float speed) throws AutonomousModeOverException, InterruptedException {
        try {
            float start = Autonomous.driveEncoder.get();
            Autonomous.allMotors.set(dist > 0 ? speed : -speed);
            if (dist > 0) {
                waitUntilAtLeast(Autonomous.driveEncoder, start + dist);
            } else {
                waitUntilAtMost(Autonomous.driveEncoder, start + dist);
            }
        } finally {
            Autonomous.allMotors.set(0);
        }
    }

    protected void turnAngle(int angle, int speed) throws AutonomousModeOverException, InterruptedException {
        try {
            float start = Autonomous.absoluteYaw.get();
            Autonomous.turnMotors.set(angle > 0 ? speed : -speed);
            if (angle > 0) {
                waitUntilAtLeast(Autonomous.absoluteYaw, start + angle);
            } else {
                waitUntilAtMost(Autonomous.absoluteYaw, start + angle);
            }
        } finally {
            Autonomous.allMotors.set(0);
        }
    }

    protected void startWarmup() throws AutonomousModeOverException, InterruptedException {
        Autonomous.warmup.set(true);
    }

    protected void fire(long time) throws AutonomousModeOverException, InterruptedException {
        Autonomous.fire.set(true);
        waitForTime(time);
        Autonomous.fire.set(false);
        Autonomous.warmup.set(false);
    }

    protected void rollersForward() throws AutonomousModeOverException, InterruptedException {
        Autonomous.rollersForward.event();
    }

    protected void rollersBackward() throws AutonomousModeOverException, InterruptedException {
        Autonomous.rollersBackward.event();
    }

    protected void rollersStop() throws AutonomousModeOverException, InterruptedException {
        Autonomous.rollersStop.event();
    }

    protected void intakeArmLower(float speed) throws AutonomousModeOverException, InterruptedException {
        Autonomous.intakeArm.set(-Math.abs(speed));
    }

    protected void intakeArmRaise(float speed) throws AutonomousModeOverException, InterruptedException {
        Autonomous.intakeArm.set(Math.abs(speed));
    }

    protected void waitUntilArmStopped() throws AutonomousModeOverException, InterruptedException {
        waitUntil(Autonomous.intakeArmStopped);
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
