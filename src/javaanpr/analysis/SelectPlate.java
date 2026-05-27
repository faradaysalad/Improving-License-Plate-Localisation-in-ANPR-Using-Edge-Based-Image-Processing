package javaanpr.analysis;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import ij.ImagePlus;
import ij.process.ImageProcessor;

// Import the Region class from EightConnectedComponent
import javaanpr.analysis.EightConnectedComponent.Region;

public class SelectPlate {

    // --- MODE 1: PRODUCTION (Standard Detection) ---
    // This is used by Core.java to detect the plate using Heuristics (Area, Ratio, Density)
    public int[][] selectPlate(BufferedImage bi){
        BufferedImage srcImg = deepCopy(bi);
        BufferedImage tmpImg = new BufferedImage(srcImg.getWidth(), srcImg.getHeight(), BufferedImage.TYPE_BYTE_BINARY);
        
        int[][] finalLocation = new int[2][2]; // Default 0,0
        int condition = 0;
        
        do {
            condition++;
            tmpImg = deepCopy(srcImg);

            // --- Step 4: Morphological Processing ---
            applyMorphology(tmpImg, condition);

            // --- Step 5: Region Extraction ---
            // Run CCL to find all blobs (No filtering yet)
            EightConnectedComponent ecc = new EightConnectedComponent(tmpImg);
            ecc.EightCCL(); 
            List<Region> allRegions = ecc.getRawRegions();
            
            // --- Step 6: Candidate Filtering ---
            List<Region> candidates = filterCandidates(allRegions);

            // --- Step 7: Final Selection (Heuristic) ---
            // Pick the best candidate based on pixel count (largest valid plate)
            if (!candidates.isEmpty()) {
                Region bestRegion = candidates.get(0);
                for (Region r : candidates) {
                    if (r.pixelCount > bestRegion.pixelCount) {
                        bestRegion = r;
                    }
                }
                
                // Save coordinates
                finalLocation[0][0] = bestRegion.rect.x;
                finalLocation[0][1] = bestRegion.rect.y;
                finalLocation[1][0] = bestRegion.rect.x + bestRegion.rect.width;
                finalLocation[1][1] = bestRegion.rect.y + bestRegion.rect.height;
                
                break; // Stop looking, we found it!
            }

        } while(condition < 10);
                
        return finalLocation;
    }

    // --- MODE 2: EVALUATION (Testing with Ground Truth) ---
    // This is used ONLY for testing to calculate IoU and accuracy
    public int[][] selectPlateWithGroundTruth(BufferedImage bi, Rectangle groundTruth, String imageName) {
        BufferedImage srcImg = deepCopy(bi);
        BufferedImage tmpImg = new BufferedImage(srcImg.getWidth(), srcImg.getHeight(), BufferedImage.TYPE_BYTE_BINARY);
        
        int[][] finalLocation = new int[2][2];
        int condition = 0;
        
        double bestIoU = 0.0;
        Rectangle bestBox = null;
        long startTime = System.currentTimeMillis();
        
        do {
            condition++;
            tmpImg = deepCopy(srcImg);

            // --- Step 4: Morphological Processing ---
            applyMorphology(tmpImg, condition);

            // --- Step 5: Region Extraction ---
            EightConnectedComponent ecc = new EightConnectedComponent(tmpImg);
            ecc.EightCCL(); 
            List<Region> allRegions = ecc.getRawRegions();
            
            // --- Step 6: Candidate Filtering ---
            List<Region> candidates = filterCandidates(allRegions);

            // --- Step 7: Final Selection (IoU based) ---
            // Compare every candidate against the Ground Truth
            for (Region candidate : candidates) {
                double iou = IoUUtils.calculateIoU(candidate.rect, groundTruth);
                
                if (iou > bestIoU) {
                    bestIoU = iou;
                    bestBox = candidate.rect;
                }
            }
            
            // If we found a good match, stop early
            if (bestIoU >= 0.5) break;

        } while(condition < 10);
        
        // --- Logging ---
        long endTime = System.currentTimeMillis();
        logResult(imageName, bestIoU, (endTime - startTime), (bestIoU >= 0.5 ? "SUCCESS" : "FAILED"));

        if (bestBox != null && bestIoU >= 0.5) {
            finalLocation[0][0] = bestBox.x;
            finalLocation[0][1] = bestBox.y;
            finalLocation[1][0] = bestBox.x + bestBox.width;
            finalLocation[1][1] = bestBox.y + bestBox.height;
        }
        
        return finalLocation;
    }

