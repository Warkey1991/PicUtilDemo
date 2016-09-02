package com.cxkr.picutildemo.blurry;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Handler;
import android.provider.MediaStore;

import com.cxkr.picutildemo.BitmapBO;
import com.cxkr.picutildemo.MainApplication;
import com.cxkr.picutildemo.ThreadPool;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by songyuanjin on 16/9/2.
 */
public class BlurryPictureUtil {
    /**
     * 计算模糊值接口
     */
    public interface OnCalculateBlurryValueListener {
        void onUpdate(BitmapBO bitmapBO, double value);

        void onSearchFilePath(String filePath);

        void onSearchFinish();
    }

    private List<OnCalculateBlurryValueListener> onCalculateBlurryValueListeners;

    public void addOnCalculateBlurryValueListener(OnCalculateBlurryValueListener onCalculateBlurryValueListener) {
        onCalculateBlurryValueListeners.add(onCalculateBlurryValueListener);
    }

    private static BlurryPictureUtil instance;
    private String filePath;
    private ExecutorService executorService;
    private Handler mHandler;
    private int calculatePictureCount = 0;
    private List<BitmapBO> blurryBOS;
    public static BlurryPictureUtil getInstance(Context context) {
        if (instance == null) {
            synchronized (BlurryPictureUtil.class) {
                instance = new BlurryPictureUtil();
            }
        }
        return instance;
    }

    private BlurryPictureUtil() {
        executorService = Executors.newFixedThreadPool(4);
        onCalculateBlurryValueListeners = new ArrayList<>();
        blurryBOS = new ArrayList<>();
        mHandler = new Handler();
    }

    public List<BitmapBO> getBlurryBOS() {
        return blurryBOS;
    }

    public void setCalculatePictureCount(int count) {
        this.calculatePictureCount = count;
    }

    /**
     * 计算模糊值
     */
    public void calculateBlurryValue(List<BitmapBO> bitmapBOList) {
        if (blurryBOS != null) {
            blurryBOS.clear();
        }
        for (BitmapBO bitmapBO : bitmapBOList) {
            calculateBlurry(bitmapBO);
        }
    }
    private void calculateBlurry(final BitmapBO bitmapBO) {
        this.filePath = bitmapBO.getFilePath();

        final double blurryValue = getBlurryVaules(MainApplication.getContext(), filePath);
        if (blurryValue != 1000000) {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    calculatePictureCount--;
                    if (blurryValue < 50) {
                        blurryBOS.add(bitmapBO);
                        for (OnCalculateBlurryValueListener calculateBlurryValueListener : onCalculateBlurryValueListeners) {
                            calculateBlurryValueListener.onUpdate(bitmapBO, blurryValue);
                            if (calculatePictureCount == 0) {
                                calculateBlurryValueListener.onSearchFinish();
                            }
                        }
                    }
                }
            }, 300);
            return;
        }
        ThreadPool.getInstance().addThreadRunable(new Runnable() {
            @Override
            public void run() {
                try {
                    int i;
                    int round;
                    double d;
                    System.currentTimeMillis();
                    Bitmap thumbnail = MediaStore.Images.Thumbnails.getThumbnail(MainApplication.getContext().getContentResolver(), bitmapBO.getId(), 1, null);
                    float width = ((float) thumbnail.getWidth()) / ((float) thumbnail.getHeight());
                    if (width > 1.0f) {
                        i = 64;
                        round = Math.round(64.0f * width);
                    } else {
                        i = Math.round(64.0f / width);
                        round = 64;
                    }

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

                                if (i4 > 100 || i4 < -100) {
                                    d += 1.0d;
                                }
                            }
                        }
                        if (createScaledBitmap != null) {
                            createScaledBitmap.recycle();
                        }
                    }
                    final double calculateVaule = d;
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            calculatePictureCount--;
                            saveBlurryValue(MainApplication.getContext(), calculateVaule, bitmapBO.getFilePath());
                            if (blurryValue < 50) {
                                blurryBOS.add(bitmapBO);
                                for (OnCalculateBlurryValueListener calculateBlurryValueListener : onCalculateBlurryValueListeners) {
                                    calculateBlurryValueListener.onUpdate(bitmapBO, calculateVaule);
                                    if (calculatePictureCount == 0) {
                                        calculateBlurryValueListener.onSearchFinish();
                                    }
                                }
                            }
                        }
                    });

                    if (thumbnail != null) {
                        thumbnail.recycle();
                    }
                } catch (Throwable th) {
                }
            }
        });
    }


    public static int getAverageAlgorithm(int i) {
        return (int) (((((double) ((i >> 8) & 255)) * 0.59d) + (0.3d * ((double) ((i >> 16) & 255)))) + (((double) (i & 255)) * 0.11d));
    }

    /**
     * 保存图片的模糊值
     *
     * @param context
     * @param value    模糊值
     * @param filePath 图片路径
     */
    public void saveBlurryValue(Context context, double value, String filePath) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("blurry_image_list", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putLong(filePath, (long) value);
        editor.commit();
    }

    /**
     * 获得图片的模糊值
     *
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
     *
     * @param context
     * @param filePath 图片路径
     */
    public void removeBlurryByFilePath(Context context, String filePath) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("blurry_image_list", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(filePath);
        editor.commit();
    }



}

