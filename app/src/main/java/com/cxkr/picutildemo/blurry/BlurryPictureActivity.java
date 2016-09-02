package com.cxkr.picutildemo.blurry;

import android.database.Cursor;
import android.media.ExifInterface;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.ListView;
import android.widget.TextView;

import com.cxkr.picutildemo.BitmapBO;
import com.cxkr.picutildemo.R;
import com.cxkr.picutildemo.simpic.SimilarPicBO;
import com.cxkr.picutildemo.simpic.SimpicAdapter;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by songyuanjin on 16/9/2.
 */
public class BlurryPictureActivity extends AppCompatActivity {

    private List<BitmapBO> picBOs;
    private List<SimilarPicBO> samePicBOs;
    private ListView lvPic;
    private SimpicAdapter simPicAdapter;
    private TextView textView;

    private BlurryPictureUtil blurryPictureUtil;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blurry_pic);
        lvPic = (ListView) findViewById(R.id.lv_pic);
        textView = (TextView) findViewById(R.id.tv_progress);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        picBOs = new ArrayList();
        samePicBOs = new ArrayList<>();
        simPicAdapter = new SimpicAdapter(BlurryPictureActivity.this);
        lvPic.setAdapter(simPicAdapter);

        blurryPictureUtil = BlurryPictureUtil.getInstance(BlurryPictureActivity.this);
//        queryPic();

        calculateBlurryPic();
    }

    private void queryPic() {
        String[] strArr = new String[]{"_id", "_data", "_size", "date_modified", "mime_type", "media_type", "datetaken", "date_added"};
        Cursor query = getContentResolver().query(MediaStore.Files.getContentUri("external"), strArr, "(media_type=1 or media_type=3) and format!=12289 and _size > 0", null, "date_modified DESC");

        if (query == null) {
            //picSimilarActivity.l.sendEmptyMessage(3);
            return;
        }

        while (query.moveToNext()) {
            String picData = query.getString(query.getColumnIndex("_data"));
            long picSize = (long) query.getInt(query.getColumnIndex("_size"));
            long picID = query.getLong(query.getColumnIndex("_id"));
            long strTime = getStrTime(picData);

            if (!(0 == strTime || picData == null)) {
                BitmapBO picBO = new BitmapBO();
                picBO.setSize(picSize);
                picBO.setTime(strTime);
                picBO.setFilePath(picData);
                picBO.setId(picID);
                picBO.setChecked(true);
                picBOs.add(picBO);
            }
        }

        query.close();

        Collections.sort(picBOs, new Comparator() {
            @Override
            public int compare(Object lhs, Object rhs) {
                return ((BitmapBO) lhs).getTime() - ((BitmapBO) rhs).getTime() <= 0 ? -1 : 1;
            }
        });


    }

    List<BitmapBO> tmpBos = new ArrayList<>();
    SimilarPicBO samePicBO = new SimilarPicBO();

    int count = 0;

    public void calculateBlurryPic() {
        blurryPictureUtil.addOnCalculateBlurryValueListener(new BlurryPictureUtil.OnCalculateBlurryValueListener() {
            @Override
            public void onUpdate(BitmapBO bitmapBO, double value) {
                count++;
                if (tmpBos.size() == 0) {
                    tmpBos.add(bitmapBO);
                    samePicBO.setTimeName(bitmapBO.getTime() + "");
                    samePicBO.setSamePics(tmpBos);
                    samePicBOs.add(samePicBO);
                } else {
                    if (getDate(samePicBO.getTimeName()) == getDate(bitmapBO.getTime() + "")) {
                        tmpBos.add(bitmapBO);
                        samePicBO.setSamePics(tmpBos);
                        samePicBOs.remove(samePicBO);
                        samePicBOs.add(samePicBO);
                    } else {
                        tmpBos = new ArrayList<>();
                        tmpBos.add(bitmapBO);
                        samePicBO = new SimilarPicBO();
                        samePicBO.setTimeName(bitmapBO.getTime() + "");
                        samePicBO.setSamePics(tmpBos);
                        samePicBOs.add(samePicBO);
                    }
                }
                textView.setText(count + "/" + picBOs.size());
            }

            @Override
            public void onSearchFilePath(String filePath) {

            }

            @Override
            public void onSearchFinish() {
                simPicAdapter.addSimilarPicList(samePicBOs);
            }
        });

        //获取上个页面计算好的数据
        List<BitmapBO> bitmapBOList = blurryPictureUtil.getBlurryBOS();
        for (BitmapBO bitmapBO : bitmapBOList) {
            if (tmpBos.size() == 0) {
                tmpBos.add(bitmapBO);
                samePicBO.setTimeName(bitmapBO.getTime() + "");
                samePicBO.setSamePics(tmpBos);
                samePicBOs.add(samePicBO);
            } else {
                if (getDate(samePicBO.getTimeName()) == getDate(bitmapBO.getTime() + "")) {
                    tmpBos.add(bitmapBO);
                    samePicBO.setSamePics(tmpBos);
                    samePicBOs.remove(samePicBO);
                    samePicBOs.add(samePicBO);
                } else {
                    tmpBos = new ArrayList<>();
                    tmpBos.add(bitmapBO);
                    samePicBO = new SimilarPicBO();
                    samePicBO.setTimeName(bitmapBO.getTime() + "");
                    samePicBO.setSamePics(tmpBos);
                    samePicBOs.add(samePicBO);
                }
            }
        }
        simPicAdapter.addSimilarPicList(samePicBOs);
        //blurryPictureUtil.setCalculatePictureCount(picBOs.size());
        //blurryPictureUtil.calculateBlurryValue(picBOs);
    }

    public static long getStrTime(String str) {
        try {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss");
            String attribute = new ExifInterface(str).getAttribute("DateTime");
            if (attribute != null) {
                return simpleDateFormat.parse(attribute).getTime();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e2) {
            e2.printStackTrace();
        }
        return 0;
    }


    public static long getDate(String str) {
        try {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy:MM:dd");
            String attribute = new ExifInterface(str).getAttribute("DateTime");
            if (attribute != null) {
                return simpleDateFormat.parse(attribute).getTime();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e2) {
            e2.printStackTrace();
        }
        return 0;
    }
}
