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
                    170, // red target
                    20, // green target
                    10, // blue target
                    70, // red threshold
                    70, // green threshold
                    20, // blue threshold
                    50, // min goal pixel count
                    200.0f, // similarity threshold
                    3.2f, // goal aspect ratio
                    20.0f); // goal aspect ratio threshold
            setSize(img.getWidth(), img.getHeight()+200);
            g.drawImage(img, 0, 0, img.getWidth(), img.getHeight(), Color.white, null);
            g.setColor(Color.black);
            g.fillRect(4, 6, 40, 20);
            g.setColor(Color.white);
            char[] text = (goals.size()+"").toCharArray();
            g.drawChars(text, 0, text.length, 20, 20);
            Goal target = null;
            for (Goal goal : goals) {
                g.setColor(Color.cyan);
                g.drawLine(goal.ll.x, goal.ll.y, goal.lr.x, goal.lr.y);
                g.drawLine(goal.lr.x, goal.lr.y, goal.ur.x, goal.ur.y);
                g.drawLine(goal.ur.x, goal.ur.y, goal.ul.x, goal.ul.y);
                g.drawLine(goal.ul.x, goal.ul.y, goal.ll.x, goal.ll.y);
                if (target == null || target.shape.getCount() > goal.shape.getCount()) {
                    //if (target.shape.getCount() - goal.)
                            target = goal;
                }
            }
            
            if (target != null) {
                g.setColor(Color.cyan);
                g.drawLine(target.ll.x, target.ll.y, target.ur.x, target.ur.y);
                g.drawLine(target.lr.x, target.lr.y, target.ul.x, target.ul.y);
                
                float centerX = (target.ll.x + target.lr.x + target.ul.x + target.ur.x) / 4.0f;
                float centerY = (target.ll.y + target.lr.y + target.ul.y + target.ur.y) / 4.0f;
                
                g.setColor(Color.black);
                g.drawString(("Center: ("+(int)centerX + ",\t"+(int)centerY+");\t\tSize: (" + target.shape.getCount() + ")"), 4, img.getHeight()+14);
                g.drawString("LL: " + "("+target.ll.x+",\t"+target.ll.y+");\t\t"
                            + "LR: " + "("+target.lr.x+",\t"+target.lr.y+");\t\t"
                            + "UL: " + "("+target.ul.x+",\t"+target.ul.y+");\t\t"
                            + "UR: " + "("+target.ur.x+",\t"+target.ur.y+");\t\t", 4, img.getHeight()+28);
                g.drawString("LL.y-LR.y: " + (target.ll.y-target.lr.y) + ";\t\t(LL.y+LR.y)/2: "+((target.ll.y+target.lr.y)/2.0f), 4, img.getHeight()+42);
                float bottomAverageY = (target.ll.y + target.lr.y) / 2.0f;
                float bottomAverageX = (target.ll.x + target.lr.x) / 2.0f;
                //float bottomDistance = (float) Math.sqrt((bottomAverageX - 245.0f)*(bottomAverageX - 245.0f)*0.8f + (bottomAverageY - 0.0f)*(bottomAverageY - 0.0f));
                //float distance = 1.1f*(0.000103f*bottomDistance*bottomDistance - 0.012f*bottomDistance + 0.4211f);
                float llLength = (float) target.ll.distance(target.lr);
                float distance = 0.0018f*llLength*llLength - 0.4529f*llLength + 31.3f;
                g.drawString("Distance: " + distance, 4, img.getHeight()+56);
                float angle = (bottomAverageX-245.0f)/(distance+3.0f);
                g.drawString("Angle: " + angle, 4, img.getHeight()+70);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
