package com.cxkr.picutildemo.album;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.provider.MediaStore.Audio.Albums;
import android.provider.MediaStore.Images.Media;
import android.provider.MediaStore.Images.Thumbnails;
import android.util.Log;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

public class PhotoUpAlbumHelper extends AsyncTask<Object, Object, Object> {

    final String TAG = getClass().getSimpleName();
    Context context;
    ContentResolver cr;
    // 缩略图列表
    HashMap<String, String> thumbnailList = new HashMap<String, String>();
    // 专辑列表
    List<HashMap<String, String>> albumList = new ArrayList<HashMap<String, String>>();
    HashMap<String, PhotoUpImageBucket> bucketList = new HashMap<String, PhotoUpImageBucket>();
    private GetAlbumList getAlbumList;

    private PhotoUpAlbumHelper() {
    }

    public static PhotoUpAlbumHelper getHelper() {
        PhotoUpAlbumHelper instance = new PhotoUpAlbumHelper();
        return instance;
    }

    /**
     * 初始化
     *
     * @param context
     */
    public void init(Context context) {
        if (this.context == null) {
            this.context = context;
            cr = context.getContentResolver();
        }
    }

    /**
     * 得到缩略图
     */
    private void getThumbnail() {
        String[] projection = {Thumbnails._ID, Thumbnails.IMAGE_ID,
                Thumbnails.DATA};
        Cursor cursor1 = Thumbnails.queryMiniThumbnails(cr, Thumbnails.EXTERNAL_CONTENT_URI,
                Thumbnails.MINI_KIND, projection);
        getThumbnailColumnData(cursor1);
        cursor1.close();
    }

    /**
     * 从数据库中得到缩略图
     *
     * @param cur
     */
    private void getThumbnailColumnData(Cursor cur) {
        if (cur.moveToFirst()) {
            int imageId;
            String imagePath;
            int columnIndex = cur.getColumnIndex(Thumbnails.IMAGE_ID);
            int dataColumn = cur.getColumnIndex(Thumbnails.DATA);
            do {
                imageId = cur.getInt(columnIndex);
                imagePath = cur.getString(dataColumn);
                thumbnailList.put("" + imageId, imagePath);
            } while (cur.moveToNext());
        }
    }

    /**
     * 得到原图-n
     */
    void getAlbum() {
        String[] projection = {Albums._ID, Albums.ALBUM, Albums.ALBUM_ART,
                Albums.ALBUM_KEY, Albums.ARTIST, Albums.NUMBER_OF_SONGS};
        Cursor cursor1 = cr.query(Albums.EXTERNAL_CONTENT_URI, projection, null,
                null, Albums.ALBUM_ID + " desc");
        getAlbumColumnData(cursor1);
        cursor1.close();
    }

    /**
     * 从本地数据库中得到原图
     *
     * @param cur
     */
    private void getAlbumColumnData(Cursor cur) {
        if (cur.moveToFirst()) {
            int _id;
            String album;
            String albumArt;
            String albumKey;
            String artist;
            int numOfSongs;
            int _idColumn = cur.getColumnIndex(Albums._ID);
            int albumColumn = cur.getColumnIndex(Albums.ALBUM);
            int albumArtColumn = cur.getColumnIndex(Albums.ALBUM_ART);
            int albumKeyColumn = cur.getColumnIndex(Albums.ALBUM_KEY);
            int artistColumn = cur.getColumnIndex(Albums.ARTIST);
            int numOfSongsColumn = cur.getColumnIndex(Albums.NUMBER_OF_SONGS);
            do {
                _id = cur.getInt(_idColumn);
                album = cur.getString(albumColumn);
                albumArt = cur.getString(albumArtColumn);
                albumKey = cur.getString(albumKeyColumn);
                artist = cur.getString(artistColumn);
                numOfSongs = cur.getInt(numOfSongsColumn);
                HashMap<String, String> hash = new HashMap<String, String>();
                hash.put("_id", _id + "");
                hash.put("album", album);
                hash.put("albumArt", albumArt);
                hash.put("albumKey", albumKey);
                hash.put("artist", artist);
                hash.put("numOfSongs", numOfSongs + "");
                albumList.add(hash);
            } while (cur.moveToNext());
        }
    }

    /**
     * 是否创建了图片集
     */
    boolean hasBuildImagesBucketList = false;

