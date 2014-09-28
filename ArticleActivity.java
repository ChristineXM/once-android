package com.once.zhou.once;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.TransitionDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AnticipateOvershootInterpolator;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


public class ArticleActivity extends FragmentActivity {

    private Context mContext = this;

    private ViewPager mPager;
    private ScreenSlidePagerAdapter mPagerAdapter;
    private ArticleFragment mFirstPage;
    private ArticleFragment mSecondPage;
    private CoverFragment mCoverPage;

    private int NUM_PAGES = 3;

    private final int TODAY_MODE = 0;
    private final int RANDOM_MODE = 1;
    private int mode = RANDOM_MODE;

    private boolean mFirstTime = true;

    private ProgressDrawable mProgressDrawable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_random_pager);

        //deal with actionbar
        ActionBar mActionBar = getActionBar();
        mActionBar.setDisplayShowHomeEnabled(false);
        mActionBar.setDisplayShowTitleEnabled(false);
        LayoutInflater mInflater = LayoutInflater.from(this);

        //load custom actionbar design into the actionbar
        View mCustomView = mInflater.inflate(R.layout.custom_actionbar, null);
        TextView mTitleTextView = (TextView) mCustomView.findViewById(R.id.action_bar_title);
        mTitleTextView.setText(getText(R.string.app_name));

        //deal with click handling
        final ClickableImageView btnLove =  (ClickableImageView)mCustomView
                .findViewById(R.id.action_bar_button_love);
        btnLove.setAlpha(0f);
        btnLove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                final TransitionDrawable transition = (TransitionDrawable)((ImageView)view).getDrawable();
                ObjectAnimator flip = ObjectAnimator.ofFloat(view, "rotationY", 0.0f, 360f);
                flip.setInterpolator(new AnticipateOvershootInterpolator());
                flip.setDuration(500);
                flip.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        if (((ClickableImageView)view).numClicked % 2 == 0) {
                            transition.startTransition(600);
                        }
                        else{
                            transition.reverseTransition(600);
                        }
                    }
                });
                flip.start();
            }
        });
        mActionBar.setCustomView(mCustomView);
        mActionBar.setDisplayShowCustomEnabled(true);

        //deal with custom background
        int[] colorList = {
                getResources().getColor(R.color.scheme_3),
                getResources().getColor(R.color.scheme_2),
                getResources().getColor(R.color.scheme_1)};

        int backgroundColor = getResources().getColor(R.color.actionBarBackground);

        mProgressDrawable = new ProgressDrawable(colorList, backgroundColor);
        mActionBar.setBackgroundDrawable(mProgressDrawable);

        // Instantiate a ViewPager and a PagerAdapter.
        mPager = (ViewPager) findViewById(R.id.pager);
        mPagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager());
        mPager.setAdapter(mPagerAdapter);
        mPager.setPageTransformer(true, new DeckPageTransformer());
        //deal with next article loading

        mPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i2) {
                if (i == 0){
                    //deal with gradual revealing
                    mCoverPage.setmProgressDrawableBackgroundProgress(v);
                    btnLove.setAlpha(v);
                }
            }

            @Override
            public void onPageSelected(int i) {
                //complex logic about whether to load the new article @todo preview next article when reach bottom
                if (i == 1 && (mFirstPage.ismIfBottomReached() || mFirstTime)){
                    mFirstTime = false;
                    mFirstPage.hideContentWithoutAnim();
                    mFirstPage.fetchNew();
                }
                else if(i == 2 && (!mFirstPage.ismIfBottomReached())){
                    Toast.makeText(mContext, R.string.main_finish_reading_tip, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onPageScrollStateChanged(int i) {
                Log.v("state: ", "idle");
                if (mPager.getCurrentItem() != 0) {
                    if (i == ViewPager.SCROLL_STATE_IDLE) {
                        if (mFirstPage.ismIfBottomReached()) {
                            mPager.setCurrentItem(1, false);
                        } else {
                            mPager.setCurrentItem(1);
                        }
                    }
                }
            }
        });
    }


    @Override
    public void onBackPressed() {
        if (mPager.getCurrentItem() == 0) {
            // If the user is currently looking at the first step, allow the system to handle the
            // Back button. This calls finish() on this activity and pops the back stack.
            super.onBackPressed();
        } else {
            // Otherwise, select the previous step.
            mPager.setCurrentItem(mPager.getCurrentItem() - 1);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_about:
                Intent intent = new Intent(this, AboutActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.activity_slide_in, R.anim.activity_fade_out);
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public ProgressDrawable getmProgressDrawable(){
        return mProgressDrawable;
    }

    private class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {

        public ScreenSlidePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            ArticleFragment mainFragment = new ArticleFragment();
            Bundle bundle = new Bundle();
            //handle update here only when it is the first time launching the app, otherwise handle that in onPageSelected function
            if (position == 0) {
                CoverFragment coverFragment = new CoverFragment();
                mCoverPage = coverFragment;
                return coverFragment;
            }
            else if (position == 1){
                mFirstPage = mainFragment;
                bundle.putBoolean("fetchNewOnCreateView", false);
            }
            else{
                bundle.putBoolean("fetchNewOnCreateView", false);
            }
            mainFragment.setArguments(bundle);
            return mainFragment;
        }

        @Override
        public int getCount() {
            return NUM_PAGES;
        }

    }

}
