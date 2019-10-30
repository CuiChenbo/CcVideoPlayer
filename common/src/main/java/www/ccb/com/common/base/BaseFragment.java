package www.ccb.com.common.base;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.gyf.barlibrary.ImmersionBar;
import www.ccb.com.common.widget.dialog.CbLoadingDialog;


public abstract class BaseFragment extends Fragment {

    private boolean isVisible;                  //是否可见状态
    protected LayoutInflater inflater;
    public ImmersionBar mImmersionBar;
    public Context mContext;

    public View rootView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        this.inflater = inflater;
        mContext = getActivity();
        rootView = initContentView(inflater, container, savedInstanceState);
        initView(rootView);
        loadData();
        initListener();
        return rootView;
    }

    public <T extends View> T findViewById(@IdRes int ids) {
        return rootView.findViewById(ids);
    }

    public void start(Class clazz) {
        startActivity(new Intent(mContext, clazz));
    }

    protected void start(@NonNull Class<?> cls, @NonNull Bundle extras) {
        Intent intent = new Intent(mContext, cls);
        intent.putExtras(extras);
        startActivity(intent);
    }

    public void isTitleBar(boolean is, View v) {
        if (is) {
            mImmersionBar = ImmersionBar.with(this);  //可以为任意view;
            mImmersionBar.titleBarMarginTop(v).statusBarDarkFont(true, 0.2f).init();
        }
    }

    /**
     * 如果是与ViewPager一起使用，调用的是setUserVisibleHint
     */
    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (getUserVisibleHint()) {
            isVisible = true;
            onVisible();
        } else {
            isVisible = false;
            onInvisible();
        }
    }

    /**
     * 如果是通过FragmentTransaction的show和hide的方法来控制显示，调用的是onHiddenChanged.
     * 若是初始就show的Fragment 为了触发该事件 需要先hide再show
     */
    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            isVisible = true;
            onVisible();
        } else {
            isVisible = false;
            onInvisible();
        }

        if (!hidden && mImmersionBar != null)
            mImmersionBar.init();
    }

    protected void onVisible() {
    }

    protected void onInvisible() {
    }

    protected abstract View initContentView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState);

    public abstract void initView(View view);

    public abstract void loadData();

    public abstract void initListener();

    private CbLoadingDialog mProgressDialog;

    public void showProgressDialog(String msg) {
        if (this.mProgressDialog == null)
            this.mProgressDialog = new CbLoadingDialog(mContext);
        this.mProgressDialog.show();
    }

    public void dismissProgressDialog() {
        if (this.mProgressDialog != null)
            this.mProgressDialog.dismiss();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mImmersionBar != null)
            mImmersionBar.destroy();
    }
}