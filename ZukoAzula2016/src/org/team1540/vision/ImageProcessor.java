package org.team1540.vision;

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
    private boolean[] preallocatedAlreadyMarked;
    
    public ImageProcessor(int imgWidth, int imgHeight) {
        preallocatedImage = new int[imgWidth*imgHeight*3];
        preallocatedProcessedImage = new boolean[imgWidth*imgHeight];
        preallocatedAlreadyMarked = new boolean[imgWidth*imgHeight];
    }
    
    public ImageProcessor useOrRealloc(int imgWidth, int imgHeight) {
        if (imgWidth * imgHeight == preallocatedAlreadyMarked.length) {
            return this; // can be reused
        } else {
            return new ImageProcessor(imgWidth, imgHeight);
        }
    }

    public List<Goal> findGoals(BufferedImage image, 
            int redTarget,
            int greenTarget,
            int blueTarget,
            int redThreshold, 
            int greenThreshold, 
            int blueThreshold,
            int minGoalPixelCount,
            float similarityThreshold,
            float goalAspectRatio,
            float goalAspectRatioThreshold) {
        Raster raster = image.getRaster();
        int width = raster.getWidth();
        int height = raster.getHeight();
        int[] pixels = raster.getPixels(0, 0, width, height, preallocatedImage);
        boolean[] filtered = preallocatedProcessedImage;
        
        for (int i=0; i<width*height; ++i) {
            // if the given pixel falls within the color threshold
            if (Math.abs(pixels[i*3+0] - redTarget) < redThreshold &&
                    Math.abs(pixels[i*3+1] - greenTarget) < greenThreshold &&
                    Math.abs(pixels[i*3+2] - blueTarget) < blueThreshold) {
                filtered[i] = true;
            } else {
                // this is needed because image is not reallocated and
                // filled with the value 'false' each step
                filtered[i] = false;
            }
            
//            System.out.println(Math.abs(pixels[i*3+0]) + " " + Math.abs(pixels[i*3+1]) + " " + Math.abs(pixels[i*3+0]));
        }
        
        List<Goal> goals = new ArrayList<>();
        
        // partition the image into a list of separate shapes
        List<Shape> shapes = partitionShapes(filtered, width);
        shapes.removeIf(x -> x.getCount() < minGoalPixelCount);
        for (Shape shape : shapes) {
            List<Point> convexHull = fastConvexHull(shapeToPoints(shape));
            
            // find best fit (for a goal)
            Point topRight = convexHull.get(0); // a shape must have at least one point
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
            
            // generate a model goal and compare it to what the camera sees
            boolean[] model = generateModelGoal(width, height, topLeft, bottomLeft, topRight, bottomRight);
            float similarity = compareImages(shape.getShape(), model);
            if (similarity > 1.0 - similarityThreshold 
                    && (Math.abs((Math.abs(topLeft.distance(topRight)/topLeft.distance(bottomLeft)) +
                            Math.abs(bottomLeft.distance(bottomRight)/topRight.distance(bottomRight))/2.0f) - goalAspectRatio)
                            < goalAspectRatioThreshold)) {
                goals.add(new Goal(topLeft, topRight, bottomRight, bottomLeft, shape));
            }
        }
        
        return goals;
    }
    
    private List<Point> shapeToPoints(Shape shape) {
        List<Point> points = new ArrayList<>(shape.getCount());
        for (int i=0; i<shape.getWidth()*shape.getHeight(); ++i) {
            if (shape.get(i)) {
                points.add(new Point(i%shape.getWidth(), i/shape.getWidth()));
            }
        }
        return points;
    }
    
    private float compareImages(boolean[] a, boolean[] b) {
        // returns 1.0 for perfect match and 0.0 for no match at all
        if (a.length != b.length)
            return 0.0f;

        int total = 0, match = 0;
        for (int i = 0; i < a.length; ++i) {
            total++;
            if (!(a[i] ^ b[i]))
                match++;
        }

        return (float) match / (float) total;
    }
    
    private boolean[] generateModelGoal(int width, int height, Point tl, Point bl, Point tr, Point br) {
        boolean[] model = new boolean[width * height];

        int x0 = (int) ((tl.x * 9 + tr.x) / 10.0f);
        int y0 = (int) ((tl.y * 9 + tr.y) / 10.0f);

        int x1 = (int) ((tl.x * 1 + tr.x * 9) / 10.0f);
        int y1 = (int) ((tl.y * 1 + tr.y * 9) / 10.0f);

        int x2 = (int) (x1 + (br.x - tr.x) * (5.8f / 7.f));
        int y2 = (int) (y1 + (br.y - tr.y) * (5.8f / 7.f));

        int x3 = (int) (x0 + (bl.x - tl.x) * (5.8f / 7.f));
        int y3 = (int) (y0 + (bl.y - tl.y) * (5.8f / 7.f));

        writeLine(model, width, tl.x, tl.y, x0, y0);
        writeLine(model, width, x1, y1, tr.x, tr.y);
        writeLine(model, width, tr.x, tr.y, br.x, br.y);
        writeLine(model, width, br.x, br.y, bl.x, bl.y);
        writeLine(model, width, bl.x, bl.y, tl.x, tl.y);
        // writeLine(model, width, x0, y0, x1, y1);
        writeLine(model, width, x1, y1, x2, y2);
        writeLine(model, width, x2, y2, x3, y3);
        writeLine(model, width, x3, y3, x0, y0);
        writeFill(model, width, tl.x + 1, tl.y + 1);

        return model;
    }

    private void writeLine(boolean[] dst, int width, int x0, int y0, int x1, int y1) {
        int dx = Math.abs(x1 - x0);
        int dy = Math.abs(y1 - y0);

        int sx = x0 < x1 ? 1 : -1;
        int sy = y0 < y1 ? 1 : -1;

        int err = dx - dy;
        int e2;

        while (true) {
            dst[y0 * width + x0] = true;

            if (x0 == x1 && y0 == y1)
                break;

            e2 = 2 * err;
            if (e2 > -dy) {
                err = err - dy;
                x0 = x0 + sx;
            }

            if (e2 < dx) {
                err = err + dx;
                y0 = y0 + sy;
            }
        }
    }

    private void writeFill(boolean[] dst, int width, int x, int y) {
        if (x + y * width >= dst.length)
            return;
        if (dst[x + y * width])
            return;
        Queue<Integer> toCheck = new LinkedList<>();
        toCheck.add(x + y * width);
        while (!toCheck.isEmpty()) {
            int i = toCheck.remove();
            if (i >= 0 && i < dst.length && !dst[i]) {
                dst[i] = true;
                toCheck.add(i + 1);
                toCheck.add(i - 1);
                toCheck.add(i + width);
                toCheck.add(i - width);
            }
        }
    }

    public List<Shape> partitionShapes(boolean[] image, int width) {
        boolean[] alreadyMarked = preallocatedAlreadyMarked;
        Arrays.fill(alreadyMarked, false);
        List<Shape> objects = new ArrayList<>();
        
        for (int i=0; i<image.length; ++i) {
            if (!alreadyMarked[i]) {
                Shape shape = floodfill(image, width, i%width, i/width);
                if (shape != null) {
                    objects.add(shape);
                    for (int j=0; j<alreadyMarked.length; ++j) alreadyMarked[j] |= shape.get(j);
                }
            }
        }
        
        return objects;
    }
    
    public Shape floodfill(boolean[] image, int width, int x, int y) {
        if (!image[x+y*width]) return null;
        
        Queue<Integer> toCheck = new LinkedList<>();
        Shape shape = new Shape(width, image.length/width, null);
        
        toCheck.add(x+y*width);
        while (!toCheck.isEmpty()) {
            int i = toCheck.remove();
            if (i >= 0 && i < image.length && image[i] && !shape.get(i)) {
                shape.mark(i);
                toCheck.add(i+1);
                toCheck.add(i-1);
                toCheck.add(i+width);
                toCheck.add(i-width);
            }
        }
        
        return shape;
    }
    
    public List<Point> fastConvexHull(List<Point> points) {        
        ArrayList<Point> xSorted = new ArrayList<>(points);
        Collections.sort(xSorted, 
                (o1, o2) -> (o1.x - o2.x));

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