    /**
     * 得到图片集
     */
    void buildImagesBucketList() {
        // 构造缩略图索引
        getThumbnail();
        // 构造相册索引
        String columns[] = new String[]{Media._ID, Media.BUCKET_ID,
                Media.PICASA_ID, Media.DATA, Media.DISPLAY_NAME, Media.TITLE,
                Media.SIZE, Media.BUCKET_DISPLAY_NAME};
        // 得到一个游标
        Cursor cur = cr.query(Media.EXTERNAL_CONTENT_URI, columns, null, null,
                Media.DATE_MODIFIED + " desc");
        if (cur.moveToFirst()) {
            // 获取指定列的索引
            int photoIDIndex = cur.getColumnIndexOrThrow(Media._ID);
            int photoPathIndex = cur.getColumnIndexOrThrow(Media.DATA);
            int bucketDisplayNameIndex = cur.getColumnIndexOrThrow(Media.BUCKET_DISPLAY_NAME);
            int bucketIdIndex = cur.getColumnIndexOrThrow(Media.BUCKET_ID);
            /**
             * Description:这里增加了一个判断：判断照片的名字是否合法，例如.jpg .png    图片名字是不合法的，直接过滤掉
             */
            do {
                if (cur.getString(photoPathIndex).substring(
                        cur.getString(photoPathIndex).lastIndexOf("/") + 1,
                        cur.getString(photoPathIndex).lastIndexOf("."))
                        .replaceAll(" ", "").length() <= 0) {
                    Log.d(TAG, "出现了异常图片的地址：cur.getString(photoPathIndex)=" + cur.getString(photoPathIndex));
                    Log.d(TAG, "出现了异常图片的地址：cur.getString(photoPathIndex).substring=" + cur.getString(photoPathIndex)
                            .substring(cur.getString(photoPathIndex).lastIndexOf("/") + 1, cur.getString(photoPathIndex).lastIndexOf(".")));
                } else {
                    String _id = cur.getString(photoIDIndex);
                    String path = cur.getString(photoPathIndex);
                    String bucketName = cur.getString(bucketDisplayNameIndex);
                    String bucketId = cur.getString(bucketIdIndex);
                    PhotoUpImageBucket bucket = bucketList.get(bucketId);
                    if (bucket == null) {
                        bucket = new PhotoUpImageBucket();
                        bucketList.put(bucketId, bucket);
                        bucket.imageList = new ArrayList<PhotoUpImageItem>();
                        bucket.bucketName = bucketName;
                    }
                    bucket.count++;
                    PhotoUpImageItem imageItem = new PhotoUpImageItem();
                    imageItem.setImageId(_id);
                    imageItem.setImagePath(path);
//					imageItem.setThumbnailPath(thumbnailList.get(_id));
                    bucket.imageList.add(imageItem);
                    Log.i(TAG, "PhotoUpAlbumHelper类中 的——》path=" + thumbnailList.get(_id));
                }
            } while (cur.moveToNext());
        }
        cur.close();
        hasBuildImagesBucketList = true;
    }

    /**
     * 得到图片集
     *
     * @param refresh
     * @return
     */
    public List<PhotoUpImageBucket> getImagesBucketList(boolean refresh) {
        if (refresh || (!refresh && !hasBuildImagesBucketList)) {
            buildImagesBucketList();
        }
        List<PhotoUpImageBucket> tmpList = new ArrayList<PhotoUpImageBucket>();
        Iterator<Entry<String, PhotoUpImageBucket>> itr = bucketList.entrySet().iterator();
        while (itr.hasNext()) {
            Entry<String, PhotoUpImageBucket> entry = (Entry<String, PhotoUpImageBucket>) itr.next();
            tmpList.add(entry.getValue());
        }
        return tmpList;
    }

    /**
     * 得到原始图像路径
     *
     * @param image_id
     * @return
     */
    String getOriginalImagePath(String image_id) {
        String path = null;
        Log.i(TAG, "---(^o^)----" + image_id);
        String[] projection = {Media._ID, Media.DATA};
        Cursor cursor = cr.query(Media.EXTERNAL_CONTENT_URI, projection,
                Media._ID + "=" + image_id, null, Media.DATE_MODIFIED + " desc");
        if (cursor != null) {
            cursor.moveToFirst();
            path = cursor.getString(cursor.getColumnIndex(Media.DATA));
        }
        return path;
    }

    public void destoryList() {
        thumbnailList.clear();
        thumbnailList = null;
        albumList.clear();
        albumList = null;
        bucketList.clear();
        bucketList = null;
    }

    public void setGetAlbumList(GetAlbumList getAlbumList) {
        this.getAlbumList = getAlbumList;
    }

    public interface GetAlbumList {
        public void getAlbumList(List<PhotoUpImageBucket> list);
    }

    @Override
    protected Object doInBackground(Object... params) {
        return getImagesBucketList((Boolean) (params[0]));
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void onPostExecute(Object result) {
        super.onPostExecute(result);
        getAlbumList.getAlbumList((List<PhotoUpImageBucket>) result);
    }

}
