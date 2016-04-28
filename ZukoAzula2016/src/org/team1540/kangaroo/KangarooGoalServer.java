package org.team1540.kangaroo;

import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Objects;

import org.team1540.vision.Goal;
import org.team1540.vision.ImageProcessor;
import org.team1540.vision.WebcamThread;

import ccre.channel.BooleanInput;
import ccre.channel.BooleanOutput;
import ccre.channel.FloatInput;
import ccre.channel.FloatOutput;
import ccre.cluck.Cluck;
import ccre.cluck.tcp.CluckTCPServer;
import ccre.log.Logger;

public class KangarooGoalServer {
    private volatile BufferedImage image;
    private volatile BufferedImage swap;
    private final Object swapLock = new Object();
    private final Object processingLock = new Object();
    private ImageProcessor processor = new ImageProcessor(320, 240, false);
    private String lastError;
    private final String webcamAddress;
    private Thread processingThread;
    private BooleanInput enabled;
    private FloatOutput cameraCenterX;
    private FloatOutput cameraCenterY;
    private FloatOutput lastGyro;
    private BooleanOutput hasTarget;
    private boolean stopped = false;

    private WebcamThread webcam = new WebcamThread((image) -> {
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

    public KangarooGoalServer(String ip, BooleanInput enabled, FloatOutput cameraCenterX, FloatOutput cameraCenterY, BooleanOutput hasTarget, FloatOutput lastGyro) {
        webcamAddress = ip;
        enabled.onPress().send(() -> {
            synchronized (processingLock) {
                processingLock.notify();
            }
        });

        this.enabled = enabled;
        this.cameraCenterX = cameraCenterX;
        this.cameraCenterY = cameraCenterY;
        this.hasTarget = hasTarget;
        this.lastGyro = lastGyro;
    }

    public void start() {
        stop();
        webcam.setAddress(webcamAddress);
        stopped = false;
        processingThread = new Thread(this::processingThreadMethod);
        processingThread.start();
    }

    public void stop() {
        if (webcam != null)
            webcam.setAddress(null);
        if (processingThread != null)
            processingThread.interrupt();
        stopped = true;
    }

    private void processingThreadMethod() {
        while (!stopped) {
            synchronized (processingLock) {
                while (!enabled.get()) {
                    try {
                        hasTarget.set(false);
                        processingLock.wait();
                    } catch (InterruptedException e) {
                    }
                }
            }

            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
            }
            synchronized (swapLock) {
                swap = null;
                while (swap == null) {
                    try {
                        swapLock.wait();
                    } catch (InterruptedException e) {
                    }
                }
                image = swap;
            }

            processor = processor.useOrRealloc(image.getWidth(), image.getHeight());
            List<Goal> goals = processor.findGoals(image, (int) KangarooRobotStatus.targetRed.get(), (int) KangarooRobotStatus.targetGreen.get(), (int) KangarooRobotStatus.targetBlue.get(), (int) KangarooRobotStatus.thresholdRed.get(), (int) KangarooRobotStatus.thresholdGreen.get(), (int) KangarooRobotStatus.thresholdBlue.get(), (int) KangarooRobotStatus.minGoalPixCount.get(), KangarooRobotStatus.similarityThreshold.get(), KangarooRobotStatus.aspectRatio.get(), KangarooRobotStatus.aspectRatioThreshold.get());
            hasTarget.set(!goals.isEmpty());
            Goal target = null;
            for (Goal goal : goals) {
                if (target == null || target.shape.getCount() < goal.shape.getCount()) {
                    target = goal;
                }
            }
            if (target != null) {
                float goalCenterX = (target.ll.x + target.lr.x + target.ur.x + target.ul.x) / (2.0f * target.shape.getWidth());
                float goalCenterY = (target.ll.y + target.lr.y + target.ur.y + target.ul.y) / (2.0f * target.shape.getHeight());
                cameraCenterX.set(goalCenterX-1.0f);
                cameraCenterY.set(goalCenterY-1.0f);
                lastGyro.set(KangarooServer.currentGyro.get());
            }
        }

        stop();
    }
}
