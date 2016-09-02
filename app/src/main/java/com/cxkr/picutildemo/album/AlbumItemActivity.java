package com.cxkr.picutildemo.album;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CheckBox;
import android.widget.GridView;
import android.widget.TextView;

import com.cxkr.picutildemo.R;
import com.cxkr.picutildemo.recycle.JunkPictureUtil;

import java.util.ArrayList;
public class AlbumItemActivity extends AppCompatActivity implements OnClickListener {

    private GridView gridView;
    private TextView back, ok;
    private PhotoUpImageBucket photoUpImageBucket;
    private ArrayList<PhotoUpImageItem> selectImages;
    private AlbumItemAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.album_item_images);
        init();
        setListener();
    }

    private void init() {
        gridView = (GridView) findViewById(R.id.album_item_gridv);
        back = (TextView) findViewById(R.id.back);
        ok = (TextView) findViewById(R.id.sure);
        selectImages = new ArrayList<PhotoUpImageItem>();

        Intent intent = getIntent();
        photoUpImageBucket = (PhotoUpImageBucket) intent.getSerializableExtra("imagelist");
        adapter = new AlbumItemAdapter(photoUpImageBucket.getImageList(), AlbumItemActivity.this);
        gridView.setAdapter(adapter);
    }

    private void setListener() {
        back.setOnClickListener(this);
        ok.setOnClickListener(this);
        gridView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                CheckBox checkBox = (CheckBox) view.findViewById(R.id.check);
                photoUpImageBucket.getImageList().get(position).setSelected(!checkBox.isChecked());
                adapter.notifyDataSetChanged();

                if (photoUpImageBucket.getImageList().get(position).isSelected()) {
                    if (selectImages.contains(photoUpImageBucket.getImageList().get(position))) {

                    } else {
                        selectImages.add(photoUpImageBucket.getImageList().get(position));
                    }
                } else {
                    if (selectImages.contains(photoUpImageBucket.getImageList().get(position))) {
                        selectImages.remove(photoUpImageBucket.getImageList().get(position));
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

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back:
                finish();
                break;
            case R.id.sure:
//                Intent intent = new Intent(AlbumItemActivity.this, SelectedImagesActivity.class);
//                intent.putExtra("selectIma", selectImages);
//                startActivity(intent);

                JunkPictureUtil.getInstance(AlbumItemActivity.this).copyFile2Folder(selectImages);
                JunkPictureUtil.getInstance(AlbumItemActivity.this).deleteImage(AlbumItemActivity.this, selectImages);
                adapter.deleteFile(selectImages);
                break;

            default:
                break;
        }
    }

}
