package com.once.zhou.once;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by zhou on 8/12/14.
 */
public class CustomAnimator {

    private int mShortAnimationDuration = 1000;

    public CustomAnimator(){}
    //deal with animation
    public void fade(final ArrayList<View> viewsToHide){
        ValueAnimator alphaAnim;
        for (final View viewToHide: viewsToHide) {
            viewToHide.setVisibility(View.VISIBLE);
            //deal with textview seperately
            if (viewToHide instanceof TextView) {
                final TextView tView = (TextView) viewToHide;
                alphaAnim = ValueAnimator.ofInt(255, 0);
                alphaAnim.setDuration(mShortAnimationDuration);
                alphaAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator valueAnimator) {
                        tView.setTextColor(tView.getTextColors().withAlpha((Integer) valueAnimator.getAnimatedValue()));
                    }
                });
                alphaAnim.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        viewToHide.setVisibility(View.GONE);
                    }
                });
                alphaAnim.start();
            } else {
                // Animate the loading view to 0% opacity. After the animation ends,
                // set its visibility to GONE as an optimization step (it won't
                // participate in layout passes, etc.)
                viewToHide.animate()
                        .alpha(0f)
                        .setDuration(mShortAnimationDuration)
                        .setListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                viewToHide.setVisibility(View.GONE);
                            }
                        });
            }
        }
    }

    public void show(ArrayList<View> viewsToShow){
        ValueAnimator alphaAnim;
        for (View viewToShow:viewsToShow) {
            viewToShow.setVisibility(View.VISIBLE);
            //deal with textview seperately
            if (viewToShow instanceof TextView){
                final TextView tView = (TextView) viewToShow;
                alphaAnim = ValueAnimator.ofInt(0, 255);
                alphaAnim.setDuration(mShortAnimationDuration);
                alphaAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator valueAnimator) {
                        tView.setTextColor(tView.getTextColors().withAlpha((Integer) valueAnimator.getAnimatedValue()));
                    }
                });
                alphaAnim.start();
            }
            else{
                viewToShow.setAlpha(0f);
                viewToShow.animate()
                        .alpha(1f)
                        .setDuration(mShortAnimationDuration)
                        .setListener(null);
            }
        }
    }

    /**
     *do crossfade on the given list of views
     * */
    public void crossfade(ArrayList<View> viewsToShow, ArrayList<View> viewsToHide) {

        show(viewsToShow);

        fade(viewsToHide);
    }
}
