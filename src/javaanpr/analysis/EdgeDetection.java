package javaanpr.analysis;

import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;

public class EdgeDetection {
    
    // The "Sobel Masks" - These are the math templates used to find edges.
    // KERNEL_H finds Horizontal edges (lines going across)
    private static final int[][] KERNEL_H = { {-1, -2, -1}, {0, 0, 0}, {1, 2, 1} };
    // KERNEL_V finds Vertical edges (lines going up/down)
    private static final int[][] KERNEL_V = { {-1, 0, 1}, {-2, 0, 2}, {-1, 0, 1} };
        
    private final int W, H;
    private BufferedImage srcImg, destImg;
    private WritableRaster srcRaster, destRaster;
    
    // Arrays to store intermediate data
    private int [][] Gy;    // Gradient Y (Horizontal changes)
    private int [][] Gx;    // Gradient X (Vertical changes)
    private double [][] mag;    // Magnitude (Total Edge Strength)
    private int stdDev, mean;   // Stats for auto-thresholding
    private int [][] dir;       // Direction (Angle of the edge: 0, 45, 90, 135)
    
    // Thresholds for Hysteresis
    private double tHigh, tLow, tRatio;
    
    public EdgeDetection(BufferedImage bi) {
        this.srcImg = bi;
        this.W = bi.getWidth();
        this.H = bi.getHeight();
        srcRaster = srcImg.getRaster();
    }
    
    // --- MAIN PIPELINE: Canny Edge Detection ---
    public BufferedImage CannyOp(double thresRatio) {
        
        tRatio = thresRatio; // Ratio between High and Low threshold (usually 0.5)
        
        SobelFilter();  // Step 1: Calculate Gradient Strength & Direction
        Suppression();  // Step 2: Thin the edges (remove non-peak pixels)
        Hysteresis();   // Step 3: Finalize edges using Double Thresholding
        
        return destImg;
    }
    
    
    // --- STEP 1: SOBEL FILTER ---
    // Calculates how fast pixel brightness changes (Gradient)
    public void SobelFilter() {
        
        destImg = new BufferedImage(W, H, BufferedImage.TYPE_BYTE_GRAY);
        destRaster = destImg.getRaster();
        
        Gx = new int [H][W];
        Gy = new int [H][W];
        
        Horizontal(); // Calculate Y-gradient
        Vertical();   // Calculate X-gradient
        Magnitude();  // Combine X and Y to get total strength
        Direction();  // Calculate angle of the edge
    }

    // Applies KERNEL_H to find horizontal edges
    public void Horizontal() {
        if(H > 2 && W > 2) { 
            for(int row=1; row<H-1; row++) {
                for(int col=1; col<W-1; col++) {
                    int sum = 0;
                    // Convolve 3x3 area
                    for (int krow = -1; krow < 2; krow++) {
                        for (int kcol = -1; kcol < 2; kcol++) {
                            sum += (KERNEL_H[krow + 1][kcol + 1] * srcRaster.getSample(col + kcol, row + krow, 0));
                        }
                    }
                    Gy[row][col] = sum;
                }
            }
        }
    }
    
    // Applies KERNEL_V to find vertical edges
    public void Vertical() {
        if(H > 2 && W > 2) { 
            for(int row=1; row<H-1; row++) {
                for(int col=1; col<W-1; col++) {
                    int sum = 0;
                    for (int krow = -1; krow < 2; krow++) {
                        for (int kcol = -1; kcol < 2; kcol++) {
                            sum += (KERNEL_V[krow + 1][kcol + 1] * srcRaster.getSample(col + kcol, row + krow, 0));
                        }
                    }
                    Gx[row][col] = sum;
                }
            }
        }
    }
    
    // Combine Vertical and Horizontal gradients using Pythagoras theorem
    // Magnitude = sqrt(Gx^2 + Gy^2)
    private void Magnitude() {
        double sum = 0;
        double var = 0; 
        double totalPixel = (H-1) * (W-1);
        mag = new double[H][W];
        
        for(int row = 1;row<H-1;row++) {
            for(int col=1;col<W-1;col++) {
                mag[row][col] = Math.sqrt((Gx[row][col] * Gx[row][col]) + (Gy[row][col] * Gy[row][col]));
                sum +=mag[row][col];
            }
        }
        
        // Calculate Mean and Standard Deviation (used for statistics if needed later)
        mean = (int) Math.round(sum / totalPixel);
        for(int row = 1;row<H-1;row++) {
            for(int col=1;col<W-1;col++) {
                double diff = mag[row][col] - (double)mean;
                var += (diff * diff);
            }
        }
        double tmpSTD = Math.sqrt(var / totalPixel);
        stdDev = (int)Math.round(tmpSTD);
    }
    
