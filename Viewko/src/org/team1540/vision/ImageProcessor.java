package org.team1540.vision;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class ImageProcessor {
    private int[] preallocatedImage;
    private boolean[] preallocatedProcessedImage;
    public final BufferedImage model;
    public final Graphics2D modelG;
    private boolean[] preallocatedAlreadyMarked;
    public final BufferedImage readout;

    public ImageProcessor(int imgWidth, int imgHeight, boolean haveReadout) {
        preallocatedImage = new int[imgWidth * imgHeight * 3];
        preallocatedProcessedImage = new boolean[imgWidth * imgHeight];
        preallocatedAlreadyMarked = new boolean[imgWidth * imgHeight];
        readout = haveReadout ? new BufferedImage(imgWidth * 2, imgHeight * 2, BufferedImage.TYPE_BYTE_GRAY) : null;
        model = new BufferedImage(imgWidth, imgHeight, BufferedImage.TYPE_BYTE_BINARY);
        modelG = model.createGraphics();
    }

    public ImageProcessor useOrRealloc(int imgWidth, int imgHeight) {
        if (imgWidth * imgHeight == preallocatedAlreadyMarked.length) {
            return this; // can be reused
        } else {
            return new ImageProcessor(imgWidth, imgHeight, readout != null);
        }
    }

    public List<Goal> findGoals(BufferedImage image, int redTarget, int greenTarget, int blueTarget, int redThreshold, int greenThreshold, int blueThreshold, int minGoalPixelCount, float similarityThreshold, float goalAspectRatio, float goalAspectRatioThreshold) {
        Raster raster = image.getRaster();
        int width = raster.getWidth();
        int height = raster.getHeight();
        int[] pixels = raster.getPixels(0, 0, width, height, preallocatedImage);
        boolean[] filtered = preallocatedProcessedImage;

        int[] pixleft = readout == null ? null : new int[readout.getWidth() * readout.getHeight() / 4];
        int[] pixright = readout == null ? null : new int[pixleft.length];
        int[] pixdown = readout == null ? null : new int[pixleft.length];

        for (int i = 0; i < width * height; ++i) {
            // if the given pixel falls within the color threshold
            if (Math.abs(pixels[i * 3 + 0] - redTarget) < redThreshold && Math.abs(pixels[i * 3 + 1] - greenTarget) < greenThreshold && Math.abs(pixels[i * 3 + 2] - blueTarget) < blueThreshold) {
                filtered[i] = true;
                if (pixdown != null) {
                    pixdown[i] = 255;
                }
            } else {
                // this is needed because image is not reallocated and
                // filled with the value 'false' each step
                filtered[i] = false;
            }
        }

        List<Goal> goals = new ArrayList<>();

        // partition the image into a list of separate shapes
        List<Shape> shapes = partitionShapes(filtered, width);

        shapes.removeIf(x -> x.getCount() < minGoalPixelCount);
        for (Shape shape : shapes) {
            List<Point> convexHull = fastConvexHull(shapeToPoints(shape));

            // find best fit (for a goal)
            // a shape must have at least one point
            Point topRight = convexHull.get(0);
            Point topLeft = convexHull.get(0);
            Point bottomRight = convexHull.get(0);
            Point bottomLeft = convexHull.get(0);

            for (Point p : convexHull) {
                if (p.x + p.y < topLeft.x + topLeft.y) {
                    topLeft = p;
                }

                if (p.x - p.y < bottomLeft.x - bottomLeft.y) {
                    bottomLeft = p;
                }

                if (p.x + p.y > bottomRight.x + bottomRight.y) {
                    bottomRight = p;
                }

                if (p.x - p.y > topRight.x - topRight.y) {
                    topRight = p;
                }
            }

            this.modelG.setColor(Color.BLACK);
            this.modelG.fillRect(0, 0, this.model.getWidth(), this.model.getHeight());
            // generate a model goal and compare it to what the camera sees
            generateModelGoal(width, height, topLeft, bottomLeft, topRight, bottomRight);

            int[] idata = new int[model.getWidth() * model.getHeight()];
            model.getRaster().getPixels(0, 0, model.getWidth(), model.getHeight(), idata);
            float similarity = compareImages(shape.getShape(), idata);
            //System.out.println("SIMILARITY: " + similarity);

            if (similarity > 1.0 - similarityThreshold && (Math.abs((Math.abs(topLeft.distance(topRight) / topLeft.distance(bottomLeft)) + Math.abs(bottomLeft.distance(bottomRight) / topRight.distance(bottomRight)) / 2.0f) - goalAspectRatio) < goalAspectRatioThreshold)) {
                if (readout != null) {
                    boolean[] bs = shape.getShape();
                    for (int i = 0; i < idata.length; i++) {
                        if (bs[i]) {
                            pixright[i] = 255;
                        }
                        if (idata[i] != 0) {
                            pixleft[i] = 255;
                        }
                    }
                }
                goals.add(new Goal(topLeft, topRight, bottomRight, bottomLeft, shape));
            }
        }

        if (readout != null) {
            readout.getRaster().setPixels(0, 0, readout.getWidth() / 2, readout.getHeight() / 2, pixleft);
            readout.getRaster().setPixels(readout.getWidth() / 2, 0, readout.getWidth() / 2, readout.getHeight() / 2, pixright);
            readout.getRaster().setPixels(0, readout.getHeight() / 2, readout.getWidth() / 2, readout.getHeight() / 2, pixdown);
        }

        return goals;
    }

    private List<Point> shapeToPoints(Shape shape) {
        List<Point> points = new ArrayList<>(shape.getCount());
        for (int i = 0; i < shape.getWidth() * shape.getHeight(); ++i) {
            if (shape.get(i)) {
                points.add(new Point(i % shape.getWidth(), i / shape.getWidth()));
            }
        }
        return points;
    }

    private float compareImages(boolean[] check, int[] expected) {
        // returns 1.0 for perfect match, 0.0 for no match at all, and negative
        // numbers for terrible matches
        if (check.length != expected.length) {
            return 0.0f;
        }

        int wrong = 0, total = 0;
        for (int i = 0; i < check.length; ++i) {
            if (expected[i] != 0) {
                total++;
            }
            if (check[i] != (expected[i] != 0)) {
                wrong++;
            }
        }
        return 1 - (wrong / (float) total);
    }

    private void generateModelGoal(int width, int height, Point tl, Point bl, Point tr, Point br) {
        int x0 = (int) ((tl.x * 9 + tr.x) / 10.0f);
        int y0 = (int) ((tl.y * 9 + tr.y) / 10.0f);

        int x1 = (int) ((tl.x * 1 + tr.x * 9) / 10.0f);
        int y1 = (int) ((tl.y * 1 + tr.y * 9) / 10.0f);

        int x2 = (int) (x1 + (br.x - tr.x) * (5.8f / 7.f));
        int y2 = (int) (y1 + (br.y - tr.y) * (5.8f / 7.f));

        int x3 = (int) (x0 + (bl.x - tl.x) * (5.8f / 7.f));
        int y3 = (int) (y0 + (bl.y - tl.y) * (5.8f / 7.f));

        this.modelG.setColor(Color.WHITE);
        this.modelG.fillPolygon(new int[] { tl.x, bl.x, br.x, tr.x, x1, x2, x3, x0 }, new int[] { tl.y, bl.y, br.y, tr.y, y1, y2, y3, y0 }, 8);
    }

    public List<Shape> partitionShapes(boolean[] image, int width) {
        boolean[] alreadyMarked = preallocatedAlreadyMarked;
        Arrays.fill(alreadyMarked, false);
        List<Shape> objects = new ArrayList<>();

        for (int i = 0; i < image.length; ++i) {
            if (!alreadyMarked[i]) {
                Shape shape;
                try {
                    shape = floodfill(image, width, i % width, i / width);
                } catch (OutOfMemoryError err) {
                    System.out.println("failed for shape total: " + objects.size() + " plus one");
                    throw err;
                }
                if (shape != null) {
                    objects.add(shape);
                    for (int j = 0; j < alreadyMarked.length; ++j)
                        alreadyMarked[j] |= shape.get(j);
                }
            }
        }

        return objects;
    }

    public Shape floodfill(boolean[] image, int width, int x, int y) {
        if (!image[x + y * width])
            return null;

        Queue<Integer> toCheck = new LinkedList<>();
        Shape shape = new Shape(width, image.length / width);

        toCheck.add(x + y * width);
        while (!toCheck.isEmpty()) {
            int i = toCheck.remove();
            if (i >= 0 && i < image.length && image[i] && !shape.get(i)) {
                shape.mark(i);
                toCheck.add(i + 1);
                toCheck.add(i - 1);
                toCheck.add(i + width);
                toCheck.add(i - width);
            }
        }

        return shape;
    }

    public List<Point> fastConvexHull(List<Point> points) {
        ArrayList<Point> xSorted = new ArrayList<>(points);
        Collections.sort(xSorted, (o1, o2) -> (o1.x - o2.x));

        int n = xSorted.size();

        Point[] lUpper = new Point[n];

        lUpper[0] = xSorted.get(0);
        lUpper[1] = xSorted.get(1);

        int lUpperSize = 2;

        for (int i = 2; i < n; i++) {
            lUpper[lUpperSize] = xSorted.get(i);
            lUpperSize++;

            while (lUpperSize > 2 && !rightTurn(lUpper[lUpperSize - 3], lUpper[lUpperSize - 2], lUpper[lUpperSize - 1])) {
                // remove the middle point of the three last
                lUpper[lUpperSize - 2] = lUpper[lUpperSize - 1];
                lUpperSize--;
            }
        }

        Point[] lLower = new Point[n];

        lLower[0] = xSorted.get(n - 1);
        lLower[1] = xSorted.get(n - 2);

        int lLowerSize = 2;

        for (int i = n - 3; i >= 0; i--) {
            lLower[lLowerSize] = xSorted.get(i);
            lLowerSize++;

            while (lLowerSize > 2 && !rightTurn(lLower[lLowerSize - 3], lLower[lLowerSize - 2], lLower[lLowerSize - 1])) {
                // remove the middle point of the three last
                lLower[lLowerSize - 2] = lLower[lLowerSize - 1];
                lLowerSize--;
            }
        }

        ArrayList<Point> result = new ArrayList<Point>();

        for (int i = 0; i < lUpperSize; i++) {
            result.add(lUpper[i]);
        }

        for (int i = 1; i < lLowerSize - 1; i++) {
            result.add(lLower[i]);
        }

        return result;
    }

    // used for fastConvexHull
    private boolean rightTurn(Point a, Point b, Point c) {
        return (b.x - a.x) * (c.y - a.y) - (b.y - a.y) * (c.x - a.x) > 0;
    }
}
