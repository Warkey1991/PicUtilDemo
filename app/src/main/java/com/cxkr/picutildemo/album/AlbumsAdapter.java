package com.cxkr.picutildemo.album;

import android.content.Context;
import android.graphics.Bitmap.Config;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.cxkr.picutildemo.R;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;

import java.util.ArrayList;
import java.util.List;

public class AlbumsAdapter extends BaseAdapter {
    private List<PhotoUpImageBucket> arrayList;
    private LayoutInflater layoutInflater;

    private ImageLoader imageLoader = ImageLoader.getInstance();
    private DisplayImageOptions options;
    private String TAG = AlbumsAdapter.class.getSimpleName();

    public AlbumsAdapter(Context context) {
        layoutInflater = LayoutInflater.from(context);
        arrayList = new ArrayList<PhotoUpImageBucket>();//初始化集合
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(context)
                .threadPriority(Thread.NORM_PRIORITY - 2)
                .denyCacheImageMultipleSizesInMemory()
                .discCacheFileNameGenerator(new Md5FileNameGenerator())
                .tasksProcessingOrder(QueueProcessingType.LIFO)
                .memoryCacheExtraOptions(96, 120)
                .build();
        // Initialize ImageLoader with configuration.
        imageLoader.init(config);

        // 使用DisplayImageOption.Builder()创建DisplayImageOptions
        options = new DisplayImageOptions.Builder()
                .showStubImage(R.drawable.album_default_loading_pic) // 设置图片下载期间显示的图片
                .showImageForEmptyUri(R.drawable.album_default_loading_pic) // 设置图片Uri为空或是错误的时候显示的图片
                .showImageOnFail(R.drawable.album_default_loading_pic) // 设置图片加载或解码过程中发生错误显示的图片
                .cacheInMemory(true) // 设置下载的图片是否缓存在内存中
                .cacheOnDisc(true) // 设置下载的图片是否缓存在SD卡中
                // .displayer(new RoundedBitmapDisplayer(20)) // 设置成圆角图片
                .bitmapConfig(Config.RGB_565)
                .imageScaleType(ImageScaleType.IN_SAMPLE_INT)
                .build(); // 创建配置过的DisplayImageOption对象

    }

    ;

    @Override
    public int getCount() {
        return arrayList.size();
    }

    @Override
    public Object getItem(int position) {
        return arrayList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Holder holder;
        if (convertView == null) {
            holder = new Holder();
            convertView = layoutInflater.inflate(R.layout.ablums_adapter_item, parent, false);
            holder.image = (ImageView) convertView.findViewById(R.id.image);
            holder.name = (TextView) convertView.findViewById(R.id.name);
            holder.count = (TextView) convertView.findViewById(R.id.count);
            convertView.setTag(holder);
        } else {
            holder = (Holder) convertView.getTag();
        }
        holder.count.setText("" + arrayList.get(position).getCount());
        holder.name.setText(arrayList.get(position).getBucketName());

        imageLoader.displayImage("file://" + arrayList.get(position).getImageList().get(0).getImagePath(),
                holder.image, options);
        return convertView;
    }

    class Holder {
        ImageView image;
        TextView name;
        TextView count;
    }

    public void setArrayList(List<PhotoUpImageBucket> arrayList) {
        this.arrayList = arrayList;
    }
}
