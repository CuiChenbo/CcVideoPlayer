package www.ccb.com.common.base;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowInsets;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;

import com.google.gson.Gson;
import com.gyf.barlibrary.ImmersionBar;

import java.util.List;

import www.ccb.com.common.BaseApplication;
import www.ccb.com.common.R;
import www.ccb.com.common.widget.dialog.CbLoadingDialog;

public abstract class BaseActivity extends AppCompatActivity {

    public Context mContext;
    public Bundle savedInstanceState;
    public ImmersionBar mImmersionBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getContentViewResource());
        BaseApplication.get().addActivity(this);
        this.savedInstanceState = savedInstanceState;
//        mImmersionBar = ImmersionBar.with(this);
//        mImmersionBar.init();  //所有子类都将继承这些相同的属性
        setStatusBarTransparent();
        mContext = this;
        initView();
        initData();
        initList();
    }

    public void start(Class clazz) {
        startActivity(new Intent(mContext, clazz));
    }

    protected void start(@NonNull Class<?> cls, @NonNull Bundle extras) {
        Intent intent = new Intent(mContext, cls);
        intent.putExtras(extras);
        startActivity(intent);
    }

    public abstract int getContentViewResource();

    protected abstract void initView();

    protected abstract void initData();

    protected abstract void initList();

    /**
     * 把状态栏设成透明
     */
    protected void setStatusBarTransparent() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            View decorView = getWindow().getDecorView();
            decorView.setOnApplyWindowInsetsListener((v, insets) -> {
                WindowInsets defaultInsets = v.onApplyWindowInsets(insets);
                return defaultInsets.replaceSystemWindowInsets(
                        defaultInsets.getSystemWindowInsetLeft(),
                        0,
                        defaultInsets.getSystemWindowInsetRight(),
                        defaultInsets.getSystemWindowInsetBottom());
            });
            ViewCompat.requestApplyInsets(decorView);
            getWindow().setStatusBarColor(ContextCompat.getColor(this, android.R.color.transparent));
        }
    }

    public void UpTitle(String title) {
        if (findViewById(R.id.vBar) == null) return;
        mImmersionBar.titleBar(R.id.vBar).statusBarDarkFont(true, 0.2f).init();
        findViewById(R.id.tvTitleBack).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        ((TextView) findViewById(R.id.tvTitleBar)).setText(title == null ? "" : title);
    }
    private CbLoadingDialog mProgressDialog;

    public void showProgressDialog(String msg) {
        if (this.mProgressDialog == null)
            this.mProgressDialog = new CbLoadingDialog(mContext);
        if (!TextUtils.isEmpty(msg)) {
            this.mProgressDialog.setMessage(msg);
        } else {
            this.mProgressDialog.setMessage("");
        }
        this.mProgressDialog.show();
    }

    public void dismissProgressDialog() {
        if (this.mProgressDialog != null)
            this.mProgressDialog.dismiss();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        BaseApplication.get().finishActivity(this);
        if (mImmersionBar != null)
            mImmersionBar.destroy();  //必须调用该方法，防止内存泄漏，不调用该方法，如果界面bar发生改变，在不关闭app的情况下，退出此界面再进入将记忆最后一次bar改变的状态
    }
}
