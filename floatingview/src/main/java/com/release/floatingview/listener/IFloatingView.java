package com.release.floatingview.listener;

import android.support.annotation.DrawableRes;

import com.release.floatingview.FloatView;
import com.release.floatingview.FloatingManage;


public interface IFloatingView {

    FloatingManage icon(@DrawableRes int resId);

    FloatingManage toast(String str);

    FloatingManage add();

    FloatingManage visibility();//显示/隐藏

    FloatingManage remove();

    FloatView getView();

}
