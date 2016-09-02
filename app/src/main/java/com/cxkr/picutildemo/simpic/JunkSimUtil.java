package com.cxkr.picutildemo.simpic;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.media.ExifInterface;
import android.os.Handler;
import android.text.TextUtils;

import com.cxkr.picutildemo.BitmapBO;
import com.cxkr.picutildemo.ThreadPool;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
/**
 * Created by songyuanjin on 16/8/25.
 */
public class JunkSimUtil {
    private LinkedHashMap<BitmapBO, List<BitmapBO>> linkedHashMap;
    private SimPicUtil picUtil = new SimPicUtil();
    private Context mContext;
    private HashMap picMap;
    private Handler handler = new Handler();
    HashMap<String, SimilarPicBO> samePicMap = new HashMap<>();
    private List<SimilarPicBO> similarPicBOs;
    private boolean searchFinish = false;

    private List<Callback> callbacks = new ArrayList<>();
    private static JunkSimUtil junkSimUtil;

    public static JunkSimUtil getInstance(Context context) {
        if (junkSimUtil == null) {
            synchronized (JunkSimUtil.class) {
                junkSimUtil = new JunkSimUtil(context);
            }
        }
        return junkSimUtil;
    }

    private JunkSimUtil(Context context) {
        this.mContext = context;
        similarPicBOs = new ArrayList<>();
        searchFinish = false;
    }

    public boolean isSearchFinish() {
        return searchFinish;
    }

    public List<SimilarPicBO> getSimilarPicBOs() {
        return similarPicBOs;
    }

    public void deleteBitmapBO (SimilarPicBO similarPicBO){
        if (similarPicBOs.contains(similarPicBO)) {
            similarPicBOs.remove(similarPicBO);
        }
    }

    public void destory() {
        if (similarPicBOs != null) {
            similarPicBOs.clear();
            similarPicBOs = null;
            mThread = null;
        }
    }

    Thread mThread = null;
    public void doFindSimPic(final List<BitmapBO> bitmapBOs) {
        searchFinish = false;
        ThreadPool.getInstance().addThreadRunable(new Runnable() {
            @Override
            public void run() {
                doGroupBitmaps(bitmapBOs);
            }
        });

//        mThread = null;
//        if (mThread == null) {
//            mThread = new Thread(new Runnable() {
//                @Override
//                public void run() {
//
//                }
//            });
//            mThread.start();
//        }
    }

    public interface Callback {
        void onSearchingPath(String path);

        void onUpdate(List<SimilarPicBO> samePicBOs);

        void onFinish();
    }

    public void addListener(Callback callback) {
        callbacks.add(callback);
    }

    public void removeCallBackListener() {
        if (callbacks != null) {
            callbacks.clear();
        }
    }
    private void doGroupBitmaps(List<BitmapBO> bitmapBOs) {
        if (similarPicBOs != null) {
            similarPicBOs.clear();
        }
        similarPicBOs = new ArrayList<>();
        SharedPreferences sharedPreferences = mContext.getSharedPreferences("image_grayArray_list", Context.MODE_PRIVATE);
        picMap = (HashMap) sharedPreferences.getAll();
        SharedPreferences.Editor edit = sharedPreferences.edit();
        edit.clear();
        edit.commit();

        linkedHashMap = new LinkedHashMap();
        BitmapBO nextPicBO = bitmapBOs.get(0);  //第一张图片的数据
        long time = nextPicBO.getTime();  //第一张图片的时间
        int i = 1;
        BitmapBO keyPicBO = nextPicBO;
        while (i < bitmapBOs.size()) {   //分组，照片时间小于90s的在一组
            BitmapBO tmpPicBO;
            nextPicBO = bitmapBOs.get(i);
            long time2 = nextPicBO.getTime();  //待比较的图片的时间
            if (Math.abs(time - time2) > 90000) { //时间间隔大于90s
                tmpPicBO = nextPicBO;
            } else if (linkedHashMap.containsKey(keyPicBO)) {
                linkedHashMap.get(keyPicBO).add(nextPicBO);
                tmpPicBO = keyPicBO;
            } else {
                List arrayList = new ArrayList();
                arrayList.add(keyPicBO);
                arrayList.add(nextPicBO);
                linkedHashMap.put(keyPicBO, arrayList);
                tmpPicBO = keyPicBO;
            }
            i++;
            time = time2;
            keyPicBO = tmpPicBO;
        }


        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        int i2 = 0;
        for (Map.Entry key : linkedHashMap.entrySet()) {
            List<BitmapBO> greyPicBOs = linkedHashMap.get(key.getKey());
            if (greyPicBOs == null) {
                continue;
            }
            boolean[] zArr = new boolean[greyPicBOs.size()];
            int i3 = 0;
            int i4 = i2;

            while (i3 < greyPicBOs.size()) {
                int i5 = i4 + 1;
                ///////搜索路径

                final String path = (greyPicBOs.get(i3)).getFilePath();
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        for (Callback callback : callbacks) {
                            callback.onSearchingPath(path);
                        }
                    }
                });

