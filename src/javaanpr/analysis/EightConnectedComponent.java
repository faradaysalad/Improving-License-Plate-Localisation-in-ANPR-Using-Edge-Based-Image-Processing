package javaanpr.analysis;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.util.ArrayList;
import java.util.List;

public class EightConnectedComponent {
    
    private BufferedImage srcImg;
    private WritableRaster srcRaster;
    private final int height, width;
    private int [][]imgAry;
    private int [] EQAry;
    private BoundingBox []boxes;
    private int[] pixelCount;
    private int newLabel;
    
    // Helper class to store raw region data
    public class Region {
        public Rectangle rect;
        public int pixelCount;
        
        public Region(int x, int y, int w, int h, int count) {
            this.rect = new Rectangle(x, y, w, h);
            this.pixelCount = count;
        }
    }
    
    public EightConnectedComponent(BufferedImage bi) {
        this.srcImg = bi;
        this.width = bi.getWidth();
        this.height = bi.getHeight();
        srcRaster = srcImg.getRaster();
    }
    
    // 5. Region Extraction (Main Method)
    public void EightCCL() {     
        imgAry = new int [height+2][width+2];
        newLabel = 0;
        EQAry = new int[(height*width)/2];
        for (int i=0;i<EQAry.length;i++) {
            EQAry[i] = 0;
        }
        
        initAry(); 
        loadImage();
        
        CCL_Pass1();
        CCL_Pass2();
        
        int count = arrangeEQAry();
        
        CCL_Pass3(count);
        // Note: Localize() is REMOVED. Filtering is now done in SelectPlate.
    }
    
    // Getter to retrieve all raw regions found
    public List<Region> getRawRegions() {
        List<Region> regionList = new ArrayList<>();
        
        // Loop through all boxes found in CCL_Pass3
        for(int i=1; i < boxes.length; i++) {
            if (boxes[i] == null) continue;
            
            // Calculate width and height
            int w = boxes[i].maxcol - boxes[i].mincol + 1;
            int h = boxes[i].maxrow - boxes[i].minrow + 1;
            
            // Basic sanity check to avoid crashes (width/height > 0)
            if (w > 0 && h > 0) {
                // Save the region (x, y, w, h) and the pixel count (for density calculation later)
                regionList.add(new Region(boxes[i].mincol, boxes[i].minrow, w, h, pixelCount[i]));
            }
        }
        return regionList;
    }
    
    public void initAry() {
        for(int i = 0;i<height;i++) {
            for(int j=0; j<width;j++) {
                imgAry[i][j] = 0;
            }
        }       
    }
    
    public void loadImage() {
        for(int row=0; row<height; row++) {
            for(int col=0; col<width; col++) {
                int sample = srcRaster.getSample(col, row, 0);
                imgAry[row+1][col+1] = sample;
            }
        }
    }
    
    public int arrangeEQAry() {
        int count = 0;
        for(int i = 1; i < newLabel+1; i++) {
            if(EQAry[i] == i) {
                count++;
                EQAry[i] = count;
            }
            else {
                EQAry[i] = EQAry[EQAry[i]];
            }
        }
        return count;
    }
    
    public void CCL_Pass1() {
        for(int row=1; row<height+1; row++) {
            for(int col=1; col<width+1; col++) {
                int pixel = imgAry[row][col];
                
                if(pixel > 0) {
                    int NW = imgAry[row-1][col-1];
                    int N = imgAry[row-1][col];
                    int NE = imgAry[row-1][col+1];
                    int W = imgAry[row][col-1];
                    
                    if(NW == 0 && N == 0 && NE == 0 && W == 0) {
                        newLabel += 1;
                        imgAry[row][col] = newLabel;
                    }
                    else if((NW != 0 || N != 0 || NE != 0 || W != 0) && 
                            ( (NW != 0 && (NW == N || NW == NE || NW == W)) || 
                                (N != 0 && (N == NE || N == W)) ||
                                    (NE != 0 && (NE == W)) )
                            ) {
                        int tmp = -1;
                        if(NW != 0) tmp = NW;
                        else if(N != 0) tmp = N;
                        else if(NE != 0) tmp = NE;
                        else if(W != 0) tmp = W;
                        imgAry[row][col] = tmp;
                    }
                    else if(NW != 0 || N != 0 || NE != 0 || W != 0) {
                        int min = newLabel;
                        int max = NW;
                        
                        if (NW != 0 && NW<min) min = NW;
                        if (N>max) max = N;
                        if (N != 0 && N<min) min = N;
                        if (NE>max)  max = NE;
                        if (NE != 0 && NE<min) min = NE;
                        if (W>max) max = W;
                        if (W != 0 && W<min) min = W;
                        
                        imgAry[row][col] = min;
                        EQAry[max] = min; 
                    }
                }
            }
        }
    }
    
    public void CCL_Pass2() {
        for(int row=height; row>1; row--) {
            for(int col=width; col>1; col--) {
                int pixel = imgAry[row][col];
                
                if(pixel > 0) {
                    int E = imgAry[row][col+1];
                    int SW = imgAry[row+1][col-1];
                    int S = imgAry[row+1][col];
                    int SE = imgAry[row+1][col+1];
                    
                    if( (E != pixel && E != 0) || (SW != pixel && SW != 0) || 
                            (S != pixel && S != 0) || (SE != pixel && SE != 0) ) {
                        int min = pixel;
                        int max = pixel;
                        if(E != 0 && E<min) min = E;
                        if(E>max) max = E;
                        if(SW != 0 && SW<min) min = SW;
                        if(SW>max) max = SW;
                        if(S != 0 && S<min) min = S;
                        if(S>max) max = S;
                        if(SE !=0 && SE<min) min = SE;
                        
                        imgAry[row][col] = min;
                        EQAry[max] = min;
                    }
                }
            }
        }
    }
    
    public void CCL_Pass3(int count) {
        pixelCount = new int[count+1];
        boxes = new BoundingBox[count+1];
        
        for(int i=0; i< count+1; i++) {
            pixelCount[i] = 0;
            boxes[i] = new BoundingBox((height*width)/4);
        }
        
        for(int row=1; row<height+1; row++) {
            for(int col=1; col<width+1; col++) {
                int pixel = imgAry[row][col];
                
                if(pixel>0) {
                    if(pixel != EQAry[pixel]) {
                        imgAry[row][col] = EQAry[pixel];
                    }
                    
                    if(boxes[imgAry[row][col]].minrow > row) boxes[imgAry[row][col]].minrow = row-1;
                    if(boxes[imgAry[row][col]].mincol > col) boxes[imgAry[row][col]].mincol = col-1;
                    if(boxes[imgAry[row][col]].maxrow < row) boxes[imgAry[row][col]].maxrow = row-1;
                    if(boxes[imgAry[row][col]].maxcol < col) boxes[imgAry[row][col]].maxcol = col-1;
                }
                
                pixelCount[imgAry[row][col]]++;
            }
        }
    }
}