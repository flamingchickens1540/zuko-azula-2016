package org.team1540.zukoazula;

import org.team1540.drivers.kauailabs.FRCAHRS;

import ccre.channel.FloatInput;
import ccre.cluck.Cluck;

public class HeadingSensor {
    private static final FRCAHRS sensor = FRCAHRS.newSPI_MXP();

    public static final FloatInput yawAngle = sensor.getYawAngle();
    public static final FloatInput yawRate = sensor.getYawRate();

    public static void setup() {
        Cluck.publish("Yaw Angle", yawAngle);
        Cluck.publish("Yaw Rate", yawRate);
        Cluck.publish("Yaw Reset", sensor.eventZeroYaw());
        Cluck.publish("Heading Connected", sensor.getConnected());
    }
}
