package org.team1540.vision;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;

import javax.swing.JPanel;

import org.team1540.zukoazula.VisionConstantsSub;

public class VisionProcessingPanel extends JPanel {
    private final WebcamThread webcam;
    private ImageProcessor processor;
    private BufferedImage current_image = null;
    private int mx, my;
    private String err = "loading";
    private int r, g, b;

    public VisionProcessingPanel() throws IOException {
        webcam = new WebcamThread((image) -> {
            current_image = image;
            if (image != null) {
                setSize(image.getWidth(), image.getHeight() + 200);
            }
            invalidate();
            repaint();
        }, (error) -> {
            err = error;
        });
        webcam.setAddress("10.15.40.13");
        processor = new ImageProcessor(100, 100);
        addMouseMotionListener(new MouseMotionListener() {
            @Override
            public void mouseMoved(MouseEvent e) {
                mx = e.getX();
                my = e.getY();
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                this.mouseMoved(e);
            }
        });
    }

    BufferedImage defaultp = new BufferedImage(640, 640, BufferedImage.TYPE_INT_RGB);

    @Override
    public void paint(Graphics g) {
        BufferedImage img;
        // TODO: this is the absolutely wrong way to do it
        img = current_image;
        if (img == null) {
            img = defaultp;
        }
        if (img == null) {
            g.setColor(Color.BLACK);
            g.drawString("NO IMAGE YET", 100, 100);
            return; // nothing yet
        } else {
            processor = processor.useOrRealloc(img.getWidth(), img.getHeight());
            String debug = ((int) VisionConstantsSub.targetRed.get()) + ", " + ((int) VisionConstantsSub.targetGreen.get()) + ", " + ((int) VisionConstantsSub.targetBlue.get()) + ", " + ((int) VisionConstantsSub.thresholdRed.get()) + ", " + ((int) VisionConstantsSub.thresholdGreen.get()) + ", " + ((int) VisionConstantsSub.thresholdBlue.get()) + ", " + ((int) VisionConstantsSub.minGoalPixCount.get()) + ", " + ((int) VisionConstantsSub.similarityThreshold.get()) + ", " + ((int) VisionConstantsSub.aspectRatio.get()) + ", " + ((int) VisionConstantsSub.aspectRatioThreshold.get());
            List<Goal> goals = processor.findGoals(img, (int) VisionConstantsSub.targetRed.get(), (int) VisionConstantsSub.targetGreen.get(), (int) VisionConstantsSub.targetBlue.get(), (int) VisionConstantsSub.thresholdRed.get(), (int) VisionConstantsSub.thresholdGreen.get(), (int) VisionConstantsSub.thresholdBlue.get(), (int) VisionConstantsSub.minGoalPixCount.get(), (int) VisionConstantsSub.similarityThreshold.get(), (int) VisionConstantsSub.aspectRatio.get(), (int) VisionConstantsSub.aspectRatioThreshold.get());
            g.drawImage(img, 0, 0, img.getWidth(), img.getHeight(), Color.white, null);
            g.setColor(Color.black);
            g.fillRect(4, 6, 40, 20);
            g.setColor(Color.white);
            g.drawString(String.valueOf(goals.size()), 20, 20);
            g.fillRect(0, img.getHeight(), img.getWidth(), 200);
            Goal target = null;
            for (Goal goal : goals) {
                g.setColor(Color.cyan);
                g.drawLine(goal.ll.x, goal.ll.y, goal.lr.x, goal.lr.y);
                g.drawLine(goal.lr.x, goal.lr.y, goal.ur.x, goal.ur.y);
                g.drawLine(goal.ur.x, goal.ur.y, goal.ul.x, goal.ul.y);
                g.drawLine(goal.ul.x, goal.ul.y, goal.ll.x, goal.ll.y);
                if (target == null || target.shape.getCount() < goal.shape.getCount()) {
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
                float ppd = 1480.0f * (float) Math.PI / 180.0f;
                float yaw = (bottomX - img.getWidth()) / ppd;
                float pitch = (bottomY - img.getHeight()) / ppd;

                g.drawString("bottomX = " + bottomX, 4, img.getHeight() + 14);
                g.drawString("bottomY = " + bottomY, 4, img.getHeight() + 28);
                g.drawString("ppd = " + ppd, 4, img.getHeight() + 28 + 14 * 1);
                g.drawString("yaw = " + yaw, 4, img.getHeight() + 28 + 14 * 2);
                g.drawString("pitch = " + pitch, 4, img.getHeight() + 28 + 14 * 3);
                g.drawString("count = " + target.shape.getCount(), 4, 14 * 6);
            }
            g.setColor(Color.black);
            if (mx >= 0 && mx < img.getWidth() && my >= 0 && my < img.getHeight()) {
                int i = img.getRGB(mx, my);
                int nr = (i >> 16) & 0xFF;
                int ng = (i >> 8) & 0xFF;
                int nb = i & 0xFF;
                g.drawString("rgb = " + nr + " " + ng + " " + nb, 4, img.getHeight() + 28 + 14 * 6);
                int lr = r, lg = this.g, lb = b;
                r = (int) (0.5 * r + 0.5 * nr);
                this.g = (int) (0.5 * this.g + 0.5 * ng);
                b = (int) (0.5 * b + 0.5 * nb);
                if (r == lr) {
                    r = nr;
                }
                if (this.g == lg) {
                    this.g = ng;
                }
                if (b == lb) {
                    b = nb;
                }
                g.drawString("avg = " + r + " " + this.g + " " + b, 4, img.getHeight() + 28 + 14 * 7);
            }
            g.drawString("debug = " + debug, 4, img.getHeight() + 28 + 14 * 8);
        }
        g.setColor(err == null ? Color.GREEN : Color.RED);
        g.drawString(err == null ? "good" : err, 300, 20);
    }
}
