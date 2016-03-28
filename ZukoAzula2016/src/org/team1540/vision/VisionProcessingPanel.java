package org.team1540.vision;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;

import javax.swing.JPanel;

public class VisionProcessingPanel extends JPanel {
    private WebcamReader webcam;
    private ImageProcessor processor;
    
    public VisionProcessingPanel() throws IOException {
        webcam = new WebcamReader("10.15.40.12", 500);
        BufferedImage b = webcam.readNext();
        processor = new ImageProcessor(b.getWidth(), b.getHeight());
    }
    
    @Override
    public void paint(Graphics g) {
        BufferedImage img;
        try {
            img = webcam.readNext();
            List<Goal> goals = processor.findGoals(img, 
                    245, // red target
                    50, // green target
                    10, // blue target
                    70, // red threshold
                    70, // green threshold
                    20, // blue threshold
                    50, // min goal pixel count
                    0.6f, // similarity threshold
                    3.2f, // goal aspect ratio
                    1.0f); // goal aspect ratio threshold
            setSize(img.getWidth(), img.getHeight()+64);
            g.drawImage(img, 0, 0, img.getWidth(), img.getHeight(), Color.white, null);
            g.setColor(Color.black);
            g.fillRect(4, 6, 40, 20);
            g.setColor(Color.white);
            char[] text = (goals.size()+"").toCharArray();
            g.drawChars(text, 0, text.length, 20, 20);
            Goal target = null;
            for (Goal goal : goals) {
                g.setColor(Color.CYAN);
                g.drawLine(goal.ll.x, goal.ll.y, goal.lr.x, goal.lr.y);
                g.drawLine(goal.lr.x, goal.lr.y, goal.ur.x, goal.ur.y);
                g.drawLine(goal.ur.x, goal.ur.y, goal.ul.x, goal.ul.y);
                g.drawLine(goal.ul.x, goal.ul.y, goal.ll.x, goal.ll.y);
                if (target == null || target.shape.getCount() > goal.shape.getCount()) {
                    target = goal;
                }
            }
            
            if (target != null) {
                float centerX = (target.ll.x + target.lr.x + target.ul.x + target.ur.x) / 4.0f;
                float centerY = (target.ll.y + target.lr.y + target.ul.y + target.ur.y) / 4.0f;
                
                g.setColor(Color.black);
                g.drawString(("Center: ("+(int)centerX + ",\t"+(int)centerY+");\t\tSize: (" + target.shape.getCount() + ")"), 10, img.getHeight()+14);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
