package com.cxkr.picutildemo.album;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import com.cxkr.picutildemo.R;
import java.util.List;

public class AlbumsActivity extends AppCompatActivity {
	private GridView gridView;
	private AlbumsAdapter adapter;
	private PhotoUpAlbumHelper photoUpAlbumHelper;
	private List<PhotoUpImageBucket> list;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.albums_gridview);
		init();
		loadData();
		onItemClick();
	}
	private void init(){
		gridView = (GridView) findViewById(R.id.album_gridv);
		adapter = new AlbumsAdapter(AlbumsActivity.this);
		gridView.setAdapter(adapter);
	}
	
	private void loadData(){
		photoUpAlbumHelper = PhotoUpAlbumHelper.getHelper();
		photoUpAlbumHelper.init(AlbumsActivity.this);
		photoUpAlbumHelper.setGetAlbumList(new PhotoUpAlbumHelper.GetAlbumList() {
			@Override
			public void getAlbumList(List<PhotoUpImageBucket> list) {
				adapter.setArrayList(list);
				adapter.notifyDataSetChanged();
				AlbumsActivity.this.list = list;
			}
		});
		photoUpAlbumHelper.execute(true);
	}
	
	private void onItemClick(){
		gridView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Intent intent = new Intent(AlbumsActivity.this, AlbumItemActivity.class);
				intent.putExtra("imagelist", list.get(position));
				startActivity(intent);
			}
		});
	}
	@Override
	protected void onDestroy() {
		super.onDestroy();
	}
}
