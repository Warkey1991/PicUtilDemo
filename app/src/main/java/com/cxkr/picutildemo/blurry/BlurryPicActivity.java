package com.cxkr.picutildemo.blurry;

import android.app.ProgressDialog;
import android.database.Cursor;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

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
 * Created by songyuanjin on 16/8/19.
 */
public class BlurryPicActivity extends AppCompatActivity {
    public static final Uri PIC_URI = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

    private List<BitmapBO> picBOs;
    private List<SimilarPicBO> samePicBOs;
    private ListView lvPic;
    private SimpicAdapter simPicAdapter;
    private TextView textView;

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
        simPicAdapter = new SimpicAdapter(BlurryPicActivity.this);
        lvPic.setAdapter(simPicAdapter);
        queryPic();
    }

    private void queryPic() {
        String[] strArr = new String[]{"_id", "_data", "_size", "date_modified", "mime_type", "media_type", "datetaken", "date_added"};
        Cursor query = getContentResolver().query(MediaStore.Files.getContentUri("external"), strArr, "(media_type=1 or media_type=3) and format!=12289 and _size > 0", null, "date_modified DESC");

        //Cursor query = getContentResolver().query(PIC_URI, null, "mime_type=? or mime_type=?", new String[]{"image/jpeg", "image/png"}, "datetaken");

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

        //getBlurry();
        task = new BlurryTask();
        task.execute();
    }

    BlurryTask task;

    private void getBlurry() {
        List<BitmapBO> tmpBos = new ArrayList<>();
        SimilarPicBO samePicBO = new SimilarPicBO();
        for (BitmapBO picBO : picBOs) {
            double value = BlurryPicUtil.blurryValue(picBO.getId(), picBO.getFilePath());

            System.out.println("value==========" + value);
            if (value <= 50) {
                if (tmpBos.size() == 0) {
                    tmpBos.add(picBO);
                    samePicBO.setTimeName(picBO.getTime() + "");
                    samePicBO.setSamePics(tmpBos);
                    samePicBOs.add(samePicBO);
                } else {
                    if (getDate(samePicBO.getTimeName()) == getDate(picBO.getTime() + "")) {
                        tmpBos.add(picBO);
                        samePicBO.setSamePics(tmpBos);
                        samePicBOs.remove(samePicBO);
                        samePicBOs.add(samePicBO);
                    } else {
                        tmpBos = new ArrayList<>();
                        tmpBos.add(picBO);
                        samePicBO = new SimilarPicBO();
                        samePicBO.setTimeName(picBO.getTime() + "");
                        samePicBO.setSamePics(tmpBos);
                        samePicBOs.add(samePicBO);
                    }
                }

                System.out.println("picDataPicDatatatatatatat" + picBO.getFilePath());
            }
        }
    }

    long time1;
    long time2;
    ProgressDialog progressDialog;

    private class BlurryTask extends AsyncTask<Void, Integer, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            time1 = System.currentTimeMillis();
            //创建ProgressDialog对象
            progressDialog = new ProgressDialog(
                    BlurryPicActivity.this);
            //设置进度条风格，风格为圆形，旋转的
            progressDialog.setProgressStyle(
                    ProgressDialog.STYLE_SPINNER);
            //设置ProgressDialog 标题
            progressDialog.setTitle("下载");
            //设置ProgressDialog 提示信息
            progressDialog.setMessage("这是一个圆形进度条对话框");
            //设置ProgressDialog 标题图标
            progressDialog.setIcon(android.R.drawable.btn_star);
            //设置ProgressDialog 的进度条是否不明确
            progressDialog.setIndeterminate(false);
            //设置ProgressDialog 是否可以按退回按键取消
            progressDialog.setCancelable(true);
            progressDialog.setCanceledOnTouchOutside(false);

            // 让ProgressDialog显示
           // progressDialog.show();
        }

        @Override
        protected Void doInBackground(Void... params) {
            List<BitmapBO> tmpBos = new ArrayList<>();
            SimilarPicBO samePicBO = new SimilarPicBO();
            int size = 0;
            for (BitmapBO picBO : picBOs) {
                size++;
                double value = BlurryPicUtil.blurryValue(picBO.getId(), picBO.getFilePath());
                if (size % 2 == 0) {
                    publishProgress(size);
                }
                System.out.println("value==========" + value);
                if (value <= 50) {
                    if (tmpBos.size() == 0) {
                        tmpBos.add(picBO);
                        samePicBO.setTimeName(picBO.getTime() + "");
                        samePicBO.setSamePics(tmpBos);
                        samePicBOs.add(samePicBO);
                    } else {
                        if (getDate(samePicBO.getTimeName()) == getDate(picBO.getTime() + "")) {
                            tmpBos.add(picBO);
                            samePicBO.setSamePics(tmpBos);
                            samePicBOs.remove(samePicBO);
                            samePicBOs.add(samePicBO);
                        } else {
                            tmpBos = new ArrayList<>();
                            tmpBos.add(picBO);
                            samePicBO = new SimilarPicBO();
                            samePicBO.setTimeName(picBO.getTime() + "");
                            samePicBO.setSamePics(tmpBos);
                            samePicBOs.add(samePicBO);
                        }
                    }

                    System.out.println("picDataPicDatatatatatatat" + picBO.getFilePath());
                }
            }

            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            textView.setText(values[0] + "/" + picBOs.size());
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            simPicAdapter.addSimilarPicList(samePicBOs);

            progressDialog.dismiss();
            time2 = System.currentTimeMillis();

            Toast.makeText(BlurryPicActivity.this, "time =" + (time2 - time1) / 1000, Toast.LENGTH_LONG).show();
        }
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
