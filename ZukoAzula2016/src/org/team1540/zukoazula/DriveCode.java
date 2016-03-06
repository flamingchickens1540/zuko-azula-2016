package org.team1540.zukoazula;

import ccre.behaviors.ArbitratedFloat;
import ccre.behaviors.Behavior;
import ccre.behaviors.BehaviorArbitrator;
import ccre.channel.BooleanCell;
import ccre.channel.FloatCell;
import ccre.channel.FloatIO;
import ccre.channel.FloatInput;
import ccre.channel.FloatOutput;
import ccre.channel.DerivedFloatInput;
import ccre.cluck.Cluck;
import ccre.ctrl.ExtendedMotor;
import ccre.ctrl.ExtendedMotorFailureException;
import ccre.drivers.ctre.talon.TalonExtendedMotor;
import ccre.frc.FRC;
import ccre.time.Time;

public class DriveCode {

    private static final FloatInput driveLeftAxis = ZukoAzula.controlBinding.addFloat("Drive Left Axis").deadzone(0.2f).negated();
    private static final FloatInput driveRightAxis = ZukoAzula.controlBinding.addFloat("Drive Right Axis").deadzone(0.2f).negated();
    private static final FloatInput driveRightTrigger = ZukoAzula.controlBinding.addFloat("Drive Forwards Trigger").deadzone(0.2f);
    private static final FloatInput driveLeftTrigger = ZukoAzula.controlBinding.addFloat("Drive Backwards Trigger").deadzone(0.2f);

    private static final TalonExtendedMotor[] rightCANs = new TalonExtendedMotor[] { FRC.talonCAN(4), FRC.talonCAN(5), FRC.talonCAN(6) };
    private static final TalonExtendedMotor[] leftCANs = new TalonExtendedMotor[] { FRC.talonCAN(1), FRC.talonCAN(2), FRC.talonCAN(3) };

    public static final FloatInput ticksPerFoot = ZukoAzula.mainTuning.getFloat("Ticks Per Foot", 100);

    private static final FloatInput leftDriveEncoder = leftCANs[0].modEncoder().getEncoderPosition().negated();
    private static final FloatInput rightDriveEncoder = rightCANs[0].modEncoder().getEncoderPosition().negated();
    private static final FloatInput driveEncodersAverage = leftDriveEncoder; // right encoder doesn't work at the moment
    private static final FloatInput ticksPerSecond = velocityOf(driveEncodersAverage, ZukoAzula.mainTuning.getFloat("Drive Velocity Update Threshold", .25f));
    private static final FloatInput feetPerSecond = ticksPerSecond.dividedBy(ticksPerFoot);

    private static final BehaviorArbitrator behaviors = new BehaviorArbitrator("Behaviors");
    private static final Behavior autonomous = behaviors.addBehavior("Autonomous", FRC.inAutonomousMode());
    private static final Behavior teleop = behaviors.addBehavior("Teleop", FRC.inTeleopMode());
    private static final BooleanCell pitModeEnable = new BooleanCell();
    private static final Behavior pit = behaviors.addBehavior("Pit Mode", pitModeEnable.andNot(FRC.isOnFMS()));
    private static final Behavior challenge = behaviors.addBehavior("Challenge Brake", ChallengeBrake.driveBrake);

    private static final ArbitratedFloat leftInput = behaviors.addFloat();
    private static final ArbitratedFloat rightInput = behaviors.addFloat();

    private static final FloatCell autonomousLeft = new FloatCell();
    private static final FloatCell autonomousRight = new FloatCell();

    public static void setup() throws ExtendedMotorFailureException {
        leftInput.attach(autonomous, autonomousLeft);
        rightInput.attach(autonomous, autonomousRight);
        leftInput.attach(teleop, driveLeftAxis.plus(driveRightTrigger.minus(driveLeftTrigger)));
        rightInput.attach(teleop, driveRightAxis.plus(driveRightTrigger.minus(driveLeftTrigger)));
        leftInput.attach(pit, FloatInput.zero);
        rightInput.attach(pit, FloatInput.zero);
        leftInput.attach(challenge, FloatInput.zero);
        rightInput.attach(challenge, FloatInput.zero);

        FloatOutput leftMotors = PowerManager.managePower(2, FloatOutput.combine(simpleAll(leftCANs, FRC.MOTOR_FORWARD)));
        FloatOutput rightMotors = PowerManager.managePower(2, FloatOutput.combine(simpleAll(rightCANs, FRC.MOTOR_REVERSE)));

        leftInput.send(leftMotors.addRamping(0.1f, FRC.constantPeriodic));
        rightInput.send(rightMotors.addRamping(0.1f, FRC.constantPeriodic));

        // Instrumentation.recordDrive(leftInput, rightInput, leftDriveEncoder, rightDriveEncoder);

        FloatCell fastestDriveSpeed = new FloatCell();
        feetPerSecond.send((newValue) -> {
            if (Math.abs(newValue) > fastestDriveSpeed.get()) {
                fastestDriveSpeed.set(Math.abs(newValue));
            }
        });
        fastestDriveSpeed.setWhen(0, FRC.startTele);

        Cluck.publish("Drive Left Raw", driveLeftAxis);
        Cluck.publish("Drive Right Raw", driveRightAxis);
        Cluck.publish("Drive Forwards Raw", driveRightTrigger);
        Cluck.publish("Drive Backwards Raw", driveLeftTrigger);
        Cluck.publish("Drive Left Motors", leftInput);
        Cluck.publish("Drive Right Motors", rightInput);
        Cluck.publish("Pit Mode Enable", pitModeEnable);
        Cluck.publish("Drive Feet Per Second", feetPerSecond);
        Cluck.publish("Drive Encoders Average", driveEncodersAverage);
        Cluck.publish("Drive Fastest Speed", fastestDriveSpeed.asInput());
        Cluck.publish("Drive Reset Fastest Speed", fastestDriveSpeed.eventSet(0));
    }

    private static FloatOutput[] simpleAll(ExtendedMotor[] cans, boolean reverse) throws ExtendedMotorFailureException {
        FloatOutput[] outs = new FloatOutput[cans.length];
        for (int i = 0; i < cans.length; i++) {
            outs[i] = cans[i].simpleControl(reverse);
        }
        return outs;
    }

    public static FloatOutput getLeftOutput() {
        return autonomousLeft;
    }

    public static FloatOutput getRightOutput() {
        return autonomousRight;
    }

    public static FloatInput getEncoder() {
        return driveEncodersAverage;
    }

    // Better accuracy than getEncoderVelocity()
    private static FloatInput velocityOf(FloatInput input, FloatInput updateThreshold) {
        return new DerivedFloatInput(input) {
            long oldtime = Time.currentTimeNanos();
            float oldvalue = input.get();

            @Override
            protected float apply() {
                long newtime = Time.currentTimeNanos();
                float newvalue = input.get();
                // To improve accuracy, if too little time has passed before the
                // most recent update, it will not update again.
                if (newtime - oldtime < updateThreshold.get() * Time.NANOSECONDS_PER_SECOND) {
                    return this.get();
                }
                float result = (newvalue - oldvalue) / (newtime - oldtime) * Time.NANOSECONDS_PER_SECOND;
                oldtime = newtime;
                oldvalue = newvalue;
                return result;
            }
        };
    }
}
