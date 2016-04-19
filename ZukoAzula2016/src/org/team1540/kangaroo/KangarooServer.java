package org.team1540.kangaroo;

import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Objects;

import org.team1540.vision.Goal;
import org.team1540.vision.ImageProcessor;
import org.team1540.vision.WebcamThread;

import ccre.channel.BooleanCell;
import ccre.channel.BooleanInput;
import ccre.channel.FloatCell;
import ccre.channel.FloatInput;
import ccre.cluck.CluckNode;
import ccre.cluck.CluckPublisher;
import ccre.cluck.tcp.CluckTCPClient;
import ccre.cluck.tcp.CluckTCPServer;
import ccre.log.Logger;

public class KangarooServer {
    private static CluckNode node = new CluckNode();
    private static volatile BufferedImage swap;
    private static final Object swapLock = new Object();
    private static String lastError;
    private static ImageProcessor processor = new ImageProcessor(320, 240);
    private static final WebcamThread webcam = new WebcamThread((image) -> {
        synchronized (swapLock) {
            swap = image;
            swapLock.notifyAll();
        }
    } , (error) -> {
        if (!Objects.equals(error, lastError)) {
            lastError = error;
            Logger.finer("Error state of webcam: " + error);
        }
    });

    public static void main(String[] args) {
        CluckTCPClient client = new CluckTCPClient("roboRIO-1540-frc.local", 4003, node, "robot", "kangaroo");
        CluckTCPServer server = new CluckTCPServer(node, 4002);
        server.start();
        client.start();
        webcam.setAddress("10.15.40.12");

        BooleanInput autonomousRunning = CluckPublisher.subscribeBI(node, "robot/autonomousRunning", true);
        FloatInput gyroCurrent = CluckPublisher.subscribeFI(node, "robot/gyroCurrent", true);
        FloatCell gyroTarget = new FloatCell();
        BooleanCell hasTarget = new BooleanCell();
        CluckPublisher.publish(node, "gyroTarget", gyroTarget.asInput());
        CluckPublisher.publish(node, "hasTarget", hasTarget.asInput());
        
        FloatInput targetRed = CluckPublisher.subscribeFI(node, "robot/Vision Target Red", true);
        FloatInput targetBlue = CluckPublisher.subscribeFI(node, "robot/Vision Target Blue", true);
        FloatInput targetGreen = CluckPublisher.subscribeFI(node, "robot/Vision Target Green", true);
        FloatInput thresholdRed = CluckPublisher.subscribeFI(node, "robot/Vision Threshold Red", true);
        FloatInput thresholdGreen = CluckPublisher.subscribeFI(node, "robot/Vision Threshold Green", true);
        FloatInput thresholdBlue = CluckPublisher.subscribeFI(node, "robot/Vision Threshold Blue", true);
        FloatInput minGoalPixCount = CluckPublisher.subscribeFI(node, "robot/Vision Min Goal Pixel Count", true);
        FloatInput similarityThreshold = CluckPublisher.subscribeFI(node, "robot/Vision Similarity Threshold", true);
        FloatInput aspectRatio = CluckPublisher.subscribeFI(node, "robot/Vision Goal Aspect Ratio", true);
        FloatInput aspectRatioThreshold = CluckPublisher.subscribeFI(node, "robot/Vision Goal Aspect Ratio Threshold", true);

        FloatInput pixelToGryo = CluckPublisher.subscribeFIO(node, "robot/pixelToGyro", true);
        BufferedImage image = null;

        for (;;) {
            while (!autonomousRunning.get()) {
                try {
                    Logger.finer("Autonomous is not running, retrying in 500ms.");
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                }
            }

            try {
                for (;;) {
                    Thread.sleep(1);
                    synchronized (swapLock) {
                        swap = null;
                        while (swap == null) {
                            swapLock.wait();
                        }
                        image = swap;
                    }

                    processor = processor.useOrRealloc(image.getWidth(), image.getHeight());
                    float gyro = gyroCurrent.get();
                    List<Goal> goals = processor.findGoals(image, (int) targetRed.get(), (int) targetGreen.get(), (int) targetBlue.get(), (int) thresholdRed.get(), (int) thresholdGreen.get(), (int) thresholdBlue.get(), (int) minGoalPixCount.get(), similarityThreshold.get(), aspectRatio.get(), aspectRatioThreshold.get());
                    hasTarget.set(!goals.isEmpty());
                    Goal target = null;
                    for (Goal goal : goals) {
                        if (target == null || target.shape.getCount() < goal.shape.getCount()) {
                            target = goal;
                        }
                    }
                    if (target != null) {
                        final float PIXEL_TO_GYRO_TICK = pixelToGryo.get();
                        float center = target.shape.getWidth() / 2.0f;
                        float goalCenter = (target.ll.x + target.lr.x + target.ur.x + target.ul.x) / 4.0f;
                        gyroTarget.safeSet(gyro + (goalCenter - center) * PIXEL_TO_GYRO_TICK);
                    }
                }
            } catch (InterruptedException e) {

            } finally {
                webcam.setAddress(null);
            }
        }
    }
}
