package javaanpr.analysis;

import java.awt.image.BufferedImage;

public class RecognitionResult {
    public BufferedImage image;
    public double iouScore;
    public String status;

    public RecognitionResult(BufferedImage image, double iouScore, String status) {
        this.image = image;
        this.iouScore = iouScore;
        this.status = status;
    }
}