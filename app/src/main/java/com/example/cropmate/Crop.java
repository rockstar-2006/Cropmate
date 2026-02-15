package com.example.cropmate;

public class Crop {
    private String name;
    private int imageResId;
    private int depthCm;
    private String imageUrl;
    private String reason;

    public Crop(String name, int imageResId, int depthCm, String imageUrl, String reason) {
        this.name = name;
        this.imageResId = imageResId;
        this.depthCm = depthCm;
        this.imageUrl = imageUrl;
        this.reason = reason;
    }

    public String getName() { return name; }
    public int getImageResId() { return imageResId; }
    public int getDepthCm() { return depthCm; }
    public String getImageUrl() { return imageUrl; }
    public String getReason() { return reason; }
}
