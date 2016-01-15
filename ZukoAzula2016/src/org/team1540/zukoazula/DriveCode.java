package org.team1540.zukoazula;

import ccre.channel.FloatInput;
import ccre.channel.FloatOutput;
import ccre.cluck.Cluck;
import ccre.ctrl.Drive;
import ccre.ctrl.ExtendedMotor;
import ccre.ctrl.ExtendedMotorFailureException;
import ccre.frc.FRC;

public class DriveCode {

    private static final FloatInput pilotLeftAxis = FRC.joystick1.axis(2).deadzone(0.1f), pilotRightAxis = FRC.joystick1.axis(6).deadzone(0.1f);
    private static final FloatInput pilotRightTrigger = FRC.joystick1.axis(4).deadzone(0.1f), pilotLeftTrigger = FRC.joystick1.axis(3).deadzone(0.1f);
    
    private static final ExtendedMotor leftFrontCAN = FRC.talonCAN(0), leftBackCAN = FRC.talonCAN(1), rightFrontCAN = FRC.talonCAN(2), rightBackCAN = FRC.talonCAN(3);
    
    public static void initDriveCode() throws ExtendedMotorFailureException {

        final FloatOutput leftMotors = leftFrontCAN.simpleControl().combine(leftBackCAN.simpleControl());
        final FloatOutput rightMotors = rightFrontCAN.simpleControl(true).combine(rightBackCAN.simpleControl(true));

        FloatOutput leftOut = leftMotors.addRamping(0.1f, FRC.constantPeriodic);
        FloatOutput rightOut = rightMotors.addRamping(0.1f, FRC.constantPeriodic);

        Drive.extendedTank(pilotLeftAxis, pilotRightAxis, pilotLeftTrigger.minus(pilotRightTrigger), leftOut, rightOut);
        
        Cluck.publish("Left Output", leftOut);
        Cluck.publish("Right Output", rightOut);
    }
}
