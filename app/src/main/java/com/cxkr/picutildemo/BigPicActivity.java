package com.cxkr.picutildemo;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.DecimalFormat;

/**
 * Created by songyuanjin on 16/8/31.
 */
public class BigPicActivity extends AppCompatActivity{
    private ImageView ivBigPic;
    private String picPath;
    private TextView tvSize;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_big_pic);
        tvSize = (TextView) findViewById(R.id.tv_size);
        ivBigPic = (ImageView) findViewById(R.id.iv_big_pic);
        picPath = getIntent().getStringExtra("big_pic_path");

        ImageUtil.getInstatnce(BigPicActivity.this).disPlay(picPath, ivBigPic);

        float selectedSize = getIntent().getLongExtra("big_pic_size", 0);
        float selectedSizeM = selectedSize / 1024 / 1024;
        DecimalFormat decimalFormat = new DecimalFormat("0.00");//构造方法的字符格式这里如果小数不足2位,会以0补足.
        String p = decimalFormat.format(selectedSizeM);//format 返回的是字符串
        tvSize.setText(p + "M");

    }
}
