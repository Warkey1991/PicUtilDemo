package com.cxkr.picutildemo.simpic;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.text.TextUtils;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;

/**
 * Created by yuanjin.Song on 2016/7/4.
 */
public class SimPicUtil {
    private static final int SIXTEEN = 16;
    private static final int EIGHT = 8;
    private double[] picArr;

    public SimPicUtil() {
        initArr();
    }

    private void initArr() {
        picArr = new double[SIXTEEN];
        for (int i = 1; i < SIXTEEN; i++) {
            picArr[i] = 1.0d;
        }
        picArr[0] = 1.0d / Math.sqrt(2.0d);
    }

    /**
     * 图片压缩处理
     * @param bitmap
     * @return
     */
    public final Bitmap compressBitmap(Bitmap bitmap) {
        if (bitmap == null) {
            return null;
        }
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        float f = ((float) SIXTEEN) / ((float) width);
        float f2 = ((float) SIXTEEN) / ((float) height);
        Matrix matrix = new Matrix();
        matrix.postScale(f, f2);
        Bitmap createBitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, false);
        bitmap.recycle();
        return createBitmap;
    }

    /**
     * 生成缩略图
     * @param str
     * @return
     */
    public final Bitmap decodeThumbBitmapForFile(String str) {
        int sample = 1;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(str, options);
        int width = options.outWidth;
        int height = options.outHeight;
        if (width > 128 || height > 128) {
            sample = Math.round(((float) width) / 128.0f);
            width = Math.round(((float) height) / 128.0f);
            if (sample >= width) {
                sample = width;
            }
        }
        options.inSampleSize = sample;
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(str, options);
    }

    public final double getDefinition(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int[] iArr = new int[(width * height)];
        bitmap.getPixels(iArr, 0, width, 0, 0, width, height);
        double size = (double) (width * height);
        double d2 = 0.0d;
        for (int i = 0; i < height - 1; i++) {
            for (int j = 0; j < width - 1; j++) {
                d2 = (d2 + Math.sqrt(Math.pow((double) (iArr[((i + 1) * width) + j] - iArr[(i * width) + j]), 2.0d)
                        + Math.pow((double) (iArr[((i * width) + j) + 1] - iArr[(i * width) + j]), 2.0d)))
                        + ((double) (Math.abs(iArr[((i + 1) * width) + j] - iArr[(i * width) + j])
                        + Math.abs(iArr[((i * width) + j) + 1] - iArr[(i * width) + j])));
            }
        }
        return d2 / size;
    }

    public final int[] getGrayArry(Bitmap bitmap) {
        int i;
        int i2;
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int[] iArr = new int[(width * height)];
        bitmap.getPixels(iArr, 0, width, 0, 0, width, height);
        for (i = 0; i < height; i++) {
            for (i2 = 0; i2 < width; i2++) {
                int i3 = iArr[(width * i) + i2];
                i3 = (int) ((((double) ((float) (i3 & 255))) * 0.11d) + ((((double) ((float) ((16711680 & i3) >> 16))) * 0.3d) + (((double) ((float) ((65280 & i3) >> 8))) * 0.59d)));
                iArr[(width * i) + i2] = i3 | ((-16777216 | (i3 << 16)) | (i3 << 8));
            }
        }
        height = SIXTEEN;
        int[] iArr2 = new int[(height * height)];
        for (int i4 = 0; i4 < height; i4++) {
            for (int i5 = 0; i5 < height; i5++) {
                i = 0;
                for (int i3 = 0; i3 < height; i3++) {
                    for (i2 = 0; i2 < height; i2++) {
                        i = (int) (((double) i) + ((Math.cos(((((double) ((i3 * 2) + 1)) / (2.0d * ((double) height))) * ((double) i4)) * Math.PI) * Math.cos(((((double) ((i2 * 2) + 1)) / (2.0d * ((double) height))) * ((double) i5)) * Math.PI)) * ((double) iArr[(height * i3) + i2])));
                    }
                }
                iArr2[(height * i4) + i5] = (int) (((double) i) * ((picArr[i4] * this.picArr[i5]) / 4.0d));
            }
        }
        int i6 = 0;
        for (i2 = 0; i2 < EIGHT; i2++) {
            i = 0;
            while (i < EIGHT) {
                int i3 = iArr2[(width * i2) + i] + i6;
                i++;
                i6 = i3;
            }
        }
        i = (i6 - iArr2[0]) / ((EIGHT * EIGHT) - 1);
        int[] iArr3 = new int[(EIGHT * EIGHT)];
        for (i2 = 0; i2 < EIGHT; i2++) {
            for (i6 = 0; i6 < EIGHT; i6++) {
                if (iArr2[(width * i2) + i6] > i) {
                    iArr3[(EIGHT * i2) + i6] = 0;
                } else {
                    iArr3[(EIGHT * i2) + i6] = 1;
                }
            }
        }
        return iArr3;
    }

    public final boolean isSimilar(int[] iArr, int[] iArr2, long j) {
        if (iArr.length != iArr2.length) {
            return false;
        }
        int i = 0;
        for (int i2 = 0; i2 < iArr.length; i2++) {
            if (iArr[i2] != iArr2[i2]) {
                i++;
            }
        }
        return i < 17 || ((Math.abs(j) <= 3000 && i < 19) || ((Math.abs(j) <= 2000 && i < 21) || (Math.abs(j) <= 1000 && i < 25)));
    }


    public static Bitmap getBitmapByWidth(String str, int width, int sample) {
        Bitmap bitmap = null;
        if (!TextUtils.isEmpty(str)) {
            try {
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                BitmapFactory.decodeFile(str, options);
                int i3 = options.outHeight;
                if (options.outWidth > width) {
                    options.inSampleSize = ((options.outWidth / width) + 1) + sample;
                    options.outWidth = width;
                    options.outHeight /= options.inSampleSize;
                }
                options.inJustDecodeBounds = false;
                bitmap = BitmapFactory.decodeFile(str, options);
            } catch (Throwable th) {
                th.printStackTrace();
            }
        }
        return bitmap;
    }

    public static long getStrTime(String str) {
        try {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss");
            String attribute = new ExifInterface(str).getAttribute("DateTime");
            if (attribute != null) {
                return simpleDateFormat.parse(attribute).getTime();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e2) {
            e2.printStackTrace();
        }
        return 0;
    }
}

