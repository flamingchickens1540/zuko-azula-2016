package org.team1540.kangaroo;

import org.team1540.zukoazula.Kangaroo;

import ccre.channel.BooleanCell;
import ccre.channel.BooleanInput;
import ccre.channel.FloatInput;
import ccre.cluck.Cluck;
import ccre.cluck.CluckPublisher;

public class KangarooGoalClient {
    public final FloatInput centerX;
    public final FloatInput centerY;
    public final BooleanInput hasTarget;
    public final BooleanCell enabled = new BooleanCell(false);
    public final FloatInput lastGyro;

    public KangarooGoalClient(String name) {
        centerX = CluckPublisher.subscribeFI(Kangaroo.node, "kangaroo/" + name + "CenterX", true);
        centerY = CluckPublisher.subscribeFI(Kangaroo.node, "kangaroo/" + name + "CenterY", true);
        hasTarget = CluckPublisher.subscribeBI(Kangaroo.node, "kangaroo/" + name + "HasTarget", true);
        lastGyro = CluckPublisher.subscribeFI(Kangaroo.node, "kangaroo/" + name + "LastGyro", true);
        CluckPublisher.publish(Kangaroo.node, name + "Enabled", enabled.asInput());

        Cluck.publish("Kangaroo " + name + "Enabled", enabled.asInput());
        Cluck.publish("Kangaroo " + name + "HasTarget", hasTarget);
        Cluck.publish("Kangaroo " + name + "LastGyro", lastGyro);
    }
}