    // Calculate the angle of the edge (Gradient Direction)
    // Results are rounded to 0, 45, 90, or 135 degrees
    private void Direction() {
        double piRad = 180 / Math.PI;
        dir = new int[H][W];
        
        for(int row = 1;row<H-1;row++) {
            for(int col=1;col<W-1;col++) {
                // atan2 calculates the angle
                double theta = Math.atan2(Gy[row][col], Gx[row][col]) * piRad; 
                
                if (theta < 0) {
                    theta += 360.;
                }
                
                // Categorize the angle into 4 buckets for simpler processing later
                if (theta <= 22.5 || (theta > 157.5 && theta <= 202.5) || theta > 337.5) {
                    dir[row][col] = 0;      // Horizontal
                } else if ((theta > 22.5 && theta <= 67.5) || (theta > 202.5 && theta <= 247.5)) {
                    dir[row][col] = 45;     // Diagonal /
                } else if ((theta > 67.5 && theta <= 112.5) || (theta > 247.5 && theta <= 292.5)) {
                    dir[row][col] = 90;     // Vertical |
                } else {
                    dir[row][col] = 135;    // Diagonal \
                }
            }
        }
    }
    
    // --- STEP 2: NON-MAXIMUM SUPPRESSION ---
    // Makes thick edges thin by keeping only the "peak" pixel along the edge direction
    private void Suppression() {
        for(int row = 1;row<H-1;row++) {
            for(int col=1;col<W-1;col++) {
                double magnitude = mag[row][col];
                
                // Compare the current pixel with its neighbors along the gradient direction.
                // If it is NOT the maximum, suppress it (set to 0).
                switch(dir[row][col]) {
                    case 0 : // Check Left and Right
                        if (magnitude < mag[row][col - 1] && magnitude < mag[row][col + 1]) {
                            mag[row][col] = 0;
                        }
                        break;
                    case 45 : // Check Diagonal /
                        if (magnitude < mag[row - 1][col + 1] && magnitude < mag[row + 1][col - 1]) {
                            mag[row][col] = 0;
                        }
                        break;
                    case 90 : // Check Up and Down
                        if (magnitude < mag[row - 1][col] && magnitude < mag[row + 1][col]) {
                            mag[row][col] = 0;
                        }
                        break;
                    case 135 : // Check Diagonal \
                        if (magnitude < mag[row - 1][col - 1] && magnitude < mag[row + 1][col + 1]) {
                            mag[row][col] = 0;
                        }
                        break;
                }
            }
        }
    }
    
    // --- STEP 3: HYSTERESIS & OTSU ---
    // Finalize the edges.
    private void Hysteresis() {
        destImg = new BufferedImage(W, H, BufferedImage.TYPE_BYTE_BINARY); // Output is Binary (B&W)
        destRaster = destImg.getRaster();
        
        // Use Otsu's method to automatically find the best "High Threshold"
        tHigh = otsuThreshold();
        tLow = tHigh * tRatio; // Low Threshold is usually 50% of High
        
        for(int row = 1;row<H-1;row++) {
            for(int col=1;col<W-1;col++) {
                double magnitude = mag[row][col];
                int sample = 0;
                
                // Rule 1: Strong Edge? Keep it!
                if (magnitude >= tHigh) {
                    sample = 1;
                } 
                // Rule 2: Weak Edge? Kill it!
                else if (magnitude < tLow) {
                    sample = 0;
                } 
                // Rule 3: "Maybe" Edge (Between Low and High)?
                // Keep it ONLY if it is connected to a Strong Edge.
                else {
                    boolean connected = false;
                    for (int krow = -1; krow < 2; krow++) {
                        for (int kcol = -1; kcol < 2; kcol++) {
                            if (mag[row + krow][col + kcol] >= tHigh) {
                                connected = true;
                            }
                        }
                    }
                    sample = (connected) ? 1 : 0;
                }
                
                destRaster.setSample(col, row, 0, sample);
            }
        }     
    }
    
    // --- HELPER: OTSU THRESHOLD ---
    // A statistical method to find the optimal separation between background and foreground.
    private int otsuThreshold() {
        // Reuse the Histogram code we saw earlier
        ImageEnhancement IEHist = new ImageEnhancement(srcImg);
        int [] hist = IEHist.standardHistogram();
                
        int total_pix = W * H;
        float totalPVal = 0;
        for(int i=0;i<hist.length;i++) totalPVal += i * hist[i];
                
        int nB = 0; // Count Background
        int nF = 0; // Count Foreground
        float sumB = 0;
        float varMax = 0;
        int threshold = 0;
                
        // Test every possible threshold (0-255) to see which one separates the classes best
        for(int i=0;i<256;i++) {
            nB += hist[i];
            if(nB == 0) continue;
            nF = total_pix - nB;
            if (nF == 0) break;
            sumB += (float)(i * hist[i]);
            
            float wB = (float)nB / (float)total_pix;
            float wF = (float)nF / (float)total_pix;
            
            float meanB = sumB / (float)nB;
            float meanF = ((float)totalPVal - sumB) / (float)nF;
            
            // Calculate Variance Between Classes (We want to maximize this)
            float varBetween = (float)wB * (float)wF * (meanB-meanF) * (meanB-meanF);
            
            if(varBetween > varMax) {
                varMax = varBetween;
                threshold = i;
            }
        }
        return threshold;
    }   
}