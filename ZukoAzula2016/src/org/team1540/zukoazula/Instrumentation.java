package org.team1540.zukoazula;

import ccre.channel.BooleanInput;
import ccre.channel.FloatInput;
import ccre.frc.FRC;
import ccre.recording.Recorder;

public class Instrumentation {
    public static Recorder rec;

    public static void setup() {
        rec = FRC.getRecorder();
    }
    
    public static void recordDrive(FloatInput left, FloatInput right, FloatInput leftEncoder, FloatInput rightEncoder) {
        if (rec != null) {
            rec.recordFloatInput(left, "Left Drive");
            rec.recordFloatInput(right, "Right Drive");
            rec.recordFloatInput(leftEncoder, "Left Drive Encoder");
            rec.recordFloatInput(rightEncoder, "Right Drive Encoder");
        }
    }

    public static void recordHeading(BooleanInput connected, FloatInput yawangle, FloatInput yawrate, FloatInput pitchangle) {
        if (rec != null) {
            rec.recordBooleanInput(connected, "NavX Connected");
            rec.recordFloatInput(yawangle, "NavX Yaw");
            rec.recordFloatInput(yawrate, "NavX Yaw Rate");
            rec.recordFloatInput(pitchangle, "NavX Pitch");
        }
    }
}
