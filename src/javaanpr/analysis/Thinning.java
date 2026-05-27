package javaanpr.analysis;

import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;

public class Thinning {
    
    private BufferedImage srcImg, destImg;
    private WritableRaster srcRaster, destRaster;
    private final int height, width;
    
    // Controls the main loop. If we delete a pixel, we set this to true to keep going.
    private boolean changeFlag = true; 
    
    // Arrays to hold the pixel data. 
    // We use "padding" (height+2, width+2) to avoid IndexOutOfBounds errors at the edges.
    private int [][]firstAry;
    private int [][]secondAry;
    
    public Thinning(BufferedImage bi) {
        this.srcImg = bi;
        this.width = bi.getWidth();
        this.height = bi.getHeight();
        srcRaster = srcImg.getRaster();
    }
    
    // --- MAIN METHOD ---
    public BufferedImage Skeletonize() {
        // Create arrays slightly bigger than the image (padding)
        firstAry = new int[height+2][width+2];
        secondAry = new int[height+2][width+2];
        
        initAry();   // Fill with zeros
        loadImage(); // Load the current image data into firstAry
        
        // KEEP LOOPING until we go through a full pass without deleting any pixels
        while(changeFlag != false) {
            changeFlag = false; // Assume we are done unless we prove otherwise
            
            // 1. North Scan: Look for pixels to delete from the TOP edge of lines
            for(int row = 1; row<height+1; row++){
                for(int col=1; col<width+1; col++) {
                    // If current pixel is White (Edge) AND pixel above is Black (Background)
                    if((firstAry[row][col] > 0) && (firstAry[row-1][col] == 0)) {
                        doThinning(row, col, changeFlag);
                    }
                }
            }
            copyAry(); // Update the main array with deletions
            
            // 2. South Scan: Look for pixels to delete from the BOTTOM edge
            for(int row = 1; row<height+1; row++){
                for(int col=1; col<width+1; col++) {
                    if((firstAry[row][col] > 0) && (firstAry[row+1][col] == 0)) {
                        doThinning(row, col, changeFlag);
                    }
                }
            }
            copyAry();
            
            // 3. West Scan: Look for pixels to delete from the LEFT edge
            for(int row = 1; row<height+1; row++){
                for(int col=1; col<width+1; col++) {
                    if((firstAry[row][col] > 0) && (firstAry[row][col-1] == 0)) {
                        doThinning(row, col, changeFlag);
                    }
                }
            }
            copyAry();
            
            // 4. East Scan: Look for pixels to delete from the RIGHT edge
            for(int row = 1; row<height+1; row++){
                for(int col=1; col<width+1; col++) {
                    if((firstAry[row][col] > 0) && (firstAry[row][col+1] == 0)) {
                        doThinning(row, col, changeFlag);
                    }
                }
            }
            copyAry();
        }
        
        // Convert the final array back into a BufferedImage to return
        setImage();
        
        return destImg;
    }
    
    // Initialize arrays with 0 (Black)
    public void initAry() {
        for(int i = 0;i<height+2;i++) {
            for(int j=0; j<width+2;j++) {
                firstAry[i][j] = 0;
                secondAry[i][j] = 0;
            }
        }
    }
    
    // Copy image pixels into the processing array
    public void loadImage() {
        for(int row=0; row<height; row++) {
            for(int col=0; col<width; col++) {
                int sample = srcRaster.getSample(col, row, 0);
                firstAry[row+1][col+1] = sample;
                secondAry[row+1][col+1] = sample;
            }
        }
    }
    
    // --- THE LOGIC: Should I delete this pixel? ---
    public void doThinning(int r, int c, boolean flag) {
        int nonZero = -1; // Count neighbors
        boolean valid = false; // Is this pixel "safe" to delete?
        
        // Count how many neighbors are White (1)
        for(int kr = -1; kr<=1; kr++) {
            for(int kc = -1; kc<=1; kc++) {
                if(firstAry[r+kr][c+kc] != 0) nonZero++;
            }
        }
        
        // Get all 8 neighbors
        // P1 P2 P3
        // P4 PC P6
        // P7 P8 P9
        int p1 = firstAry[r-1][c-1];
        int p2 = firstAry[r-1][c];
        int p3 = firstAry[r-1][c+1];
        int p4 = firstAry[r][c-1];
        int p6 = firstAry[r][c+1];
        int p7 = firstAry[r+1][c-1];
        int p8 = firstAry[r+1][c];
        int p9 = firstAry[r+1][c+1];
        
        // --- COMPLEX RULES AHEAD ---
        // These rules ensure we don't break a line into two pieces.
        // We only delete a pixel if it's on the edge AND removing it won't break connectivity.
        
        // Check structural integrity (prevent breaking lines)
        if( (p2 == 0 && p8 == 0) || (p4 == 0 && p6 == 0) || 
            (p1 == 1 && p2 == 0 && p4 == 0) || (p6 == 0 && p8 == 0 && p9 == 1) ||
            (p2 == 0 && p3 == 1 && p6 == 0) || (p4 == 0 && p7 == 1 && p8 == 0) )
            valid = true;
        
        // Check if it's an "endpoint" (we don't want to eat away the tips of lines)
        if( (p1 == 0 && p9 == 0 && ((p6 !=1 && p8 != 1) || (p2 != 1 && p4 != 1))) || 
            (p3 == 0 && p7 == 0 && ((p2 != 1 && p4 != 1) || (p6 != 1 && p8 != 1))) )
            valid = true;
        
        // DECISION:
        // If it has enough neighbors (>=4) AND it failed the "valid" checks above (meaning it's NOT critical structure),
        // THEN delete it (set to 0).
        if (nonZero >= 4 && valid == false) {
            secondAry[r][c] = 0; // Erase pixel
            changeFlag = true;   // We made a change, so the loop must run again
        }
        else {
            secondAry[r][c] = 1; // Keep pixel
        }
    }
    
    // Updates the main array for the next pass
    public void copyAry() {
        for(int row = 1; row<height+1; row++){
            for(int col=1; col<width+1; col++) {
                firstAry[row][col] = secondAry[row][col];
            }
        }    
    }
    
    // Saves the result back to an Image object
    public void setImage() {
        destImg = new BufferedImage(srcImg.getWidth(), srcImg.getHeight(), BufferedImage.TYPE_BYTE_BINARY);
        destRaster = destImg.getRaster();
        
        for(int row = 1; row<height+1; row++){
            for(int col=1; col<width+1; col++) {
                int sample = firstAry[row][col];
                
                // Invert colors for display if needed (Optional depending on your display logic)
                if(sample == 0) sample = 1;
                else sample = 0;
                
                destRaster.setSample(col-1, row-1, 0, sample);
            }
        }
    }
}