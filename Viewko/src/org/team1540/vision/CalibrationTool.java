package org.team1540.vision;

import java.io.IOException;

import javax.swing.JFrame;

import org.team1540.zukoazula.VisionConstantsSub;

import ccre.cluck.Cluck;
import ccre.cluck.tcp.CluckTCPClient;

public class CalibrationTool {
    public static void main(String[] args) throws IOException, InterruptedException {
        new CluckTCPClient("10.15.40.19:5800", Cluck.getNode(), "robot", "calibration-tool").start();
        Thread.sleep(1000); // TODO: remove this
        VisionConstantsSub.setup();
        JFrame frame = new JFrame();
        frame.setSize(1000, 800);
        frame.add(new VisionProcessingPanel());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }
}
