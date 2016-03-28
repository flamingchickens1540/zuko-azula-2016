package org.team1540.vision;

import java.io.IOException;

import javax.swing.JFrame;

public class CalibrationTool {
    public static void main(String[] args) throws IOException {
        JFrame frame = new JFrame();
        frame.setSize(800, 600);
        VisionProcessingPanel panel = new VisionProcessingPanel();
        frame.add(panel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
        while (frame.isVisible()) {
            panel.invalidate();
            frame.repaint();
        }
    }
}
