package com.cxkr.picutildemo;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.ImageView;

import com.cxkr.picutildemo.recycle.memory.impl.LruMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;

import java.io.File;
import java.io.FileInputStream;

/**
 * Created by songyuanjin on 16/8/25.
 */
public class ImageUtil {
    private static ImageUtil instatnce;

    private static ImageLoader imageLoader;
    private DisplayImageOptions options;


    private LruMemoryCache memoryCache;

    public static ImageUtil getInstatnce(Context context) {
        if (instatnce == null) {
            instatnce = new ImageUtil();
            initImageLoader(context);
        }
        return instatnce;
    }

    private ImageUtil() {
        memoryCache = new LruMemoryCache(MAX_SIZE);
    }

    private static void initImageLoader(Context context) {
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(context).threadPriority(Thread.NORM_PRIORITY - 2).threadPoolSize(3).denyCacheImageMultipleSizesInMemory().build();
        ImageLoader.getInstance().init(config);
        imageLoader = ImageLoader.getInstance();


    }

    public void disPlay(String filePath, ImageView imageView) {
        // 使用DisplayImageOption.Builder()创建DisplayImageOptions
        options = new DisplayImageOptions.Builder()
                .showStubImage(R.drawable.album_default_loading_pic) // 设置图片下载期间显示的图片
                .showImageForEmptyUri(R.drawable.album_default_loading_pic) // 设置图片Uri为空或是错误的时候显示的图片
                .showImageOnFail(R.drawable.album_default_loading_pic) // 设置图片加载或解码过程中发生错误显示的图片
                .cacheInMemory(true) // 设置下载的图片是否缓存在内存中
                .cacheOnDisc(true) // 设置下载的图片是否缓存在SD卡中
                // .displayer(new RoundedBitmapDisplayer(20)) // 设置成圆角图片
                .bitmapConfig(Bitmap.Config.ARGB_8888)
                .imageScaleType(ImageScaleType.IN_SAMPLE_INT)
                .build(); // 创建配置过的DisplayImageOption对象

        imageLoader.displayImage("file://" + filePath, imageView, options);
    }


    static final int MAX_SIZE = (int) Runtime.getRuntime().maxMemory() / 8;

    //读取dat文件
    public Bitmap loadImage(String filePath) {
        File file = new File(filePath);

        if (!file.exists() || file.isDirectory()) {
            return null;
        }

        try {
            //FileInputStream input = new FileInputStream(file);
            if (memoryCache.get(filePath) == null) {
                //Bitmap bitmap = BitmapFactory.decodeStream(input);
                Bitmap bitmap = decodeStreamThumbBitmapForIs(filePath);
                memoryCache.put(filePath, bitmap);
                return bitmap;
            }
            return memoryCache.get(filePath);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 生成缩略图
     *
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


    public Bitmap decodeStreamThumbBitmapForIs(String file) {
        try {
            FileInputStream input = new FileInputStream(file);
            int sample = 1;
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(input, null, options);
            int width = options.outWidth;
            int height = options.outHeight;
            if (width > 256 || height > 256) {
                sample = Math.round(((float) width) / 256.0f);
                width = Math.round(((float) height) / 256.0f);
                if (sample >= width) {
                    sample = width;
                }
            }

            options.inSampleSize = sample;
            options.inPreferredConfig = Bitmap.Config.RGB_565;
            options.inJustDecodeBounds = false;
            input.close();
            FileInputStream tmpInput = new FileInputStream(file);
            Bitmap bitmap = BitmapFactory.decodeStream(tmpInput, null, options);
            tmpInput.close();
            return bitmap;

        } catch (Exception e) {

        }


        return null;
    }
}