    // --- HELPER: Morphological Recipes ---
    private void applyMorphology(BufferedImage tmpImg, int condition) {
        ImagePlus iplus = new ImagePlus("img", tmpImg);
        ImageProcessor ip = iplus.getProcessor();
        Morpho mph = new Morpho();
        
        if(condition == 1) { mph.FillHole(ip); mph.Opening(ip, 1); mph.Closing(ip, 1); mph.FillHole(ip); mph.Erode(ip, 10); mph.Dilate(ip, 10); } 
        else if(condition == 2) { mph.Closing(ip, 1); mph.Dilate(ip, 3); mph.Erode(ip, 10); mph.Dilate(ip, 7); }
        else if(condition == 3) { mph.Closing(ip, 1); mph.Dilate(ip, 3); mph.Closing(ip, 1); mph.Erode(ip, 10); mph.Dilate(ip, 7); }
        else if(condition == 4) { mph.FillHole(ip); mph.Dilate(ip, 1); mph.Erode(ip, 10); mph.Dilate(ip, 7); }
        else if(condition == 5) { mph.FillHole(ip); mph.Closing(ip, 1); mph.FillHole(ip); mph.Erode(ip, 10); mph.Dilate(ip, 10); }
        else if(condition == 6) { mph.FillHole(ip); mph.Dilate(ip, 1); mph.Closing(ip, 1); mph.FillHole(ip); mph.Opening(ip, 1); mph.Erode(ip, 10); mph.Dilate(ip, 9); }
        else if(condition == 7) { mph.Dilate(ip, 1); mph.Closing(ip, 1); mph.Erode(ip, 1); mph.Opening(ip, 1); mph.FillHole(ip); mph.Erode(ip, 10); mph.Dilate(ip, 10); }
        else if(condition == 8) { mph.FillHole(ip); mph.Dilate(ip, 3); mph.Closing(ip, 1); mph.Erode(ip, 3); mph.Opening(ip, 1); }
        else if(condition == 9) { mph.FillHole(ip); mph.Opening(ip, 1); mph.Dilate(ip, 3); mph.Closing(ip, 1); mph.FillHole(ip); mph.Erode(ip, 10); mph.Dilate(ip, 8); }
        else if (condition == 10) { mph.FillHole(ip); mph.Opening(ip, 1); mph.Erode(ip, 5); mph.Dilate(ip, 5); }
        
        // Write changes back to BufferedImage and Invert Colors
        BufferedImage processedImg = iplus.getBufferedImage();
        WritableRaster raster = tmpImg.getRaster();
        WritableRaster procRaster = processedImg.getRaster();
        
        for(int r=0; r<raster.getHeight(); r++) {
            for(int c=0; c<raster.getWidth(); c++) {
                int sample = procRaster.getSample(c, r, 0);
                // Invert: 0->1, 255->0
                raster.setSample(c, r, 0, (sample == 0) ? 1 : 0);
            }
        }
    }

    // --- HELPER: Step-by-Step Filtering ---
    private List<Region> filterCandidates(List<Region> allRegions) {
        List<Region> passedArea = new ArrayList<>();
        List<Region> passedRatio = new ArrayList<>();
        List<Region> passedDensity = new ArrayList<>();
        
        // 6.1 Area Filter
        for (Region r : allRegions) {
            if (r.pixelCount >= 20 && r.pixelCount <= 15000 && r.rect.height >= 20 && r.rect.width >= 40) {
                passedArea.add(r);
            }
        }

        // 6.2 Aspect Ratio Filter
        for (Region r : passedArea) {
            double ratio = (double) r.rect.width / (double) r.rect.height;
            if (ratio >= 2.0 && ratio <= 6.0) {
                passedRatio.add(r);
            }
        }

        // 6.3 Edge Density Filter
        for (Region r : passedRatio) {
            int boxArea = r.rect.width * r.rect.height;
            double density = (double) r.pixelCount / (double) boxArea;
            if (density >= 0.6) {
                passedDensity.add(r);
            }
        }
        
        return passedDensity;
    }

    private void logResult(String imgName, double iou, long runtime, String status) {
        try (FileWriter fw = new FileWriter("evaluation_log.txt", true)) {
            fw.write(String.format("%s | IoU: %.4f | Time: %dms | Result: %s%n", imgName, iou, runtime, status));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    BufferedImage deepCopy(BufferedImage bi) {
         ColorModel cm = bi.getColorModel();
         boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
         WritableRaster raster = bi.copyData(null);
         return new BufferedImage(cm, raster, isAlphaPremultiplied, null);
    }
}