package javaanpr.analysis;

import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;

public class ImageEnhancement {

    // W = Width, H = Height
    private final int W, H;
    // srcImg = Input Image, destImg = Output (Processed) Image
    private BufferedImage srcImg, destImg;
    // Raster objects allow us to access the raw pixel data directly
    private WritableRaster srcRaster, destRaster;
    // K = 256 means we are working with 8-bit grayscale (0 to 255 brightness levels)
    private final int K = 256; 
    
    // Constructor: Loads the image and prepares dimensions
    public ImageEnhancement(BufferedImage bi) {
        this.srcImg = bi;
        this.W = bi.getWidth();
        this.H = bi.getHeight();
        srcRaster = srcImg.getRaster();
    }
    
    // --- MAIN METHOD: The actual logic ---
    public BufferedImage HistogramEqualization() {
        
        // 1. Create a blank grayscale image for the result
        destImg = new BufferedImage(W, H, BufferedImage.TYPE_BYTE_GRAY);
        destRaster = destImg.getRaster();
        
        // 2. Get the "math" needed to fix the image
        // First, count how many times each shade of gray appears (standardHistogram)
        int [] histogram = standardHistogram(); 
        
        // Second, calculate the running total (cumulativeHistogram)
        // This tells us the statistical "rank" of each brightness level
        int [] culHistogram = cumulativeHistogram(histogram);
        
        // 3. Loop through every single pixel in the image
        for(int row=0; row < H; row++) {
            for(int col=0; col < W; col++) {
                
                // Get the OLD brightness value (0-255)
                int sample = srcRaster.getSample(col, row, 0);
                
                // CALCULATE THE NEW BRIGHTNESS
                // Formula: (Cumulative Frequency * Max Brightness) / Total Pixels
                // This maps the old value to a new, better-distributed value
                int HE = culHistogram[sample] * (K - 1) / (W * H);
                
                // Save the NEW brightness value to the destination image
                destRaster.setSample(col, row, 0, HE);
            }
        }
        
        return destImg;
    }
    
    // --- HELPER METHOD 1: Count Frequency ---
    // This creates a simple "bar chart" of the current image's brightness
    public int[] standardHistogram() {
        
        int[] histogram = new int[256]; // Array indices 0 to 255 represent brightness levels
        
        // Initialize counts to 0
        for(int i=0; i<histogram.length; i++) {
            histogram[i] = 0;
        }
            
        // Scan the whole image
        for(int row=0; row < H; row++) {
            for(int col=0; col < W; col++) {
                int sample = srcRaster.getSample(col, row, 0); // Get pixel value
                histogram[sample]++; // Increment the counter for that specific gray value
            }
        }
        
        return histogram;
    }
    
    // --- HELPER METHOD 2: Accumulate Counts ---
    // This calculates the Cumulative Distribution Function (CDF)
    // It answers: "How many pixels are THIS bright OR darker?"
    public int[] cumulativeHistogram(int[] H) {
        int[] culHistogram = H.clone();
        
        // Loop starts at 1 because index 0 stays the same
        for(int j = 1; j < culHistogram.length; j++) {
            // New Value = Previous Total + Current Count
            culHistogram[j] = culHistogram[j-1] + culHistogram[j];
        }
        
        return culHistogram;
    }
}