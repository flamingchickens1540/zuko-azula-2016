package org.team1540.zukoazula;

import org.team1540.drivers.kauailabs.FRCAHRS;

import ccre.channel.FloatInput;
import ccre.cluck.Cluck;

public class HeadingSensor {
    private static final FRCAHRS sensor = FRCAHRS.newSPI_MXP();

    public static final FloatInput yawAngle = sensor.getYawAngle();
    public static final FloatInput yawRate = sensor.getYawRate();
    public static final FloatInput pitchAngle = sensor.getRollAngle(); // pitch is actually roll

    public static void setup() {
        Cluck.publish("Heading Yaw Angle", yawAngle);
        Cluck.publish("Heading Yaw Rate", yawRate);
        Cluck.publish("Heading Yaw Reset", sensor.eventZeroYaw());
        Cluck.publish("Heading Pitch Angle", pitchAngle);
        Cluck.publish("Heading Connected", sensor.getConnected());
        //Instrumentation.recordHeading(sensor.getConnected(), yawAngle, yawRate, pitchAngle);
    }
}
