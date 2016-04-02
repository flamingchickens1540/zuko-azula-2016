package org.team1540.zukoazula;

import java.lang.reflect.Field;
import java.util.ArrayList;

import ccre.channel.BooleanInput;
import ccre.channel.CancelOutput;
import ccre.channel.FloatInput;
import ccre.channel.FloatOutput;
import ccre.cluck.Cluck;
import ccre.ctrl.PIDController;
import ccre.frc.FRC;
import ccre.instinct.AutonomousModeOverException;
import ccre.instinct.InstinctModeModule;
import ccre.log.Logger;
import ccre.rconf.RConf;
import ccre.rconf.RConf.Entry;
import ccre.rconf.RConfable;
import ccre.time.Time;
import ccre.timers.PauseTimer;
import ccre.tuning.TuningContext;

public abstract class AutonomousBase extends InstinctModeModule {

    private static final FloatOutput allMotors = DriveCode.getLeftOutput().combine(DriveCode.getRightOutput());
    private static final FloatOutput turnMotors = DriveCode.getLeftOutput().combine(DriveCode.getRightOutput().negate());

    private static final FloatInput rotateMultiplier = Autonomous.autoTuning.getFloat("Autonomous Rotate Multiplier", 1);
    private static final FloatInput rotateOffset = Autonomous.autoTuning.getFloat("Autonomous Rotate Offset", 20);
    private static final FloatInput rotateSpeed = Autonomous.autoTuning.getFloat("Autonomous Rotate Speed", .5f);
    private static final FloatInput forwardArcSpeed = Autonomous.autoTuning.getFloat("Autonomous Arc Forward Speed", 0.5f);
    private static final FloatInput reverseArcSpeed = Autonomous.autoTuning.getFloat("Autonomous Arc Reverse Speed", 0f);
    private static final FloatInput portcullisWiggleRoom = Autonomous.autoTuning.getFloat("Autonomous Portcullis Wiggle Room", .05f);
    private static final FloatInput turningP = Autonomous.autoTuning.getFloat("Autonomous PID P", .1f);
    private static final FloatInput turningI = Autonomous.autoTuning.getFloat("Autonomous PID I", 0);
    private static final FloatInput turningD = Autonomous.autoTuning.getFloat("Autonomous PID D", 0);
    private static final FloatInput turningError = Autonomous.autoTuning.getFloat("Autonomous Turning Acceptable Error", 2);

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
            Portcullis.getPortcullisOutput().set(0);
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

    protected void driveDistance(float feet, float speed, boolean adjust) throws AutonomousModeOverException, InterruptedException {
        float ticks = feet * DriveCode.ticksPerFoot.get();
        float actualTicks = (adjust && Math.abs(speed) <= .5f) ? ticks - Math.signum(ticks) * DriveCode.getCoastDistance(Math.abs(speed)) : ticks;
        float start = DriveCode.getEncoder().get();
        allMotors.set(feet > 0 ? Math.abs(speed) : -Math.abs(speed));
        if (feet > 0) {
            waitUntilAtLeast(DriveCode.getEncoder(), start + actualTicks);
        } else {
            waitUntilAtMost(DriveCode.getEncoder(), start + actualTicks);
        }
        allMotors.set(0);
    }

    protected void driveUntilStall(float speed) throws AutonomousModeOverException, InterruptedException {
        allMotors.set(speed);
        waitUntil(DriveCode.isStalling());
        allMotors.set(0);
    }

    protected void arcForTime(float seconds, float speed) throws AutonomousModeOverException, InterruptedException {
        if (speed > 0) {
            DriveCode.getLeftOutput().set(speed);
            DriveCode.getRightOutput().set(0);
        } else {
            DriveCode.getLeftOutput().set(0);
            DriveCode.getRightOutput().set(-speed);
        }
        waitSeconds(seconds);
        allMotors.set(0);
    }

