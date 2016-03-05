package org.team1540.zukoazula;

import ccre.channel.BooleanInput;
import ccre.channel.FloatOutput;
import ccre.frc.FRC;

public class ChallengeBrake {
    private static final BooleanInput firstTrigger = ZukoAzula.controlBinding.addBoolean("Challenge Brake A");
    private static final BooleanInput secondTrigger = ZukoAzula.controlBinding.addBoolean("Challenge Brake B");
    private static final BooleanInput composedTriggers = FRC.inTeleopMode().and(firstTrigger).and(secondTrigger);

    private static final FloatOutput servos = FRC.servo(0, -1, 1).combine(FRC.servo(1, -1, 1));

    public static void setup() {
        composedTriggers.toFloat(0, 1).send(servos);
    }
}
