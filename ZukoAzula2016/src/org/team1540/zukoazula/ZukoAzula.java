package org.team1540.zukoazula;

import java.util.ArrayList;

import ccre.cluck.Cluck;
import ccre.ctrl.ExtendedMotorFailureException;
import ccre.ctrl.binding.ControlBindingCreator;
import ccre.frc.FRC;
import ccre.frc.FRCApplication;
import ccre.log.Logger;
import ccre.rconf.RConf.Entry;
import ccre.rconf.RConf;
import ccre.rconf.RConfable;
import ccre.tuning.TuningContext;

public class ZukoAzula implements FRCApplication {

    public static final ControlBindingCreator controlBinding = FRC.controlBinding();
    public static final TuningContext mainTuning = new TuningContext("MainTuning").publishSavingEvent();

    @Override
    public void setupRobot() throws ExtendedMotorFailureException {
        Logger.info("🐣 CHEEP CHEEP 🐣");
        //Instrumentation.setup();

        DriveCode.setup();
        Autonomous.setup();
        Shooter.setup();
        Portcullis.setup();
        IntakeArm.setup();
        ChallengeBrake.setup();
        HeadingSensor.setup();

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
    }
}
