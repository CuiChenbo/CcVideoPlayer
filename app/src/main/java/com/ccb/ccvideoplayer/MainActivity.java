package com.ccb.ccvideoplayer;
import www.ccb.com.common.base.BaseActivity;

import android.Manifest;
import android.animation.ValueAnimator;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.RotateDrawable;
import android.graphics.drawable.ScaleDrawable;
import android.os.Build;

import android.widget.FrameLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.ccb.ccvideoplayer.fragment.HomeFragment;
import com.ccb.ccvideoplayer.fragment.ListPlayFragment;
import com.ccb.ccvideoplayer.fragment.MyFragment;
import com.ccb.ccvideoplayer.fragment.SpcFragment;


public class MainActivity extends BaseActivity {

    private FrameLayout fl;
    private RadioGroup rg;
    private Fragment HomeFm = null,SpcFm = null, OrderFm = null, MyFm = null;

    private final class RadioGroupOnCheckedChangeListener implements RadioGroup.OnCheckedChangeListener {
        @Override
        public void onCheckedChanged(RadioGroup radioGroup, int checkedId) {
            if (HomeFm != null) {
                getSupportFragmentManager().beginTransaction().hide(HomeFm).commit();
            }
            if (SpcFm != null) {
                getSupportFragmentManager().beginTransaction().hide(SpcFm).commit();
            }
            if (OrderFm != null) {
                getSupportFragmentManager().beginTransaction().hide(OrderFm).commit();
            }
            if (MyFm != null) {
                getSupportFragmentManager().beginTransaction().hide(MyFm).commit();
            }
            switch (checkedId) {
                case R.id.rb_home:
                    if (HomeFm == null) {
                        HomeFm = new HomeFragment();
                        getSupportFragmentManager().beginTransaction().add(R.id.fl, HomeFm).commit();
                    } else {
                        getSupportFragmentManager().beginTransaction().show(HomeFm).commit();
                    }
                    break;
                case R.id.rb_shoppingcart:
                    if (SpcFm == null) {
                        SpcFm = new SpcFragment();
                        getSupportFragmentManager().beginTransaction().add(R.id.fl, SpcFm).commit();
                    } else {
                        getSupportFragmentManager().beginTransaction().show(SpcFm).commit();
                    }
                    break;

                case R.id.rb_orderfrom:
                    if (OrderFm == null) {
                        OrderFm = new ListPlayFragment();
                        getSupportFragmentManager().beginTransaction().add(R.id.fl, OrderFm).commit();
                    } else {
                        getSupportFragmentManager().beginTransaction().show(OrderFm).commit();
                    }
                    break;
                case R.id.rb_my:
                    if (MyFm == null) {
                        MyFm = new MyFragment();
                        getSupportFragmentManager().beginTransaction().add(R.id.fl, MyFm).commit();
                    } else {
                        getSupportFragmentManager().beginTransaction().show(MyFm).commit();
                    }
                    break;
            }
            RadioButton radioButton = findViewById(checkedId);
            Drawable drawable = radioButton.getCompoundDrawables()[1].getCurrent();
            if (drawable instanceof ScaleDrawable) {
                final ScaleDrawable scaleDrawable = (ScaleDrawable) drawable;
                ValueAnimator valueAnimator = ValueAnimator.ofInt(10000,8000,10000);
                valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        scaleDrawable.setLevel((Integer) animation.getAnimatedValue());
                    }
                });
                valueAnimator.setDuration(500).start();
            }else if (drawable instanceof RotateDrawable){
                final RotateDrawable rotateDrawable = (RotateDrawable) drawable;
                ValueAnimator valueAnimator = ValueAnimator.ofInt(0,10000);
                valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        rotateDrawable.setLevel((Integer) animation.getAnimatedValue());
                    }
                });
                valueAnimator.setDuration(500).start();
            }
        }
    }

    @Override
    public int getContentViewResource() {
        return R.layout.activity_main;
    }

    @Override
    protected void initView() {
        fl = findViewById(R.id.fl);
        rg = findViewById(R.id.rg);

    }

    @Override
    protected void initData() {

        if(Build.VERSION.SDK_INT>=23){
            //动态获取内存存储权限
            int permission = ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE);
            if (permission != PackageManager.PERMISSION_GRANTED) {
                // We don't have permission so prompt the user
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        1);
            }
        }
    }

    @Override
    protected void initList() {
        rg.setOnCheckedChangeListener(new RadioGroupOnCheckedChangeListener());
        rg.check(R.id.rb_home);
    }

}
