package com.cxkr.picutildemo.simpic;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Toast;

import com.cxkr.picutildemo.BigPicActivity;
import com.cxkr.picutildemo.BitmapBO;
import com.cxkr.picutildemo.R;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by songyuanjin on 16/8/25.
 */
public class SimpicAdapter extends BaseAdapter {
    private Context mContext;
    private List<SimilarPicBO> similarPicBOs;

    public SimpicAdapter(Context context) {
        this.mContext = context;
        similarPicBOs = new ArrayList<>();
    }

    public void addSimilarPicBOs(SimilarPicBO similarPicBO) {
        similarPicBOs.add(similarPicBO);
    }

    public void addSimilarPicList(List<SimilarPicBO> similarPicBOs) {
        this.similarPicBOs = similarPicBOs;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return similarPicBOs.size();
    }

    @Override
    public Object getItem(int position) {
        return similarPicBOs.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.item_simpic_listview, null);
            holder = new ViewHolder();
            holder.myGridView = (MyGridView) convertView.findViewById(R.id.item_gv_pic);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        final SimilarPicBO similarPicBO = (SimilarPicBO) getItem(position);
        SimPicGridViewAdapter simPicGridViewAdapter = new SimPicGridViewAdapter(mContext, similarPicBO.getSamePics());
        holder.myGridView.setAdapter(simPicGridViewAdapter);
        holder.myGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(mContext, "大图", Toast.LENGTH_SHORT).show();
                BitmapBO bitmapBO = ((SimPicGridViewAdapter.ViewHolder)view.getTag()).bitmapBO;
                Intent intent = new Intent(mContext, BigPicActivity.class);
                intent.putExtra("big_pic_size", bitmapBO.getSize());
                intent.putExtra("big_pic_path", bitmapBO.getFilePath());
                mContext.startActivity(intent);
            }
        });

        simPicGridViewAdapter.setOnSelectedCallBackListener(new SimPicGridViewAdapter.OnSelectedCallBackListener() {
            @Override
            public void onCallBack(BitmapBO bitmapBO) {
                if (callBackListener != null) {
                    callBackListener.onCallBack(bitmapBO);
                }
            }
        });
        return convertView;
    }

    class ViewHolder {
        private MyGridView myGridView;
    }

    private OnSelectedCallBackListener callBackListener;
    public void setOnSelectedCallBackListener(OnSelectedCallBackListener callBackListener) {
        this.callBackListener = callBackListener;
    }
    public interface OnSelectedCallBackListener {
        void onCallBack(BitmapBO bitmapBO);
    }
}
