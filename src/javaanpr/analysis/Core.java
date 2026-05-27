package javaanpr.analysis;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class Core {
    
    private String url;
    
    // CHANGE: Return type is now RecognitionResult instead of BufferedImage
    public RecognitionResult runCore(String link) throws IOException {
        /* 1. Image Preparation */
        url = link;
        File imageFile = new File(url);
        BufferedImage originalImg = ImageIO.read(imageFile);
        
        // --- RESIZING LOGIC (Standardize to 640x480) ---
        int targetWidth = 640;
        int targetHeight = 480;
        BufferedImage srcImg = new BufferedImage(targetWidth, targetHeight, originalImg.getType());
        
        Graphics2D g = srcImg.createGraphics();
        g.drawImage(originalImg.getScaledInstance(targetWidth, targetHeight, Image.SCALE_SMOOTH), 0, 0, null);
        g.dispose();
        // ------------------------------------------------
        
        BufferedImage destImg = null;
        WritableRaster srcRaster = srcImg.getRaster();
        
        /* 2. Preprocessing - RGB2Grayscale */
        if(srcRaster.getNumBands() != 1) {
            ImageColorConversion icc2gs = new ImageColorConversion(srcImg);
            destImg = new BufferedImage(srcImg.getWidth(), srcImg.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
            destImg = deepCopy(icc2gs.RGB2GRAYSCALE());
        } else {
            destImg = deepCopy(srcImg);
        }

        /* 3. Noise Reduction - Gaussian Filter */
        GaussianFilter gf = new GaussianFilter();
        destImg = deepCopy(gf.GaussianBlur(destImg, 7, 1.5));

        /* 4. Image Enhancement - Histogram Equalization */
        ImageEnhancement ie = new ImageEnhancement(destImg);
        destImg = deepCopy(ie.HistogramEqualization());
        
        /* 5. Edge Detection - Canny Edge Detector */
        EdgeDetection ed = new EdgeDetection(destImg);
        destImg = new BufferedImage(srcImg.getWidth(), srcImg.getHeight(), BufferedImage.TYPE_BYTE_BINARY);
        destImg = deepCopy(ed.CannyOp(0.5));
        
        /* 6. Morphological Processing (Thinning) */
        Thinning thin = new Thinning(destImg);
        destImg = deepCopy(thin.Skeletonize());
        
        /* 7. Morphological process && CCL (Plate Detection) */
        SelectPlate sp = new SelectPlate();
        int [][] location = sp.selectPlate(destImg); // Returns [[x1,y1], [x2,y2]]
        
        /* 8. Draw Boxes & Calculate IoU */
        Graphics2D g2d = srcImg.createGraphics();
        
        // Convert detected location to Rectangle
        int x = location[0][0];
        int y = location[0][1];        int w = location[1][0] - location[0][0];
        int h = location[1][1] - location[0][1] + 1;
        Rectangle detectedBox = new Rectangle(x, y, w, h);

        // --- IOU CALCULATION ---
        double iou = 0.0;
        Rectangle groundTruthBox = getGroundTruth(imageFile.getName());

        if (groundTruthBox != null) {
            // Draw Ground Truth in BLUE
            g2d.setColor(Color.BLUE);
            g2d.setStroke(new BasicStroke(2));
            g2d.drawRect(groundTruthBox.x, groundTruthBox.y, groundTruthBox.width, groundTruthBox.height);
            
            // Calculate Score
            iou = IoUUtils.calculateIoU(detectedBox, groundTruthBox);
        }

        // Draw Detected Box in RED
        g2d.setColor(Color.RED);
        g2d.setStroke(new BasicStroke(3));
        g2d.drawRect(x, y, w, h);
        g2d.dispose();
        
        // Return both the image and the score
        return new RecognitionResult(srcImg, iou, "Done");
    }
    
    /**
     * DATABASE SIMULATION
     * You must manually enter the coordinates for your specific test images here.
     * Open your image in Paint/Photoshop to find the X, Y, Width, Height of the plate.
     */
    private Rectangle getGroundTruth(String filename) {
        // EXAMPLE 1: If filename is "test1.jpg"
        if (filename.equalsIgnoreCase("test1.jpg")) {
            return new Rectangle(200, 310, 110, 35); // Replace with REAL values (x, y, w, h)
        }
        
        // EXAMPLE 2: 
        if (filename.contains("car_001")) {
            return new Rectangle(150, 200, 120, 40);
        }
        
        // If unknown, return null (IoU will be 0)
        return null;
    }
    
    BufferedImage deepCopy(BufferedImage bi) {
         ColorModel cm = bi.getColorModel();
         boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
         WritableRaster raster = bi.copyData(null);
         return new BufferedImage(cm, raster, isAlphaPremultiplied, null);
    }
}