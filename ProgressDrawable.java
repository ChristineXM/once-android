package com.once.zhou.once;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ArgbEvaluator;
import android.animation.FloatEvaluator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.view.animation.AccelerateDecelerateInterpolator;

    public class ProgressDrawable extends ColorDrawable {

        private final int defaultBackgroundColor;
        private final int mDuration = 600;
        private final int mShortDuration = 400;

        /** The drawable's bounds Rect */
        private final Rect mBounds = getBounds();
        /** Used to draw the progress bar */
        private Paint mProgressPaint = new Paint();
        private Paint mBackgroundPaint = new Paint();

        /** The current progress */
        public float progress;
        public float backgroundProgress;

        //track with color to use
        private int mColorIndex = 0;
        private int[] mColorList;

        //deal with animation
        ValueAnimator mProgressAnim = ObjectAnimator.ofFloat(this, "progress", getProgress(), 1.0f);
        ValueAnimator mColorAnim;
        ValueAnimator mColorAnim2;

        public ProgressDrawable(int[] colorList, int backgroundColor) {

            //initialize backgroud color
            defaultBackgroundColor = backgroundColor;
            mBackgroundPaint.setColor(defaultBackgroundColor);
            mBackgroundPaint.setAntiAlias(true);

            //update colorlist
            mColorList = colorList;

            mProgressPaint.setColor(mColorList[mColorIndex]);
            mProgressPaint.setAntiAlias(true);

            resetAnimation();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void draw(Canvas canvas) {
            final double centerX = mBounds.centerX();
            final double centerY = mBounds.centerY();
            final float radius = (float) Math.sqrt(centerX * centerX + centerY * centerY) * progress;
            final float radiusBackground = (float) Math.sqrt(centerX * centerX + centerY * centerY) * backgroundProgress;

            //draw background
            canvas.drawCircle(mBounds.centerX(), mBounds.centerY(), radiusBackground, mBackgroundPaint);
            //draw progress circle
            canvas.drawCircle(mBounds.centerX(), mBounds.centerY(), radius, mProgressPaint);

            //draw the left rect
            //canvas.drawRect(left, mBounds.top, center, mBounds.bottom / 10, mProgressPaint);
            //draw the right rect
            //canvas.drawRect(mBounds.centerX(), mBounds.top, right, mBounds.bottom / 10, mProgressPaint);
        }


        /**
         * set the current progress, between 0 and 1f
         * */
        public void setProgress(float progress) {
            this.progress = progress;
            invalidateSelf();
        }

        public float getProgress() {
            return this.progress;
        }

        public void setBackgroundProgress(float progress){
            this.backgroundProgress = progress;
            invalidateSelf();
        }

        public float getBackgroundProgress(){
            return this.backgroundProgress;
        }

        /**
         * set or get the current alpha value of the mProgressPaint
         * */
        public int getProgressPaintAlpha(){
            return mProgressPaint.getAlpha();
        }
        public void setProgressPaintAlpha(int a){
            mProgressPaint.setAlpha(a);
            invalidateSelf();
        }

        public int getBackgroundPaintAlpha(){
            return mBackgroundPaint.getAlpha();
        }
        public void setBackgroundPaintAlpha(int a){
            mBackgroundPaint.setAlpha(a);
            invalidateSelf();
        }

        public int getProgressPaintColor(){
            return mProgressPaint.getColor();
        }
        public void setProgressPaintColor(int color){
            mProgressPaint.setColor(color);
            invalidateSelf();
        }

        public int getBackGroundPaintColor(){
            return mBackgroundPaint.getColor();
        }
        public void setBackGroundPaintColor(int color){
            mBackgroundPaint.setColor(color);
            invalidateSelf();
        }

       //animate
        public void animate(){
            mProgressAnim.start();
        }

        private void resetAnimation(){
            mBackgroundPaint.setColor(defaultBackgroundColor);
            setProgress(0);
            mColorIndex = 0;
            mProgressPaint.setColor(mColorList[mColorIndex]);

            mProgressAnim = ObjectAnimator.ofFloat(this, "progress", getProgress(), 1.0f);
            mProgressAnim.setDuration(mDuration);
            mProgressAnim.setRepeatCount(ValueAnimator.INFINITE);
            mProgressAnim.setInterpolator(new AccelerateDecelerateInterpolator());
            mProgressAnim.setEvaluator(new FloatEvaluator());
            mProgressAnim.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationRepeat(Animator animation) {
                    //loop over the list of color over repetition
                    setBackGroundPaintColor(mProgressPaint.getColor());
                    mColorIndex = (mColorIndex + 1) % mColorList.length;
                    mProgressPaint.setColor(mColorList[mColorIndex]);
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    doEndAnimation();
                }

                @Override
                public void onAnimationPause(Animator animation) {
                }
            });
        }

        public void doEndAnimation(){
            mColorAnim = ValueAnimator.ofObject(new ArgbEvaluator(), mBackgroundPaint.getColor(), defaultBackgroundColor);
            mColorAnim.setDuration(mShortDuration);
            mColorAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animator) {
                    setBackGroundPaintColor((Integer) animator.getAnimatedValue());
                }
            });
            mColorAnim2 = ValueAnimator.ofObject(new ArgbEvaluator(), mProgressPaint.getColor(), defaultBackgroundColor);
            mColorAnim2.setDuration(mShortDuration);
            mColorAnim2.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animator) {
                    setProgressPaintColor((Integer) animator.getAnimatedValue());
                }
            });

            AnimatorSet animatorSet = new AnimatorSet();
            animatorSet.play(mColorAnim).with(mColorAnim2);
            animatorSet.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    resetAnimation();
                }
            });
            animatorSet.start();
        }
        /**
         * get if the main animation is running(could be buggy because mProgressAnim is not the only animation)
         * */
        public boolean isAnimationRunning(){
            return (mProgressAnim.isRunning());
        }
        /**
         * expose a method for other class to end the animation
         * */
        public void endAnimation(){
            mProgressAnim.cancel();
        }

    }


