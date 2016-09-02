package com.cxkr.picutildemo.recycle;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import com.cxkr.picutildemo.BitmapBO;
import com.cxkr.picutildemo.album.PhotoUpImageItem;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by songyuanjin on 16/8/26.
 * 删除图片
 */
public class JunkPictureUtil {
    private final String recyclerPictureFilePath;
    private static JunkPictureUtil junkPictureUtil;
    private Context mContext;
    public static JunkPictureUtil getInstance(Context context) {
        if (junkPictureUtil == null) {
            synchronized (JunkPictureUtil.class) {
                junkPictureUtil = new JunkPictureUtil(context);
            }
        }
        return junkPictureUtil;
    }

    private JunkPictureUtil(Context mContext) {
        this.mContext = mContext;
        recyclerPictureFilePath = Environment.getExternalStorageDirectory().getAbsolutePath() +"/picutildemo/recycler";
    }

    /**
     * 多个图片文件复制到一个目录中
     * @param oldFilePaths
     */
    public void copyFile2Folder(final List<PhotoUpImageItem> oldFilePaths) {
        File file = new File(recyclerPictureFilePath);
        if (!file.exists() && !file.isDirectory()) {
            file.mkdirs();
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                for (PhotoUpImageItem tmpPath : oldFilePaths) {
                    File oldfile = new File(tmpPath.getImagePath());
                    if (!oldfile.exists() || !oldfile.isFile() || !oldfile.canRead()) {
                        continue;
                    }

                    if (oldfile.exists()) {
                        try {
                            int lastNameIndex = tmpPath.getImagePath().lastIndexOf("/");
                            String fileName = tmpPath.getImagePath().substring(lastNameIndex, tmpPath.getImagePath().length());
                            FileInputStream input = new FileInputStream(oldfile);
                            FileOutputStream output = new FileOutputStream(recyclerPictureFilePath + "/"+ fileName + ".dat");
                            byte[] b = new byte[1024 * 5];
                            int len;
                            while ((len = input.read(b)) != -1) {
                                output.write(b, 0, len);
                            }
                            output.flush();
                            output.close();
                            input.close();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    deleteFile(tmpPath.getImagePath());
                }
            }
        }).start();
    }


    public void copyFileFolder(final List<BitmapBO> bitmapBOs) {
        File file = new File(recyclerPictureFilePath);
        if (!file.exists() && !file.isDirectory()) {
            file.mkdirs();
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                for (BitmapBO tmpPath : bitmapBOs) {
                    if (tmpPath.isChecked()) {
                        File oldfile = new File(tmpPath.getFilePath());
                        if (!oldfile.exists() || !oldfile.isFile() || !oldfile.canRead()) {
                            continue;
                        }
                        if (oldfile.exists()) {
                            try {
                                int lastNameIndex = tmpPath.getFilePath().lastIndexOf("/");
                                String fileName = tmpPath.getFilePath().substring(lastNameIndex, tmpPath.getFilePath().length());
                                FileInputStream input = new FileInputStream(oldfile);
                                FileOutputStream output = new FileOutputStream(recyclerPictureFilePath + "/" + fileName + ".dat");
                                byte[] b = new byte[1024 * 5];
                                int len;
                                while ((len = input.read(b)) != -1) {
                                    output.write(b, 0, len);
                                }
                                output.flush();
                                output.close();
                                input.close();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                        deleteFile(tmpPath.getFilePath());
                    }
                }
            }
        }).start();
    }
    /**
     * 删除单个文件
     * @param filePath
     * @return
     */
    public boolean deleteFile(String filePath) {
        File file = new File(filePath);

        if (file.isFile() && file.exists()) {
            return file.delete();
        }
        return false;
    }


    public List<PhotoUpImageItem> getPhotoItems() {
        List<PhotoUpImageItem> photoUpImageItems = new ArrayList<>();

        File file = new File(recyclerPictureFilePath);

        if (file.exists() && file.isDirectory()) {
            File[] files = file.listFiles();

            for (File tmpFile : files) {
                PhotoUpImageItem item = new PhotoUpImageItem();
                item.setImagePath(tmpFile.getAbsolutePath());
                photoUpImageItems.add(item);
            }
        }
        return photoUpImageItems;
    }

    /**
     * 得到回收站图片的大小
     * @return
     */
    public long getRecyclerPicSize() {
        File file = new File(recyclerPictureFilePath);
        if (file.exists()) {
            try {
                return getFileSizes(file);
            } catch (Exception e){

            }

        }
        return 0;
    }


    /**
     * 获取指定文件大小
     *
     * @param
     * @return
     * @throws Exception
     */
    private static long getFileSize(File file) throws Exception {
        long size = 0;
        if (file.exists()) {
            FileInputStream fis = null;
            fis = new FileInputStream(file);
            size = fis.available();
        } else {
            file.createNewFile();
            Log.e("获取文件大小", "文件不存在!");
        }
        return size;
    }
    /**
     * 获取指定文件夹
     *
     * @param f
     * @return
     * @throws Exception
     */
    private static long getFileSizes(File f) throws Exception {
        long size = 0;
        File flist[] = f.listFiles();
        for (int i = 0; i < flist.length; i++) {
            if (flist[i].isDirectory()) {
                size = size + getFileSizes(flist[i]);
            } else {
                size = size + getFileSize(flist[i]);
            }

            System.out.println("Size=====" + size);
        }
        return size;
    }

    /**
     * 删除数据库中的图片
     * @param context
     * @param imageItems
     */
    public void deleteImage(Context context, List<PhotoUpImageItem> imageItems) {
        ContentResolver resolver = context.getContentResolver();
        for (int i=0;i<imageItems.size(); i++) {

            Cursor cursor = MediaStore.Images.Media.query(resolver, MediaStore.Images.Media.EXTERNAL_CONTENT_URI, new String[]{MediaStore.Images.Media._ID}, MediaStore.Images.Media.DATA + "=?",
                    new String[]{imageItems.get(i).getImagePath()}, null);
            boolean result = false;
            if (cursor.moveToFirst()) {
                long id = cursor.getLong(0);
                Uri contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                Uri uri = ContentUris.withAppendedId(contentUri, id);
                int count = context.getContentResolver().delete(uri, null, null);
                result = count == 1;
            }
        }
    }
}