    protected void arcAngle(float degrees, boolean adjustAngle) throws AutonomousModeOverException, InterruptedException {
        float adjustedDegrees = Math.abs(degrees) / rotateMultiplier.get() - rotateOffset.get();
        if (adjustAngle && adjustedDegrees > 0) {
            adjustedDegrees *= Math.signum(degrees);
        } else {
            adjustedDegrees = degrees;
        }
        float start = HeadingSensor.absoluteYaw.get();
        if (adjustedDegrees > 0) {
            DriveCode.getLeftOutput().set(forwardArcSpeed.get());
            DriveCode.getRightOutput().set(-reverseArcSpeed.get());
            waitUntilAtLeast(HeadingSensor.absoluteYaw, start + adjustedDegrees);
        } else {
            DriveCode.getLeftOutput().set(reverseArcSpeed.get());
            DriveCode.getRightOutput().set(-forwardArcSpeed.get());
            waitUntilAtMost(HeadingSensor.absoluteYaw, start + adjustedDegrees);
        }
        allMotors.set(0);
    }

    protected void turnForTime(float seconds, float speed) throws AutonomousModeOverException, InterruptedException {
        turnMotors.set(speed);
        waitSeconds(seconds);
        allMotors.set(0);
    }

    protected void turnToAngle(float angle) throws AutonomousModeOverException, InterruptedException {
        FloatInput desiredAngle = FloatInput.always(angle);
        PIDController pid = new PIDController(HeadingSensor.absoluteYaw, desiredAngle, turningP, turningI, turningD);
        pid.setOutputBounds(.5f);
        pid.updateWhen(FRC.constantPeriodic);
        CancelOutput cancel = pid.send(turnMotors);
        waitUntil(HeadingSensor.absoluteYaw.minus(desiredAngle).inRange(turningError.negated(), turningError));
        cancel.cancel();
        allMotors.set(0);
    }

    protected void turnAngle(float degrees, boolean adjustAngle) throws AutonomousModeOverException, InterruptedException {
        float adjustedDegrees = Math.abs(degrees) / rotateMultiplier.get() - rotateOffset.get();
        if (adjustAngle && adjustedDegrees > 0) {
            adjustedDegrees *= Math.signum(degrees);
        } else {
            adjustedDegrees = degrees;
        }
        float start = HeadingSensor.absoluteYaw.get();
        if (adjustedDegrees > 0) {
            turnMotors.set(rotateSpeed.get());
            waitUntilAtLeast(HeadingSensor.absoluteYaw, start + adjustedDegrees);
        } else {
            turnMotors.set(-rotateSpeed.get());
            waitUntilAtMost(HeadingSensor.absoluteYaw, start + adjustedDegrees);
        }
        allMotors.set(0);
    }

    protected void startWarmup() throws AutonomousModeOverException, InterruptedException {
        Shooter.warmupEvent();
    }

    protected void spinup() throws AutonomousModeOverException, InterruptedException {
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

    protected void movePortcullisArmToPosition(float position, float speed) throws AutonomousModeOverException, InterruptedException {
        if (position > Portcullis.getArmHeight().get()) {
            Portcullis.getPortcullisOutput().set(Math.abs(speed));
            waitUntilAtLeast(Portcullis.getArmHeight(), position - portcullisWiggleRoom.get());
        } else {
            Portcullis.getPortcullisOutput().set(-Math.abs(speed));
            waitUntilAtMost(Portcullis.getArmHeight(), position + portcullisWiggleRoom.get());
        }
        Portcullis.getPortcullisOutput().set(0);
    }

    protected void driveUntilPitchOrTimeout(float speed, float desiredPitch, float timeout) throws AutonomousModeOverException, InterruptedException {
        boolean higher = desiredPitch > HeadingSensor.pitchAngle.get();
        PauseTimer timer = new PauseTimer((long) (timeout) * Time.MILLISECONDS_PER_SECOND);
        allMotors.set(speed);
        timer.event();
        if (higher) {
            waitUntilOneOf(HeadingSensor.pitchAngle.atLeast(desiredPitch), timer.not());
        } else {
            waitUntilOneOf(HeadingSensor.pitchAngle.atMost(desiredPitch), timer.not());
        }
        allMotors.set(0);
    }

    @Override
    public void loadSettings(TuningContext ctx) {
        ArrayList<String> settings = new ArrayList<>();
        for (Field f : this.getClass().getDeclaredFields()) {
            Tunable annot = f.getAnnotation(Tunable.class);
            if (annot != null) {
                f.setAccessible(true);
                try {
                    String name = "Auto Mode " + getModeName() + " " + toTitleCase(f.getName());
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
