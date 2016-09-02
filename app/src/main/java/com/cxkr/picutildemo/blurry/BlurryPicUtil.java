package com.cxkr.picutildemo.blurry;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.provider.MediaStore;
import com.cxkr.picutildemo.MainApplication;
import java.util.HashMap;

/**
 * Created by songyuanjin on 16/8/19.
 */
public final class BlurryPicUtil extends Thread {
    public double mediaId = 0.0d;
    private long origId = -1;
    private String filePath;

    public BlurryPicUtil(long mediaId, String filePath) {
        super("BlurryCreateImgThumbThread");
        this.origId = mediaId;
        this.filePath = filePath;
    }

    public final void run() {
        try {
            double blurryValue = getBlurryVaules(MainApplication.getContext(), filePath);
            if (blurryValue != 1000000) {
                mediaId = blurryValue;
                return;
            }
            int i;
            int round;
            double d;
            System.currentTimeMillis();
            Bitmap thumbnail = MediaStore.Images.Thumbnails.getThumbnail(MainApplication.getContext().getContentResolver(), origId, 1, null);
            float width = ((float) thumbnail.getWidth()) / ((float) thumbnail.getHeight());
           if (width > 1.0f) {
                i = 64;
                round = Math.round(64.0f * width);
           } else {
                i = Math.round(64.0f / width);
                round = 64;
            }

        /*    if (width > 1.0f) {
                i = 32;
                round = Math.round(32.0f * width);
            } else {
                i = Math.round(32.0f / width);
                round = 32;
            }*/
            Bitmap createScaledBitmap = Bitmap.createScaledBitmap(thumbnail, round, i, false);
            if (createScaledBitmap == null) {
                d = 0.0d;
            } else {
                int[][] iArr = new int[][]{new int[]{0, 1, 0}, new int[]{1, -4, 1}, new int[]{0, 1, 0}};
                d = 0.0d;
                for (int i2 = 1; i2 < i - 1; i2++) {
                    for (int i3 = 1; i3 < round - 1; i3++) {
                        int i4 = 0;
                        for (int i5 = -1; i5 <= 1; i5++) {
                            for (int i6 = -1; i6 <= 1; i6++) {
                                i4 += getAverageAlgorithm(createScaledBitmap.getPixel(i3 + i6, i2 + i5)) * iArr[i6 + 1][i5 + 1];
                            }
                        }

                       /* if (i4 > 120 || i4 < -120) {
                            d += 1.0d;
                        }*/
                        if (i4 > 100 || i4 < -100) {
                            d += 1.0d;
                        }
                    }
                }
                if (createScaledBitmap != null) {
                    createScaledBitmap.recycle();
                }
            }
            mediaId = d;
            saveBlurryValue(MainApplication.getContext(), mediaId, filePath);
            if (thumbnail != null) {
                thumbnail.recycle();
            }
        } catch (Throwable th) {
        }
    }


    public static int getAverageAlgorithm(int i) {
        return (int) (((((double) ((i >> 8) & 255)) * 0.59d) + (0.3d * ((double) ((i >> 16) & 255)))) + (((double) (i & 255)) * 0.11d));
    }

    public static double blurryValue(long mediaFileId, String filePath) {
        BlurryPicUtil junkBlurryPicUtil = new BlurryPicUtil(mediaFileId, filePath);
        junkBlurryPicUtil.start();
        try {
            junkBlurryPicUtil.join(5000);
        } catch (InterruptedException e) {
        }
        return junkBlurryPicUtil.mediaId;
    }


    /**
     * 保存图片的模糊值
     * @param context
     * @param value     模糊值
     * @param filePath  图片路径
     */
    private void saveBlurryValue(Context context, double value, String filePath) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("blurry_image_list", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putLong(filePath, (long)value);
        editor.commit();
    }

    /**
     * 获得图片的模糊值
     * @param context
     * @param filePath 图片路径
     * @return
     */
    private double getBlurryVaules(Context context, String filePath) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("blurry_image_list", Context.MODE_PRIVATE);
        HashMap<String, Long> blurryMap = (HashMap) sharedPreferences.getAll();
        if (blurryMap.containsKey(filePath)) {
            return blurryMap.get(filePath);
        }
        return 1000000;
    }

    /**
     * 移除特定的图片信息
     * @param context
     * @param filePath 图片路径
     */
    public void removeBlurryByFilePath(Context context, String filePath) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("blurry_image_list", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor =  sharedPreferences.edit();
        editor.remove(filePath);
        editor.commit();
    }
}
