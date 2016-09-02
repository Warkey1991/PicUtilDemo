package com.cxkr.picutildemo;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by songyuanjin on 16/8/25.
 */
public class BitmapBO implements Comparable, Parcelable {
    private String picPath;    //图片路径
    private boolean isChecked; //是否选中
    private String picName;    //图片名称
    private String albunName;  //相册名称
    private long picSize = 0;  //图片大小
    private long picId = -1;   //图片ID
    private long picTime; //图片创建时间
    private Bitmap bitmap;
    private int[] picGrayArray; //图片灰度数组
    private int orientation;    //图片方向
    private double definition;   //
    private boolean bestPic;    //是否是最好的照片
    private int section;

    @Override
    public int compareTo(Object another) {
        return this.picTime - ((BitmapBO) another).picTime > 0 ? 1 : -1;
    }

    public final String getAlbumName() {
        return albunName;
    }

    public final Bitmap getBitmap() {
        return bitmap;
    }

    public final double getDefinition() {
        return definition;
    }

    public final String getFilePath() {
        return picPath;
    }

    public final int[] getGrayArry() {
        return picGrayArray;
    }

    public final long getId() {
        return picId;
    }

    public final int getOrientation() {
        return orientation;
    }

    public final long getSize() {
        return picSize;
    }

    public final long getTime() {
        return picTime;
    }

    public final boolean isBestPicture() {
        return bestPic;
    }

    public final boolean isChecked() {
        return isChecked;
    }

    public final void setAlbumName(String albunName) {
        this.albunName = albunName;
    }

    public final void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    public final void setChecked(boolean isChecked) {
        this.isChecked = isChecked;
    }

    public final void setDefinition(double d) {
        this.definition = d;
    }

    public final void setFilePath(String picPath) {
        this.picPath = picPath;
    }

    public final void setGrayArry(int[] iArr) {
        this.picGrayArray = iArr;
    }

    public final void setId(long picId) {
        this.picId = picId;
    }

    public final void setIsBestPicture(boolean bestPic) {
        this.bestPic = bestPic;
    }

    public final void setName(String picName) {
        this.picName = picName;
    }

    public final void setOrientation(int orientation) {
        this.orientation = orientation;
    }

    public final void setSize(long picSize) {
        this.picSize = picSize;
    }

    public final void setTime(long picTime) {
        this.picTime = picTime;
    }

    public int getSection() {
        return section;
    }

    public void setSection(int section) {
        this.section = section;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
//        dest.writeString(picPath);
//        dest.writeString(picName);
//        dest.writeString(albunName);
//        dest.writeLong(picSize);
//        dest.writeLong(picId);
//        dest.writeLong(picTime);
//        dest.writeIntArray(picGrayArray);
//        dest.writeInt(orientation);
//        dest.writeDouble(definition);
//        dest.writeInt(section);
//        dest.writeByte((byte) (isChecked ? 1 : 0));
//        dest.writeByte((byte) (bestPic ? 1 : 0));
//        if (bitmap != null)
//           bitmap.writeToParcel(dest, 0);
    }

    public static final Parcelable.Creator<BitmapBO> CREATOR = new Parcelable.Creator<BitmapBO>() {
        @Override
        public BitmapBO createFromParcel(Parcel source) {
//            BitmapBO bitmapBO = new BitmapBO();
//            bitmapBO.picPath = source.readString();
//            bitmapBO.picName = source.readString();
//            bitmapBO.picSize = source.readLong();
//            bitmapBO.picId = source.readLong();
//            bitmapBO.picTime = source.readLong();
//            if (bitmapBO.picGrayArray != null) {
//                source.readIntArray(bitmapBO.picGrayArray);
//            }
//            bitmapBO.orientation = source.readInt();
//            bitmapBO.definition = source.readDouble();
//            bitmapBO.section = source.readInt();
//            bitmapBO.isChecked = (source.readByte() != 0);
//            bitmapBO.bestPic = (source.readByte() != 0);
//            bitmapBO.bitmap = Bitmap.CREATOR.createFromParcel(source);
            //return bitmapBO;
            return null;
        }

        @Override
        public BitmapBO[] newArray(int size) {
            return new BitmapBO[0];
        }
    };

    public BitmapBO() {

    }
}

