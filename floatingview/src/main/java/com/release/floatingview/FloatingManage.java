package com.release.floatingview;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.DrawableRes;
import android.support.v4.view.ViewCompat;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.AnticipateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.release.floatingview.listener.FloatMoveListener;
import com.release.floatingview.listener.IFloatingView;

import static com.release.floatingview.FloatRootView.MARGIN_EDGE;

public class FloatingManage implements IFloatingView, FloatMoveListener {

    private int TOAST_SHOW_TIME = 3000;//toast显示时间/毫秒
    private Context mContext;
    private FloatView mEnFloatingView;
    //    private static volatile FloatingManage mInstance;
    private FrameLayout mContainer;

    private View viewToast;
    private boolean isToastShow = false;

    private String toastStr;
    private int resId;

    //主体大小
    private float contentHeight;
    //toast大小
    private float toastWidth;
    //第一次
    private boolean isFirst = true;

    public FloatingManage(Activity activity) {
        mContainer = getActivityRoot(activity);
        this.mContext = activity;
    }

//    public static FloatingManage get() {
//        if (mInstance == null) {
//            synchronized (FloatingManage.class) {
//                if (mInstance == null) {
//                    mInstance = new FloatingManage();
//                }
//            }
//        }
//        return mInstance;
//    }

    @Override
    public FloatingManage remove() {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                if (mEnFloatingView == null) {
                    return;
                }
                if (ViewCompat.isAttachedToWindow(mEnFloatingView) && mContainer != null) {
                    mContainer.removeView(mEnFloatingView);
                }
                mEnFloatingView = null;
            }
        });
        return this;
    }

    @Override
    public FloatingManage add() {
        showFloat(mContext);
        if (mEnFloatingView != null) {
            mEnFloatingView.setIconImage(resId);
        }
        if (!TextUtils.isEmpty(toastStr)) {
            initToastView();
        }
        return this;
    }

    @Override
    public FloatingManage visibility() {
        if (mEnFloatingView != null) {
            if (mEnFloatingView.isShown()) {
                mEnFloatingView.setVisibility(View.GONE);
            } else {
                mEnFloatingView.setVisibility(View.VISIBLE);
            }
        }
        return this;
    }

    @Override
    public FloatView getView() {
        return mEnFloatingView;
    }

    @Override
    public FloatingManage icon(@DrawableRes int resId) {
        this.resId = resId;
        return this;
    }

    @Override
    public FloatingManage toast(String str) {
        toastStr = str;
        return this;
    }

    /**
     * 初始位置
     */
    private FrameLayout.LayoutParams getParams() {
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.BOTTOM | Gravity.END;
        params.setMargins(params.leftMargin, params.topMargin, dp2px(MARGIN_EDGE), dp2px(100));
        return params;
    }


    /**
     * 当前activity的content
     */
    private FrameLayout getActivityRoot(Activity activity) {
        if (activity == null) {
            return null;
        }
        try {
            return (FrameLayout) activity.getWindow().getDecorView().findViewById(android.R.id.content);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 显示
     */
    private void showFloat(Context context) {
        synchronized (this) {
            if (mEnFloatingView != null) {
                return;
            }
            mEnFloatingView = new FloatView(context.getApplicationContext());//全应用
            mEnFloatingView.setFloatMoveListener(this);
            mEnFloatingView.setLayoutParams(getParams());
            if (mContainer == null) {
                return;
            }
            mContainer.addView(mEnFloatingView);

            //主体绘制完成后 显示toast
            mEnFloatingView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    if (isFirst) {
                        showToastDialog();
                        isFirst = false;
                    }
                }
            });
        }
    }

    /**
     * 初始化toast
     */
    private void initToastView() {
        mEnFloatingView.measure(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
        contentHeight = mEnFloatingView.getMeasuredHeight();

        viewToast = LayoutInflater.from(mContext).inflate(R.layout.view_floating_toast, null);
        viewToast.findViewById(R.id.rl_totst).getLayoutParams().height = (int) contentHeight;
        ((TextView) viewToast.findViewById(R.id.tv_toast)).setText(toastStr);

        viewToast.measure(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
        toastWidth = viewToast.getMeasuredWidth();
    }

    private void showToastDialog() {
        if (viewToast == null) {
            return;
        }
        if (!isToastShow) {
            showToast();
        } else {
            closeToast();
        }
    }

    private void showToast() {
        isToastShow = true;
        mContainer.removeView(viewToast);

        float x = mEnFloatingView.getX();
        float y = mEnFloatingView.getY();

        viewToast.setX(x - toastWidth);
        viewToast.setY(y);

        mContainer.addView(viewToast, 1, new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT));

        AnimatorSet set = new AnimatorSet();
        ObjectAnimator animatorX = ObjectAnimator.ofFloat(viewToast, "X", x, x - toastWidth);
        ObjectAnimator alpha = ObjectAnimator.ofFloat(viewToast, "alpha", 0, 1f);
        animatorX.setInterpolator(new OvershootInterpolator());
        animatorX.setDuration(500);
        set.play(alpha).with(animatorX);
        set.start();
        mContainer.postDelayed(closeRunnable, TOAST_SHOW_TIME);
    }

    private void closeToast() {
        mContainer.removeCallbacks(closeRunnable);
        isToastShow = false;

        float x = mEnFloatingView.getX();

        AnimatorSet set = new AnimatorSet();
        ObjectAnimator animatorX = ObjectAnimator.ofFloat(viewToast, "X", x - toastWidth, x);
        ObjectAnimator alpha = ObjectAnimator.ofFloat(viewToast, "alpha", 1f, 0);
        animatorX.setInterpolator(new AnticipateInterpolator());
        animatorX.setDuration(500);
        set.play(animatorX).with(alpha);
        set.start();
        set.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                mContainer.removeView(viewToast);
            }

            @Override
            public void onAnimationCancel(Animator animation) {
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }
        });
    }

    private void closeToastQuick() {
        isToastShow = false;
        mContainer.removeView(viewToast);
    }

    private CloseRunnable closeRunnable = new CloseRunnable();

    private class CloseRunnable implements Runnable {
        @Override
        public void run() {
            if (isToastShow) {
                closeToast();
            }
        }
    }

    @Override
    public void onMove(FloatRootView magnetView) {
        closeToastQuick();
    }


    private int dp2px(float value) {
        float scale = mContext.getResources().getDisplayMetrics().densityDpi;
        return (int) (value * (scale / 160) + 0.5f);
    }
}