                if (!zArr[i3]) {
                    nextPicBO = greyPicBOs.get(i3);
                    int[] a = getGrayArry(nextPicBO);
                    final SimilarPicBO samePicBO = new SimilarPicBO();
                    long samePicSize = 0;
                    List<BitmapBO> arrayList2 = new ArrayList();
                    samePicSize += nextPicBO.getSize();
                    arrayList2.add(nextPicBO);
                    BitmapBO qVar4 = nextPicBO;

                    //////找出相似图片
                    for (int startIndex = i3 + 1; startIndex < greyPicBOs.size(); startIndex++) {
                        if (!zArr[startIndex]) {
                            nextPicBO = greyPicBOs.get(startIndex);
                            int[] a2 = getGrayArry(nextPicBO);
                            if (picUtil.isSimilar(a, a2, qVar4.getTime() - nextPicBO.getTime())) {
                                zArr[startIndex] = true;
                                samePicSize += nextPicBO.getSize();
                                arrayList2.add(nextPicBO);
                                a = a2;
                                qVar4 = nextPicBO;
                            }
                        }
                    }

                    //入股大于2，则表示有相似的图片
                    if (arrayList2.size() >= 2) {
                        BitmapBO qVar3 = null;
                        int i6 = 0;

                        /////，并找出最佳的图片
                        while (i6 < arrayList2.size()) {
                            nextPicBO = arrayList2.get(i6);
                            if (i6 + 1 < arrayList2.size()) {
                                qVar3 = arrayList2.get(i6 + 1);
                                if (nextPicBO.getDefinition() > qVar3.getDefinition()) {
                                    qVar3 = nextPicBO;
                                }
                            }
                            try {
                                int attributeInt = new ExifInterface(nextPicBO.getFilePath()).getAttributeInt("Orientation", 0);
                                if (attributeInt == 6 || attributeInt == 3 || attributeInt == 8) {
                                    nextPicBO.setOrientation(attributeInt);
                                    nextPicBO.setBitmap(picUtil.getBitmapByWidth(nextPicBO.getFilePath(), 140, 0));
                                    i6++;
                                } else {
                                    i6++;
                                }
                            } catch (Exception e) {
                            }
                        }

                        samePicBO.setSimilarPicSize(samePicSize);
                        samePicBO.setSamePics(arrayList2);
                        samePicBO.setTimeName(simpleDateFormat.format(new Date((arrayList2.get(0)).getTime())));
                        qVar3.setChecked(false);
                        qVar3.setIsBestPicture(true);

                        similarPicBOs.add(samePicBO);
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                for (Callback callback : callbacks) {
                                    callback.onUpdate(similarPicBOs);
                                }
                            }
                        });

                        samePicMap.put(qVar3.getFilePath(), samePicBO);
                        if (picMap != null && picMap.size() > 0) {
                            try {
                                if (arrayList2.size() <= 5) {
                                    Thread.sleep(400);
                                } else {
                                    Thread.sleep((long) (arrayList2.size() * 60));
                                }
                            } catch (InterruptedException e2) {
                                e2.printStackTrace();
                            }
                        }
                    }
                }
                i3++;
                i4 = i5;
            }
            i2 = i4;
        }
        searchFinish = true;
        if (searchFinish) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    for (Callback callback : callbacks) {
                        callback.onFinish();
                    }
                    removeCallBackListener();
                }
            });
        }
    }

    private int[] getGrayArry(BitmapBO qVar) {
        int i = 0;
        int[] grayArry = qVar.getGrayArry();
        if (grayArry != null && grayArry.length > 0) {
            return grayArry;
        }
        grayArry = new int[64];
        double d = 0.0d;
        String filePath = qVar.getFilePath();
        if (TextUtils.isEmpty(filePath) || filePath.equals("")) {
            return grayArry;
        }
        int[] iArr;
        if (picMap != null && picMap.containsKey(filePath) && picMap.containsKey(filePath + "definition")) {
            char[] toCharArray = ((String) picMap.get(filePath)).toCharArray();
            for (int i2 = 0; i2 < toCharArray.length; i2++) {
                grayArry[i2] = Integer.parseInt(toCharArray[i2] + "");
            }
            try {
                d = Double.parseDouble((String) picMap.get(filePath + "definition"));
                iArr = grayArry;
            } catch (Exception e) {
                iArr = grayArry;
            }
        } else {
            Bitmap decodeThumbBitmapForFile = picUtil.decodeThumbBitmapForFile(filePath);
            if (decodeThumbBitmapForFile != null) {
                Bitmap compressBitmap = picUtil.compressBitmap(decodeThumbBitmapForFile);
                iArr = picUtil.getGrayArry(compressBitmap);
                d = picUtil.getDefinition(compressBitmap);
            } else {
                while (i < 64) {
                    grayArry[grayArry[i]] = 3;
                    i++;
                }
                return grayArry;
            }
        }
        String str = "";
        SharedPreferences.Editor edit = mContext.getSharedPreferences("image_grayArray_list", Context.MODE_PRIVATE).edit();
        while (i < iArr.length) {
            str = str + iArr[i];
            i++;
        }
        edit.putString(filePath, str);
        edit.putString(filePath + "definition", String.valueOf(d));
        edit.commit();
        qVar.setGrayArry(iArr);
        qVar.setDefinition(d);
        return iArr;
    }
}
