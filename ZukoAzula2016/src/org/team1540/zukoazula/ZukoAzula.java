package org.team1540.zukoazula;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;

import ccre.channel.BooleanOutput;
import ccre.channel.EventInput;
import ccre.channel.EventOutput;
import ccre.cluck.Cluck;
import ccre.concurrency.ReporterThread;
import ccre.ctrl.ExtendedMotorFailureException;
import ccre.ctrl.binding.ControlBindingCreator;
import ccre.frc.FRC;
import ccre.frc.FRCApplication;
import ccre.log.Logger;
import ccre.rconf.RConf.Entry;
import ccre.rconf.RConf;
import ccre.rconf.RConfable;
import ccre.scheduler.Scheduler;
import ccre.storage.Storage;
import ccre.time.Time;
import ccre.timers.Ticker;
import ccre.tuning.TuningContext;
import sun.management.HotSpotDiagnostic;

public class ZukoAzula implements FRCApplication {

    public static final ControlBindingCreator controlBinding = FRC.controlBinding();
    public static final TuningContext mainTuning = new TuningContext("MainTuning").publishSavingEvent();

    @Override
    public void setupRobot() throws ExtendedMotorFailureException {
        Logger.info("🐣 CHEEP CHEEP 🐣");
        // Instrumentation.setup();

        VisionConstants.setup();
        HeadingSensor.setup();
//        KangarooTargeting.setup();
        DriveCode.setup();
        Shooter.setup();
        Portcullis.setup();
        IntakeArm.setup();
        ChallengeBrake.setup();
        Kangaroo.setup();
        Autonomous.setup();

        Cluck.publishRConf("Diagnostics", new RConfable() {
            @Override
            public Entry[] queryRConf() throws InterruptedException {
                ArrayList<Entry> entries = new ArrayList<>();
                entries.add(RConf.title("Diagnostics"));
                entries.add(RConf.string("Left Drive Encoder: " + DriveCode.leftDriveEncoder.get()));
                entries.add(RConf.string("Right Drive Encoder: " + DriveCode.rightDriveEncoder.get()));
                entries.add(RConf.string("Left Portcullis Encoder: " + Portcullis.leftEncoder.get()));
                entries.add(RConf.string("Right Portcullis Encoder: " + Portcullis.rightEncoder.get()));
                entries.add(RConf.string("Intake Arm Encoder: " + IntakeArm.encoder.get()));
                entries.add(RConf.string("Heading Yaw: " + HeadingSensor.yawAngle.get()));
                entries.add(RConf.string("Heading Pitch: " + HeadingSensor.pitchAngle.get()));
                entries.add(RConf.autoRefresh(1000));
                return entries.toArray(new Entry[entries.size()]);
            }

            @Override
            public boolean signalRConf(int field, byte[] data) throws InterruptedException {
                return false;
            }
        });

        Cluck.publishRConf("(COMMON)", new RConfable() {
            @Override
            public boolean signalRConf(int field, byte[] data) throws InterruptedException {
                return false;
            }

            @Override
            public Entry[] queryRConf() throws InterruptedException {
                ArrayList<Entry> ents = new ArrayList<>();
                ents.add(RConf.title("Common Options"));
                ents.add(RConf.cluckRef("(PIT) Self Test"));
                ents.add(RConf.cluckRef("Autonomous Mode Selector"));
                ents.add(RConf.cluckRef("Challenge Brake State"));
                ents.add(RConf.cluckRef("Diagnostics"));
                ents.add(RConf.cluckRef("Heading Connected"));
                ents.add(RConf.cluckRef("Heading Yaw Angle"));
                ents.add(RConf.cluckRef("Pit Mode Enable"));
                ents.add(RConf.cluckRef("Save Tuning for MainTuning"));
                ents.add(RConf.cluckRef("Save Tuning for AutonomousTuning"));
                ents.add(RConf.cluckRef("Shooter Flywheel Target High Speed"));
                ents.add(RConf.cluckRef("Shooter Flywheel Velocity"));
                return ents.toArray(new Entry[ents.size()]);
            }
        });

        for (int i = 11; i <= 18; i++) {
            runCamera("cam" + i, "10.15.40." + i);
        }

        Cluck.publish("(DEBUG) Dump heap", () -> {
            HotSpotDiagnostic hsd = new sun.management.HotSpotDiagnostic();
            try {
                hsd.dumpHeap("/tmp/heap-dump-" + System.currentTimeMillis(), false);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            Logger.info("DUMPED");
        });
    }

    private static final EventInput hundred = new Ticker(100);

    private static void runCamera(String name, String address) {
        Logger.info("RUN CAMERA: " + name);
        OutputStream cam = Cluck.publishOS(name);
        byte[] avail = (address + "\n").getBytes();
        byte[] unavail = ("autonomous\n").getBytes();
        BooleanOutput update = (b) -> {
            try {
                cam.write(b ? unavail : avail);
            } catch (IOException e) {
                Logger.warning("Cannot write", e);
            }
        };
        update.setWhen(FRC.inAutonomousMode(), hundred);
    }
}
