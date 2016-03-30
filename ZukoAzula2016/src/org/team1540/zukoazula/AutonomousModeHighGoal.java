package org.team1540.zukoazula;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;

import org.team1540.vision.CalibrationTool;
import org.team1540.vision.Goal;
import org.team1540.vision.ImageProcessor;
import org.team1540.vision.WebcamReader;

import ccre.frc.FRC;
import ccre.instinct.AutonomousModeOverException;
import ccre.log.Logger;

public class AutonomousModeHighGoal extends AutonomousBase {

    private WebcamReader webcam;
    private ImageProcessor processor;
    private BufferedImage swap;
    
    public AutonomousModeHighGoal() {
        super("Drive and Shoot");
    }

    @Override
    protected void runAutonomous() throws InterruptedException, AutonomousModeOverException {
        swap = null;
        Thread thread = new Thread(() -> {
            while (FRC.inAutonomousMode().get()) {
                try {
                    webcam = new WebcamReader("10.15.40.12", 500);
                    swap = webcam.readNext();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        thread.start();
        
        boolean alreadyLocked = false;
        while (swap == null) /* do nothing */;
        BufferedImage currentImage = swap;
        processor = new ImageProcessor(currentImage.getWidth(), currentImage.getHeight());
        while (FRC.inAutonomousMode().get()) {
            long currentTime = System.nanoTime();
            currentImage = swap;
            List<Goal> goals = processor.findGoals(currentImage, 
                    (int) CalibrationTool.redTarget.get(), // red target
                    (int) CalibrationTool.greenTarget.get(), // green target
                    (int) CalibrationTool.blueTarget.get(), // blue target
                    (int) CalibrationTool.redThreshold.get(), // red threshold
                    (int) CalibrationTool.greenThreshold.get(), // green threshold
                    (int) CalibrationTool.blueThreshold.get(), // blue threshold
                    50, // min goal pixel count
                    0.6f, // similarity threshold
                    3.2f, // goal aspect ratio
                    1.0f); // goal aspect ratio threshold
            
            Goal target = null;
            for (Goal g : goals) {
                if (target == null || g.shape.getCount() > target.shape.getCount()) {
                    target = g;
                }
            }
            
            if (target != null) {
                float centerX = (target.ll.x + target.lr.x + target.ul.x + target.ur.x) / 4.0f;
                float centerY = (target.ll.y + target.lr.y + target.ul.y + target.ur.y) / 4.0f;
                
                if (Math.abs(centerX - 225.0) > 20.0f) {
                    float direction = Math.signum(centerX - 225.0f);
                    turnForTime(0.07f, direction*0.5f);
                    waitForTime(100);
                } else {
                    if (target.shape.getCount() < 845) {
                        alreadyLocked = true;
                        driveForTime(0.1f, 0.4f);
                    } else {
                        break;
                    }
                }
                
                System.out.println("Number of goals: " + goals.size() + "; Center X: " + centerX);
            } else {
                if (alreadyLocked) {
                    //turnForTime(0.1f, -0.3f);
                    driveForTime(0.1f, 0.4f);
                    fire(2.0f);
                    break; 
                } else {
                    turnForTime(0.2f, 0.5f);
                    waitForTime(100);
                }
                System.out.println("Number of goals: " + goals.size());                
            }
        }
    }
}
