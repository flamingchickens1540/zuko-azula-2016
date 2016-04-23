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
    
    public KangarooGoalClient(String name) {
        centerX = Cluck.subscribeFI("kangaroo/"+name+"CenterX", true);
        centerY = Cluck.subscribeFI("kangaroo/"+name+"CenterY", true);
        hasTarget = Cluck.subscribeBI("kangaroo/"+name+"HasTarget", true);
        CluckPublisher.publish(Kangaroo.node, name+"Enabled", enabled.asInput());
    }
    
    public void enable() {
        enabled.set(true);
    }
    
    public void disable() {
        enabled.set(false);
    }
}
