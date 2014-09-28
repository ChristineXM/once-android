package com.once.zhou.once;


import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import org.apache.http.client.ClientProtocolException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;


/**
 * A simple {@link android.app.Fragment} subclass.
 *
 */
public class CoverFragment extends android.support.v4.app.Fragment {

    private ProgressDrawable mProgressDrawable;
    private ImageView mCoverImage;
    private ImageView mArrowImage;

    public CoverFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_cover, container, false);

        //get the cover image and load it with picasso
        mCoverImage = (ImageView) rootView.findViewById(R.id.cover_image);
        mArrowImage = (ImageView) rootView.findViewById(R.id.arrow_image);

        FetchCoverTask task = new FetchCoverTask();
        task.execute();

        mProgressDrawable = ((ArticleActivity) getActivity()).getmProgressDrawable();
        mProgressDrawable.setBackgroundProgress(0);

        return rootView;
    }

    public void setmProgressDrawableBackgroundAlpha(int alpha) {
        mProgressDrawable.setBackgroundPaintAlpha(alpha);
    }

    public void setmProgressDrawableBackgroundProgress(float progress) {
        mProgressDrawable.setBackgroundProgress(progress);
    }

    public void startScaleAnimation() {
        Animation scale = AnimationUtils.loadAnimation(getActivity(), R.anim.slow_scale_animation);
        mCoverImage.startAnimation(scale);
    }

    public void startJumpAnimation() {
        Animation jump = AnimationUtils.loadAnimation(getActivity(), R.anim.shake_animation);
        mArrowImage.startAnimation(jump);
    }

    public class FetchCoverTask extends AsyncTask<Void, Void, String> {

        protected final String LOG_TAG = FetchCoverTask.class.getSimpleName();

        @Override
        protected String doInBackground(Void... params) {
                /*
                * return a json string of the article
                * */

            try {
                final String COVER_BASE_URL = "http://once.21zhou.me/.background";

                Uri buildUri;
                buildUri = Uri.parse(COVER_BASE_URL).buildUpon().build();

                Log.v("request", buildUri.toString());

                InputStream is = null;

                URL url = new URL(buildUri.toString());
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(5000 /* milliseconds */);
                conn.setConnectTimeout(5000 /* milliseconds */);
                conn.setRequestMethod("GET");
                conn.setDoInput(true);
                // Starts the query
                conn.connect();
                int response = conn.getResponseCode();

                is = conn.getInputStream();

                // Convert the InputStream into a string

                BufferedReader reader = new BufferedReader(new InputStreamReader(is));

                StringBuilder sb = new StringBuilder();
                String line = null;
                while ((line = reader.readLine()) != null) // Read line by line
                    sb.append(line + "\n");

                String resString = sb.toString(); // Result is here
                is.close(); // Close the stream
                return resString;


            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (ClientProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;

        }

        @Override
        protected void onPostExecute(String coverUrl) {
            Picasso.with(getActivity())
                    .load(coverUrl)
                    .skipMemoryCache()
                    .placeholder(R.drawable.gradient_background)
                    .into(mCoverImage, new Callback() {
                        @Override
                        public void onSuccess() {
                            startScaleAnimation();
                            startJumpAnimation();
                        }

                        @Override
                        public void onError() {
                        }
                    });
        }


    }
}
