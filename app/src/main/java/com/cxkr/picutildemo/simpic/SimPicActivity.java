package com.cxkr.picutildemo.simpic;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.AbsListView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.cxkr.picutildemo.BitmapBO;
import com.cxkr.picutildemo.R;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by songyuanjin on 16/8/25.
 */
public class SimPicActivity extends AppCompatActivity implements AbsListView.OnScrollListener {
    private ListView mListView;
    private RelativeLayout rlBest;
    private LinearLayout llDelete;
    private View headerView;
    private int aHeight;
    private SimpicAdapter simpicAdapter;
    private TextView tvAllSize;
    private TextView tvPath;
    private TextView tvSelectedSize;

    private List<BitmapBO> allSimilarBOs = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_simpic);

        mListView = (ListView) findViewById(R.id.lv_similar_pic);
        rlBest = (RelativeLayout) findViewById(R.id.rl_best);
        llDelete = (LinearLayout) findViewById(R.id.ll_delete);
        headerView = LayoutInflater.from(SimPicActivity.this).inflate(R.layout.layout_similar_header, null);
        tvAllSize = (TextView) headerView.findViewById(R.id.tv_size);
        tvPath = (TextView) headerView.findViewById(R.id.tv_search_path);
        tvSelectedSize = (TextView) findViewById(R.id.tv_delete_size);

        mListView.addHeaderView(headerView);

        simpicAdapter = new SimpicAdapter(SimPicActivity.this);

        mListView.setAdapter(simpicAdapter);
        mListView.setOnScrollListener(this);

        ViewTreeObserver vto = rlBest.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                rlBest.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                aHeight = rlBest.getHeight();
            }
        });

        initData();

        JunkSimUtil.getInstance(SimPicActivity.this).addListener(new JunkSimUtil.Callback() {
            @Override
            public void onSearchingPath(String path) {
                tvPath.setText(path);
            }

            @Override
            public void onUpdate(List<SimilarPicBO> samePicBOs) {
                simpicAdapter.addSimilarPicList(samePicBOs);
                setTextSize(samePicBOs);
            }

            @Override
            public void onFinish() {
                if (JunkSimUtil.getInstance(SimPicActivity.this).isSearchFinish()) {
                    int simPicCount = 0;
                    List<SimilarPicBO> similarPicBOs = JunkSimUtil.getInstance(SimPicActivity.this).getSimilarPicBOs();
                    for (SimilarPicBO similarPicBO : similarPicBOs) {
                        simPicCount += similarPicBO.getSamePics().size();
                    }
                    tvPath.setText("共发现" + simPicCount + "张相似图片");

                    initAllSimilarBOs();
                    setDeletedDataViews();
                }
            }
        });

        simpicAdapter.setOnSelectedCallBackListener(new SimpicAdapter.OnSelectedCallBackListener() {
            @Override
            public void onCallBack(BitmapBO bitmapBO) {
                for (BitmapBO bo : allSimilarBOs) {
                    if (bo != null && bo.getFilePath().equals(bitmapBO.getFilePath())) {
                        bo.setChecked(bitmapBO.isChecked());
                        break;
                    }
                }
                setDeletedDataViews();
            }
        });

        //删除选中的图片并同步到页面上
        llDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<SimilarPicBO> similarPicBOs = JunkSimUtil.getInstance(SimPicActivity.this).getSimilarPicBOs();
                //JunkPictureUtil.getInstance(SimPicActivity.this).copyFileFolder(allSimilarBOs);
                List<SimilarPicBO> tmpSimilarPicBos = new ArrayList<SimilarPicBO>();  //保存删除的一组相似图片
                for (SimilarPicBO similarPicBO : similarPicBOs) {
                    List<BitmapBO> tmpBitmapBo = similarPicBO.getSamePics();

                    //查找一组相似图片中选中的图片
                    List<BitmapBO> selectedBitmap = new ArrayList<BitmapBO>();
                    for (BitmapBO bo : tmpBitmapBo) {
                        if (bo.isChecked()) {
                            selectedBitmap.add(bo);
                        }
                    }
                    //删除一组相似图片中选中的照片
                    tmpBitmapBo.removeAll(selectedBitmap);

                    if (tmpBitmapBo.size() == 0) {  //找出一组均删除的相似图片
                        tmpSimilarPicBos.add(similarPicBO);
                    }
                }
                //若一组的相似图片都删除,则移除
                similarPicBOs.removeAll(tmpSimilarPicBos);

                simpicAdapter.notifyDataSetChanged();

                setTextSize(similarPicBOs);
            }
        });
    }

    private void initData() {
        List<SimilarPicBO> similarPicBOs = JunkSimUtil.getInstance(SimPicActivity.this).getSimilarPicBOs();
        simpicAdapter.addSimilarPicList(similarPicBOs);
        setTextSize(similarPicBOs);
    }

    /**
     * 显示图片数据到view
     * @param similarPicBOs
     */
    private void setTextSize(List<SimilarPicBO> similarPicBOs) {
        int simPicCount = 0;
        long simPicSize = 0;
        for (SimilarPicBO similarPicBO : similarPicBOs) {
            simPicCount += similarPicBO.getSamePics().size(); //计算相似照片的数量
            simPicSize += similarPicBO.getSimilarPicSize();   //计算相似照片的总大小
            float simPicSizeM = simPicSize / 1024 / 1024;
            DecimalFormat decimalFormat = new DecimalFormat("0.00");//构造方法的字符格式这里如果小数不足2位,会以0补足.
            String p = decimalFormat.format(simPicSizeM);//format 返回的是字符串
            tvAllSize.setText(p + "M");
        }
        if (JunkSimUtil.getInstance(SimPicActivity.this).isSearchFinish()) {
            tvPath.setText("共发现" + simPicCount + "张相似图片");
            initAllSimilarBOs();
            saveBest();
            setDeletedDataViews();
        }
    }

    /**
     * 初始化所有图片的集合
     */
    private void initAllSimilarBOs() {
        llDelete.setVisibility(View.VISIBLE);
        if (allSimilarBOs != null) {
            allSimilarBOs.clear();
        }
        List<SimilarPicBO> similarPicBOs = JunkSimUtil.getInstance(SimPicActivity.this).getSimilarPicBOs();
        for (SimilarPicBO similarPicBO : similarPicBOs) {
            allSimilarBOs.addAll(similarPicBO.getSamePics());
        }
    }

    /**
     * 选中删除的图片大小
     */
    private void setDeletedDataViews() {
        int selectedSize = 0;
        for (BitmapBO bitmapBO : allSimilarBOs) {
            if (bitmapBO.isChecked()) {
                selectedSize += bitmapBO.getSize();
            }
        }

        float selectedSizeM = selectedSize / 1024 / 1024;
        DecimalFormat decimalFormat = new DecimalFormat("0.00");//构造方法的字符格式这里如果小数不足2位,会以0补足.
        String p = decimalFormat.format(selectedSizeM);//format 返回的是字符串
        tvSelectedSize.setText(p + "M");
    }

    /**
     * 保留最佳图片
     */
    private void saveBest() {
        for (BitmapBO bo : allSimilarBOs) {
            if (bo.isBestPicture()) {
                bo.setChecked(false);
            }
        }
        simpicAdapter.notifyDataSetChanged();
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        if (getScrollY() >= aHeight) {
            if (rlBest.getVisibility() == View.INVISIBLE) {
                rlBest.setVisibility(View.VISIBLE);
            }
        } else if (getScrollY() < aHeight) {
            if (rlBest.getVisibility() == View.VISIBLE) {
                rlBest.setVisibility(View.INVISIBLE);
            }
        }
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {

    }

    //获取滚动距离
    public int getScrollY() {
        View c = mListView.getChildAt(0);
        if (c == null) {
            return 0;
        }
        int firstVisiblePosition = mListView.getFirstVisiblePosition();
        int top = c.getTop();
        int headerHeight = 0;
        if (firstVisiblePosition >= 1) {
            headerHeight = mListView.getHeight();
        }
        return -top + firstVisiblePosition * c.getHeight() + headerHeight;
    }

    private void onFinishActivity() {
        Intent intent = new Intent();
        setResult(1000, intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        onFinishActivity();
    }

}
