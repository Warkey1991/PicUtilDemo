package com.cxkr.picutildemo;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by songyuanjin on 16/8/25.
 */
public class GridViewAdapter extends BaseAdapter {
    private Context mContext;
    private LayoutInflater inflater;
    private List<BitmapBO> bitmapBOs;

    public GridViewAdapter(Context context) {
        this.mContext = context;
        inflater = LayoutInflater.from(mContext);
        bitmapBOs = new ArrayList<>();
    }

    public void resetItems() {
        if (bitmapBOs != null) {
            bitmapBOs.clear();
        }
        notifyDataSetChanged();
    }
    public void addItems(List<BitmapBO> bitmaps) {
        this.bitmapBOs.addAll(bitmaps);
        notifyDataSetChanged();
    }

    public void addItem(BitmapBO bitmapBO) {
        this.bitmapBOs.add(bitmapBO);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        if (bitmapBOs == null) {
            return 0;
        }
        if (bitmapBOs.size() > 4) {
            return 4;
        }
        return bitmapBOs.size();
    }

    @Override
    public Object getItem(int position) {
        return bitmapBOs == null ? null : bitmapBOs.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageViewHolder holder = null;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.grid_item, parent, false);
            holder = new ImageViewHolder();
            holder.ivPic = (MyImageView) convertView.findViewById(R.id.grid_item);
            convertView.setTag(holder);
        } else {
            holder = (ImageViewHolder) convertView.getTag();
        }
        final BitmapBO bitmapBO = (BitmapBO) getItem(position);
        ImageUtil.getInstatnce(mContext).disPlay(bitmapBO.getFilePath(), holder.ivPic);
        return convertView;
    }

    public class ImageViewHolder {
        ImageView ivPic;
    }
}
