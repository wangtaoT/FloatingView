package com.release.floatingview;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.widget.FrameLayout;

import com.release.floatingview.listener.FloatClickListener;
import com.release.floatingview.listener.FloatMoveListener;
import com.release.floatingview.utils.SystemUtils;

public class FloatRootView extends FrameLayout {

    private Context mContext;
    //左右边距
    public static final int MARGIN_EDGE = 16;
    //上下边距
    public static final int MARGIN_EDGE_V = 100;
    //记录down/up位置
    private float xUpScreen;
    private float yUpScreen;
    private float xDownInScreen;
    private float yDownInScreen;

    private float mOriginalRawX;
    private float mOriginalRawY;
    private float mOriginalX;
    private float mOriginalY;
    private FloatClickListener mMagnetViewListener;
    private FloatMoveListener mFloatMoveListener;
    protected MoveAnimator mMoveAnimator;
    private int mScreenWidth;
    private int mScreenHeight;

    public void setFloatClickListener(FloatClickListener magnetViewListener) {
        this.mMagnetViewListener = magnetViewListener;
    }

    public void setFloatMoveListener(FloatMoveListener floatMoveListener) {
        this.mFloatMoveListener = floatMoveListener;
    }

    public FloatRootView(Context context) {
        this(context, null);
    }

    public FloatRootView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FloatRootView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mContext = context;
        init();
    }

    private void init() {
        mMoveAnimator = new MoveAnimator();
        setClickable(true);
        updateSize();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event == null) {
            return false;
        }
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                changeOriginalTouchParams(event);
                updateSize();
                mMoveAnimator.stop();
                dealMoveEvent();

                xDownInScreen = event.getRawX();
                yDownInScreen = event.getRawY();
                xUpScreen = xDownInScreen;
                yUpScreen = yDownInScreen;
                break;
            case MotionEvent.ACTION_MOVE:
                xUpScreen = event.getRawX();
                yUpScreen = event.getRawY();
                updateViewPosition(event);
                break;
            case MotionEvent.ACTION_UP:
                moveToEdge();
                if (isOnClickEvent()) {
                    dealClickEvent();
                }
                break;
        }
        return false;
    }

    protected void dealClickEvent() {
        if (mMagnetViewListener != null) {
            mMagnetViewListener.onClick(this);
        }
    }

    private void dealMoveEvent() {
        if (mFloatMoveListener != null) {
            mFloatMoveListener.onMove(this);
        }
    }

    /**
     * 是否为点击事件
     */
    private boolean isOnClickEvent() {
        int scaledTouchSlop = ViewConfiguration.get(mContext).getScaledTouchSlop();// - 10;
        return Math.abs(xDownInScreen - xUpScreen) <= scaledTouchSlop
                && Math.abs(yDownInScreen - yUpScreen) <= scaledTouchSlop;
    }

    private void updateViewPosition(MotionEvent event) {
        // 限制不可超出屏幕宽度
        float desX = mOriginalX + event.getRawX() - mOriginalRawX;
        if (desX <= 0) {
            desX = 0;
        }
        if (desX > mScreenWidth) {
            desX = mScreenWidth;
        }
        setX(desX);

        // 限制不可超出屏幕高度
        float desY = mOriginalY + event.getRawY() - mOriginalRawY;
        if (desY <= MARGIN_EDGE_V) {
            desY = MARGIN_EDGE_V;
        }
        if (desY > mScreenHeight - getHeight() - dp2px(MARGIN_EDGE_V)) {
            desY = mScreenHeight - getHeight() - dp2px(MARGIN_EDGE_V);
        }
        setY(desY);
    }

    private void changeOriginalTouchParams(MotionEvent event) {
        mOriginalX = getX();
        mOriginalY = getY();
        mOriginalRawX = event.getRawX();
        mOriginalRawY = event.getRawY();
    }

    protected void updateSize() {
        mScreenWidth = (SystemUtils.getScreenWidth(getContext()) - this.getWidth());
        mScreenHeight = SystemUtils.getScreenHeight(getContext());
    }

    public void moveToEdge() {
        float moveDistance = isNearestLeft() ? dp2px(MARGIN_EDGE) : mScreenWidth - dp2px(MARGIN_EDGE);
        mMoveAnimator.start(moveDistance, getY());
    }

    protected boolean isNearestLeft() {
        int middle = mScreenWidth / 2;
        return getX() < middle;
    }

//    public void onRemove() {
//        if (mMagnetViewListener != null) {
//            mMagnetViewListener.onRemove(this);
//        }
//    }

    protected class MoveAnimator implements Runnable {
        private Handler handler = new Handler(Looper.getMainLooper());
        private float destinationX;
        private float destinationY;
        private long startingTime;

        void start(float x, float y) {
            this.destinationX = x;
            this.destinationY = y;
            startingTime = System.currentTimeMillis();
            handler.post(this);
        }

        @Override
        public void run() {
            if (getRootView() == null || getRootView().getParent() == null) {
                return;
            }
            float progress = Math.min(1, (System.currentTimeMillis() - startingTime) / 400f);
            float deltaX = (destinationX - getX()) * progress;
            float deltaY = (destinationY - getY()) * progress;
            move(deltaX, deltaY);
            if (progress < 1) {
                handler.post(this);
            }
        }

        private void stop() {
            handler.removeCallbacks(this);
        }
    }

    private void move(float deltaX, float deltaY) {
        setX(getX() + deltaX);
        setY(getY() + deltaY);
    }

    private int dp2px(float value) {
        float scale = mContext.getResources().getDisplayMetrics().densityDpi;
        return (int) (value * (scale / 160) + 0.5f);
    }
}
