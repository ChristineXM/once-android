package com.once.zhou.once;

import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;

public class DeckPageTransformer implements ViewPager.PageTransformer {
    private static final float MIN_SCALE = 0.25f;

    public void transformPage(View view, float position) {
        int pageWidth = view.getWidth();

        if (position < -1) { // [-Infinity,-1)
            // This page is way off-screen to the left.
            view.setAlpha(0);

        } else if (position <= 0) { // [-1,0]
            // Use the default slide transition when moving to the left page
            //view.setAlpha(1 * (1 + position * 2));
            view.setAlpha(1);
            view.setTranslationX(0);
            view.setScaleX(1);
            view.setScaleY(1);

        } else if (position <= 1) { // (0,1]
            // Fade the page out.
            view.setAlpha(1 - position);
            //view.setX((float)pageWidth / 3);
            //reset the x translation when the view is completely hidden
            if (position == 1){
                view.setTranslationX(0);
                Log.v("translation", String.valueOf(view.getTranslationX()));
            }
            else {
                view.setTranslationX(pageWidth * -position / 3);
            }

        } else { // (1,+Infinity]
            // This page is way off-screen to the right.
            view.setAlpha(0);
        }
    }
}