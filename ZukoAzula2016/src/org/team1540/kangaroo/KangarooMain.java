package org.team1540.kangaroo;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.nio.file.Files;

import ccre.channel.FloatCell;
import ccre.channel.FloatInput;
import ccre.cluck.Cluck;
import ccre.deployment.Artifact;
import ccre.deployment.DepRoboRIO;
import ccre.deployment.Shell;
import ccre.deployment.DepRoboRIO.RIOShell;
import ccre.log.Logger;

public class KangarooMain {
    private static float x = 0;
    public static FloatCell wave = new FloatCell(0);

    public static void main(String[] args) {
        Cluck.setupServer();
        Cluck.setupClient("roboRIO-1540.local", "kangaroo", "kangaroo");
        Cluck.publish("Wave", (FloatInput) wave);
    }

    public static void loop() {
        while (true) {
            wave.set((float) Math.sin(x));
            x += 0.1;
        }
    }
}
