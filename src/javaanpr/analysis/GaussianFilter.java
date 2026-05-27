package javaanpr.analysis;

import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;

public class GaussianFilter {
    
    // This method applies a "Gaussian Blur" to reduce noise in the image.
    // bi = Input Image
    // rad = Radius (How big is the blur? e.g., 7 pixels)
    // sigma = Strength (How "spread out" is the blur math?)
    public BufferedImage GaussianBlur(BufferedImage bi, int rad, double sigma) {
        
        // 1. Setup the output image (Destination)
        BufferedImage destImg = new BufferedImage(bi.getWidth(), bi.getHeight(), bi.getType());
        
        // Get access to the raw pixel data for both Input (bi) and Output (dest)
        WritableRaster biRaster = bi.getRaster();
        WritableRaster destRaster = destImg.getRaster();
        
        int height = bi.getHeight();
        int width = bi.getWidth();
        
        // 2. Prepare the "Kernel"
        // The Kernel is a small array of numbers representing a "Bell Curve".
        // It tells us how much importance to give to the center pixel vs. its neighbors.
        double norm = 0.0; // This will hold the sum of all weights (for normalization)
        double sigma2 = sigma * sigma; // Sigma squared (used in the formula)
        float[] kernel = new float[2 * rad + 1]; // Array size depends on radius
        
        // Loop to calculate the Gaussian Weight for every position in the kernel
        for (int x = -rad; x < rad + 1; x++) {
            // The Gaussian Formula: e^(-0.5 * x^2 / sigma^2)
            float exp = (float)Math.exp(-0.5 * (x * x) / sigma2);
            
            // Finalize the weight calculation
            kernel[x + rad] = (float)(1 / (2 * Math.PI * sigma2)) * exp;
            
            // Add to total sum so we can normalize later (make sure brightness stays constant)
            norm += kernel[x + rad];
        }
        
        // 3. Pass 1: Horizontal Blur
        // We slide the kernel across the image left-to-right.
        for (int row = 0; row < height; row++) {
            // We start at 'rad' and end at 'width - rad' to avoid crashing at the edges
            for (int col = rad; col < width - rad; col++) {
                double sum = 0.0;
                
                // Look at neighbors to the Left and Right
                for (int y = -rad; y < rad + 1; y++) {
                    // Get neighbor pixel value
                    int sample = biRaster.getSample(col + y, row, 0);
                    
                    // Multiply neighbor by its Kernel Weight and add to sum
                    sum += (kernel[y + rad] * sample); 
                }
                
                // Normalize: Divide by the total weight to keep image brightness correct
                sum /= norm;
                
                // Save the new pixel value
                destRaster.setSample(col, row, 0, Math.round(sum));
            }
        }
        
        // 4. Pass 2: Vertical Blur
        // We slide the kernel across the image top-to-bottom.
        // Note: In standard Gaussian Blur, you usually use the result of Pass 1 as input here.
        // This specific code reads from the ORIGINAL image again, which is a unique choice.
        for (int row = rad; row < height - rad; row++) {
            for (int col = 0; col < width; col++) {
                double sum = 0.;
                
                // Look at neighbors Above and Below
                for(int x = -rad; x < rad + 1; x++) {
                    int sample = biRaster.getSample(col, row + x, 0);
                    sum += (kernel[x + rad] * sample);
                }
                
                // Normalize
                sum /= norm;
                destRaster.setSample(col, row, 0, Math.round(sum));
            }
        }
        
        // 5. Border Handling (The "Frame")
        // Because the loops above skipped the edges (to avoid ArrayOutOfBounds errors),
        // the edges of the image would be black. This loop manually copies the original pixels
        // to the edges so the image looks complete.
        for(int row = 0; row < rad; row++) {
            for (int col = 0; col < rad; col++) {
                // Top-Left corners
                destRaster.setSample(col, row, 0, biRaster.getSample(col, row, 0)); 
                
                // Top-Right corners
                destRaster.setSample(biRaster.getWidth()-1-col, row, 0, biRaster.getSample(biRaster.getWidth()-1-col, row, 0)); 
                
                // Bottom-Left corners
                destRaster.setSample(col, biRaster.getHeight()-1-row, 0, biRaster.getSample(col, biRaster.getHeight()-1-row, 0)); 
                
                // Bottom-Right corners
                destRaster.setSample(biRaster.getWidth()-1-col, biRaster.getHeight()-1-row, 0, biRaster.getSample(biRaster.getWidth()-1-col, biRaster.getHeight()-1-row, 0)); 
            }
        }
        
        return destImg;
    }
}