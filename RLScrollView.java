package com.once.zhou.once;

import android.content.Context;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.widget.ScrollView;

/**
 * Created by zhou on 8/5/14.
 */
public class RLScrollView extends ScrollView {

    private static final int MAX_Y_OVERSCROLL_DISTANCE = 100;

    private Context mContext;
    private int mMaxYOverscrollDistance;

    public RLScrollView(Context context) {
        super(context);
        mContext = context;
        initBounceScrollView();
    }

    public RLScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        initBounceScrollView();
    }

    public RLScrollView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mContext = context;
        initBounceScrollView();
    }

    private void initBounceScrollView()
    {
        //get the density of the screen and do some maths with it on the max overscroll distance
        //variable so that you get similar behaviors no matter what the screen size

        final DisplayMetrics metrics = mContext.getResources().getDisplayMetrics();
        final float density = metrics.density;

        mMaxYOverscrollDistance = (int) (density * MAX_Y_OVERSCROLL_DISTANCE);
    }

    public int getMaxYOverscrollDistance(){
        return mMaxYOverscrollDistance;
    }

    public interface OnScrollChangedListener{
        public void onScrollChanged(int x, int y, int oldX, int oldY);
    }

    private OnScrollChangedListener onScrollChangedListener;

    /**
     *
     * @param onScrollChangedListener
     */
    public void setOnScrollListener(OnScrollChangedListener onScrollChangedListener){
        this.onScrollChangedListener=onScrollChangedListener;
    }

    @Override
    protected void onScrollChanged(int x, int y, int oldX, int oldY){
        super.onScrollChanged(x, y, oldX, oldY);
        if(onScrollChangedListener!=null){
            onScrollChangedListener.onScrollChanged(x, y, oldX, oldY);
        }
    }

    /**
    * allowing overscroll
    * */
    @Override
    protected boolean overScrollBy(int deltaX, int deltaY, int scrollX, int scrollY, int scrollRangeX, int scrollRangeY, int maxOverScrollX, int maxOverScrollY, boolean isTouchEvent)
    {
        //This is where the magic happens, we have replaced the incoming maxOverScrollY with our own custom variable mMaxYOverscrollDistance;
        //make is only bounce on the top
        if (scrollY > 0) {
            return super.overScrollBy(deltaX, deltaY, scrollX, scrollY, scrollRangeX, scrollRangeY, maxOverScrollX, mMaxYOverscrollDistance, isTouchEvent);
        }
        else{
            return super.overScrollBy(deltaX, deltaY, scrollX, scrollY, scrollRangeX, scrollRangeY, maxOverScrollX, maxOverScrollY, isTouchEvent);
        }

    }

    /**
    * make scroll slower in general
    * */
    @Override
    public void fling(int velocityY) {
        super.fling(velocityY * 5 / 11);
    }
}