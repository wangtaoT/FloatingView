package com.release.floatingview;

import android.content.Context;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import android.view.MotionEvent;

import com.makeramen.roundedimageview.RoundedImageView;

public class FloatView extends FloatRootView {

    private RoundedImageView ivCover;

    public FloatView(@NonNull Context context) {
        super(context, null);
        inflate(context, R.layout.view_floating, this);
        ivCover = findViewById(R.id.iv_cover);
    }

    public void setIconImage(@DrawableRes int resId) {
        ivCover.setImageResource(resId);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);
        if (event != null) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    break;
                case MotionEvent.ACTION_UP:
                    break;
            }
        }
        return true;
    }

}
