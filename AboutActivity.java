package com.once.zhou.once;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


public class AboutActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, new AboutFragment())
                    .commit();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.activity_fade_in, R.anim.activity_slide_out);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.about, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class AboutFragment extends Fragment {

        public AboutFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_about, container, false);
            //deal with actionbar
            ActionBar mActionBar = getActivity().getActionBar();
            mActionBar.setDisplayShowHomeEnabled(false);
            mActionBar.setDisplayShowTitleEnabled(false);
            LayoutInflater mInflater = LayoutInflater.from(getActivity());

            //load custom actionbar design into the actionbar
            View mCustomView = mInflater.inflate(R.layout.custom_actionbar, null);
            TextView mTitleTextView = (TextView) mCustomView.findViewById(R.id.action_bar_title);
            mTitleTextView.setText(getText(R.string.app_name));
            //hide unwanted element
            final ClickableImageView btnLove =  (ClickableImageView)mCustomView
                    .findViewById(R.id.action_bar_button_love);
            btnLove.setAlpha(0f);

            mActionBar.setCustomView(mCustomView);
            mActionBar.setDisplayShowCustomEnabled(true);

            mActionBar.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.scheme_base)));
            return rootView;
        }
    }
}
