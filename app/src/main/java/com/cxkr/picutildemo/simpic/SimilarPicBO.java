package com.cxkr.picutildemo.simpic;

import com.cxkr.picutildemo.BitmapBO;

import java.io.Serializable;
import java.util.List;

/**
 * Created by songyuanjin on 16/8/25.
 */
public class SimilarPicBO implements Serializable {
    private boolean a = false;
    private String timeName;
    private long similarPicSize;
    private List<BitmapBO> samePics;

    public String getTimeName() {
        return timeName;
    }

    public void setTimeName(String timeName) {
        this.timeName = timeName;
    }

    public List<BitmapBO> getSamePics() {
        return samePics;
    }

    public void setSamePics(List<BitmapBO> samePic) {
        this.samePics = samePic;
    }

    public long getSimilarPicSize() {
        similarPicSize = 0;
        for (BitmapBO bitmapBO : samePics) {
            similarPicSize  += bitmapBO.getSize();
        }
        return similarPicSize;
    }

    public void setSimilarPicSize(long similarPicSize) {
        this.similarPicSize = similarPicSize;
    }

    public void deletedBitmapBO (BitmapBO bo) {
        if (samePics != null && samePics.contains(bo)) {
            samePics.remove(bo);
        }
    }
}
