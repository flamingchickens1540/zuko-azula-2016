package org.team1540.vision;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;

import ccre.channel.FloatCell;

public class CalibrationTool {
    private static final int SIDEBAR_WIDTH = 200;

    private static Color currentColor = null;
    private static TextPanel redIn;
    private static TextPanel blueIn;
    private static TextPanel greenIn;
    private static VisionProcessingPanel panel;

    public static final FloatCell redTarget = new FloatCell(245);
    public static final FloatCell greenTarget = new FloatCell(50);
    public static final FloatCell blueTarget = new FloatCell(10);
    public static final FloatCell redThreshold = new FloatCell(70);
    public static final FloatCell greenThreshold = new FloatCell(70);
    public static final FloatCell blueThreshold = new FloatCell(20);

    public static void main(String[] args) throws IOException {
        JFrame frame = new JFrame();
        JPanel mainPanel = new JPanel(new BorderLayout());
        panel = new VisionProcessingPanel();
        mainPanel.add(panel, BorderLayout.CENTER);

        JPanel infoPanel = new JPanel();
        JPanel info = new JPanel();
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
        TextPanel red = new TextPanel("Red:", info);
        TextPanel blue = new TextPanel("Blue:", info);
        TextPanel green = new TextPanel("Green:", info);
        new LabelledSlider("Red Target", 0, 255, redTarget, info);
        new LabelledSlider("Red Margin of Error", 0, 100, redThreshold, info);
        new LabelledSlider("Blue Target", 0, 255, blueTarget, info);
        new LabelledSlider("Blue Margin of Error", 0, 100, blueThreshold, info);
        new LabelledSlider("Green Target", 0, 255, greenTarget, info);
        new LabelledSlider("Green Margin of Error", 0, 100, greenThreshold, info);
        new TextPanel("In Threshold:", info);
        redIn = new TextPanel("Red:", info);
        blueIn = new TextPanel("Blue:", info);
        greenIn = new TextPanel("Green:", info);
        info.setPreferredSize(new Dimension(SIDEBAR_WIDTH, 600));
        infoPanel.add(info);
        mainPanel.add(infoPanel, BorderLayout.EAST);

        frame.add(mainPanel);

        panel.addMouseListener(new MouseAdapter() {

            @Override
            public void mousePressed(MouseEvent e) {
                if (e.getY() < panel.img.getHeight()) {
                    currentColor = new Color(panel.img.getRGB(e.getX(), e.getY()));
                    red.setText("Red: " + currentColor.getRed());
                    blue.setText("Blue: " + currentColor.getBlue());
                    green.setText("Green: " + currentColor.getGreen());
                    updateResults();
                }
            }

        });

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(panel.img.getWidth() + SIDEBAR_WIDTH + 10, panel.img.getHeight() + 64);
        frame.setResizable(true);
        frame.setVisible(true);
        while (frame.isVisible()) {
            panel.invalidate();
            frame.repaint();
        }
    }

    static class TextPanel extends JPanel {
        private JLabel text;

        public TextPanel(String s, JPanel addTo) {
            text = new JLabel(s);
            add(text);
            setMaximumSize(new Dimension(SIDEBAR_WIDTH, 30));
            addTo.add(this);
        }

        public void setText(String s) {
            text.setText(s);
        }
    }

    static class LabelledSlider extends JPanel {

        public LabelledSlider(String name, int low, int high, FloatCell value, JPanel addTo) {
            super();
            JLabel nameLabel = new JLabel(name + ": " + (int) value.get());
            JSlider slider = new JSlider(low, high, (int) value.get());
            slider.addChangeListener((e) -> {
                value.set(slider.getValue());
                nameLabel.setText(name + ": " + (int) value.get());
                updateResults();
            });
            add(nameLabel);
            add(slider);
            setMaximumSize(new Dimension(SIDEBAR_WIDTH, 50));
            addTo.add(this);
        }
    }

    private static void updateResults() {
        if (currentColor != null) {
            redIn.setText("Red: " + (Math.abs(currentColor.getRed() - redTarget.get()) <= redThreshold.get()));
            blueIn.setText("Blue: " + (Math.abs(currentColor.getBlue() - blueTarget.get()) <= blueThreshold.get()));
            greenIn.setText("Green: " + (Math.abs(currentColor.getGreen() - greenTarget.get()) <= greenThreshold.get()));
        }
    }
}
