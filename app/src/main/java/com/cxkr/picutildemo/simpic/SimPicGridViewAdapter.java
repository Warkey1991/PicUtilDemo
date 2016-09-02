package com.cxkr.picutildemo.simpic;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;

import com.cxkr.picutildemo.BitmapBO;
import com.cxkr.picutildemo.ImageUtil;
import com.cxkr.picutildemo.R;

import java.util.List;

/**
 * Created by songyuanjin on 16/8/26.
 */
public class SimPicGridViewAdapter extends BaseAdapter {
    private Context mContext;
    private LayoutInflater inflater;
    private List<BitmapBO> bitmapBOList;

    public SimPicGridViewAdapter(Context mContext, List<BitmapBO> bitmapBOList) {
        this.mContext = mContext;
        inflater = LayoutInflater.from(mContext);
        this.bitmapBOList = bitmapBOList;
    }

    public void setBitmapBOList(List<BitmapBO> bitmapBOList) {
        this.bitmapBOList = bitmapBOList;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return bitmapBOList == null ? 0 : bitmapBOList.size();
    }

    @Override
    public Object getItem(int position) {
        return bitmapBOList == null ? null : bitmapBOList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_simpic_girdview, parent, false);
            holder = new ViewHolder();
            holder.ivSimPic = (ImageView) convertView.findViewById(R.id.iv_sim_pic);
            holder.ivBest = (ImageView) convertView.findViewById(R.id.iv_sim_best);
            holder.cbPic = (CheckBox) convertView.findViewById(R.id.cb_pic);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        final BitmapBO bitmapBO = (BitmapBO) getItem(position);
        holder.bitmapBO = bitmapBO;

        ImageUtil.getInstatnce(mContext).disPlay(bitmapBO.getFilePath(), holder.ivSimPic);

        if (bitmapBO.isBestPicture()) {
            holder.ivBest.setVisibility(View.VISIBLE);
        } else {
            holder.ivBest.setVisibility(View.GONE);
        }
        holder.cbPic.setChecked(bitmapBO.isChecked());

        holder.cbPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               if (callBackListener != null) {
                   bitmapBO.setChecked(!bitmapBO.isChecked());
                   callBackListener.onCallBack(bitmapBO);
               }
            }
        });

        return convertView;
    }


    class ViewHolder {
        public BitmapBO bitmapBO;
        private ImageView ivSimPic;
        private ImageView ivBest;
        private CheckBox cbPic;
    }

    private OnSelectedCallBackListener callBackListener;
    public void setOnSelectedCallBackListener(OnSelectedCallBackListener callBackListener) {
        this.callBackListener = callBackListener;
    }
    public interface OnSelectedCallBackListener {
        void onCallBack(BitmapBO bitmapBO);
    }
}
