package com.once.zhou.once;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

public class ClickableImageView extends ImageView{

    public int numClicked = 0;

    public ClickableImageView(Context context) {
        super(context);
    }

    public ClickableImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ClickableImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void setPressed(boolean pressed) {
        super.setPressed(pressed);
        numClicked ++;
    }
}