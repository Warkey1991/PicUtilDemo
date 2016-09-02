package com.cxkr.picutildemo.recycle;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.GridView;

import com.cxkr.picutildemo.R;
import com.cxkr.picutildemo.album.PhotoUpImageItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by songyuanjin on 16/8/26.
 */
public class RecyclerPictureActivity extends AppCompatActivity {
    private GridView gridView;
    private List<PhotoUpImageItem> photoUpImageBucket;
    private ArrayList<PhotoUpImageItem> selectImages;
    private RecyclerPicAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recycler_picture);
        init();
        setListener();
    }

    private void init() {
        gridView = (GridView) findViewById(R.id.album_item_gridv);
        selectImages = new ArrayList<PhotoUpImageItem>();

        //Intent intent = getIntent();
        //photoUpImageBucket = (PhotoUpImageBucket) intent.getSerializableExtra("imagelist");

        photoUpImageBucket = JunkPictureUtil.getInstance(RecyclerPictureActivity.this).getPhotoItems();
        adapter = new RecyclerPicAdapter(photoUpImageBucket, RecyclerPictureActivity.this);
        gridView.setAdapter(adapter);
    }

    private void setListener() {
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                CheckBox checkBox = (CheckBox) view.findViewById(R.id.check);
                photoUpImageBucket.get(position).setSelected(!checkBox.isChecked());
                adapter.notifyDataSetChanged();

                if (photoUpImageBucket.get(position).isSelected()) {
                    if (selectImages.contains(photoUpImageBucket.get(position))) {

                    } else {
                        selectImages.add(photoUpImageBucket.get(position));
                    }
                } else {
                    if (selectImages.contains(photoUpImageBucket.get(position))) {
                        selectImages.remove(photoUpImageBucket.get(position));
                    } else {

                    }
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }


}
