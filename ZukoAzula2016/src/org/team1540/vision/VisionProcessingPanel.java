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
        webcam = new WebcamReader("10.15.40.13", 500);
        BufferedImage b = webcam.readNext();
        processor = new ImageProcessor(b.getWidth(), b.getHeight());
    }
    
    @Override
    public void paint(Graphics g) {
        BufferedImage img;
        try {
            img = webcam.readNext();
            List<Goal> goals = processor.findGoals(img, 
                    205, // red target
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
                
                g.setColor(Color.black);
                float bottomX = (target.ll.x + target.lr.x) / 2.0f;
                float bottomY = (target.ll.y + target.lr.y) / 2.0f;
                float ppd = 1480.0f*(float)Math.PI/180.0f;
                float yaw = (bottomX-img.getWidth()) / ppd;
                float pitch = (bottomY-img.getHeight()) / ppd;
                
                g.drawString("bottomX = "+ bottomX, 4, img.getHeight()+14);
                g.drawString("bottomY = "+ bottomY, 4, img.getHeight()+28);
                g.drawString("ppd = "+ ppd, 4, img.getHeight()+28+14*1);
                g.drawString("yaw = "+ yaw, 4, img.getHeight()+28+14*2);
                g.drawString("pitch = "+ pitch, 4, img.getHeight()+28+14*3);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
