package org.team1540.zukoazula;

import ccre.channel.BooleanCell;
import ccre.channel.BooleanInput;
import ccre.channel.BooleanOutput;
import ccre.channel.FloatOutput;
import ccre.cluck.Cluck;
import ccre.frc.FRC;

public class ChallengeBrake {
    private static final BooleanInput firstTrigger = ZukoAzula.controlBinding.addBoolean("Challenge Brake A");
    private static final BooleanInput secondTrigger = ZukoAzula.controlBinding.addBoolean("Challenge Brake B");
    private static final BooleanInput composedTriggers = FRC.inTeleopMode().and(firstTrigger).and(secondTrigger);

    private static final FloatOutput servos = FRC.servo(0, 0, 1).combine(FRC.servo(1, 1, 0));
    private static final BooleanCell servoState = new BooleanCell();
    public static final BooleanInput driveBrake = servoState;

    public static void setup() {
        servoState.setTrueWhen(composedTriggers.onPress());
        servoState.setFalseWhen(FRC.startDisabled);
        servoState.toFloat(0, 1).send(servos);
        Cluck.publish("Challenge Brake State", servoState);
    }
}
