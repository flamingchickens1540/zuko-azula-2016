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

    // needs to be this for Zuko; the reverse for Azula.
    private static final BooleanCell servoState = new BooleanCell();
    public static final BooleanInput driveBrake = servoState;

    public static void setup() {
        servoState.setTrueWhen(composedTriggers.onPress());
        servoState.setFalseWhen(FRC.startDisabled);
        servoState.toFloat(ZukoAzula.mainTuning.getFloat("Challenge Retracted Left", 1.0f), ZukoAzula.mainTuning.getFloat("Challenge Extended Left", 0.373f)).send(FRC.servo(0, 0, 1));
        servoState.toFloat(ZukoAzula.mainTuning.getFloat("Challenge Retracted Right", 0.0f), ZukoAzula.mainTuning.getFloat("Challenge Extended Right", 0.186f)).send(FRC.servo(1, 0, 1));
        Cluck.publish("Challenge Brake State", servoState);
    }
}
