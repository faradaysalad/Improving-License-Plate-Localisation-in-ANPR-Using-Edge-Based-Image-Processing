package javaanpr.analysis;

import java.awt.Rectangle;

public class IoUUtils {

    // Calculate Intersection over Union (IoU)
    // Formula: (Overlap Area) / (Area of Box A + Area of Box B - Overlap Area)
    public static double calculateIoU(Rectangle detectedBox, Rectangle groundTruthBox) {
        
        // 1. Calculate the INTERSECTION (Overlap area)
        int x1 = Math.max(detectedBox.x, groundTruthBox.x);
        int y1 = Math.max(detectedBox.y, groundTruthBox.y);
        int x2 = Math.min(detectedBox.x + detectedBox.width, groundTruthBox.x + groundTruthBox.width);
        int y2 = Math.min(detectedBox.y + detectedBox.height, groundTruthBox.y + groundTruthBox.height);
        
        // If x2 < x1 or y2 < y1, there is no overlap
        if (x2 < x1 || y2 < y1) {
            return 0.0;
        }
        
        int intersectionArea = (x2 - x1) * (y2 - y1);
        
        // 2. Calculate the UNION
        int detectedArea = detectedBox.width * detectedBox.height;
        int truthArea = groundTruthBox.width * groundTruthBox.height;
        
        int unionArea = detectedArea + truthArea - intersectionArea;
        
        // 3. Return IoU
        return (double) intersectionArea / (double) unionArea;
    }
}