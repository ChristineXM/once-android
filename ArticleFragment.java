package com.once.zhou.once;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.text.Spannable;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.URLSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.apache.http.client.ClientProtocolException;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class ArticleFragment extends Fragment implements RLScrollView.OnScrollChangedListener{

    //deal with main content
    private LinearLayout mContentLayout;
    private TextView mTitle;
    private TextView mAuthor;
    private TextView mContent;
    private String mLink;

    //decors
    private ProgressDrawable mProgressDrawable;
    private RLScrollView mScrollView;

    //deal with main error
    private LinearLayout mErrorLayout;
    private Button mBtnRefresh;

    private boolean mIfBottomReached = false;

    //current article
    private Article mCurrentArticle;

    public ArticleFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup)inflater.inflate(R.layout.fragment_article, container, false);

        //deal with actionbar
        mProgressDrawable = ((ArticleActivity)getActivity()).getmProgressDrawable();

        //deal with actionbar autohide
        mScrollView = (RLScrollView) rootView.findViewById(R.id.main_scroll_view);
        mScrollView.setOnScrollListener(this);
        int options = View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION;
        mScrollView.setSystemUiVisibility(options);

        //initialize article related component
        mContentLayout = (LinearLayout) rootView.findViewById(R.id.main_linear_layout);
        mTitle = (TextView) rootView.findViewById(R.id.main_title);
        mAuthor = (TextView) rootView.findViewById(R.id.main_author);
        mContent = (TextView) rootView.findViewById(R.id.main_content);
        mContent.setMovementMethod(LinkMovementMethod.getInstance()); //declare link handler here instead of in the xml file, cuz thats buggy(it reads the content itself).


        //initialize error related component
        mErrorLayout = (LinearLayout) rootView.findViewById(R.id.main_error_linear_layout);
        mBtnRefresh = (Button) rootView.findViewById(R.id.main_button_refresh);
        mBtnRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideError();
                fetchNew();
            }
        });

        //get the argument, trick that allows view pagers to work
        if (getArguments().getBoolean("fetchNewOnCreateView")){
            fetchNew();
        }

        return rootView;
    }

    @Override
    public void onScrollChanged(int x, int y, int oldX, int oldY) {

        View view = (View) mScrollView.getChildAt(mScrollView.getChildCount()-1);
        int difference = (view.getBottom()-(mScrollView.getHeight()+mScrollView.getScrollY()+view.getTop()));// Calculate the scrolldiff
        if( difference == 0 ){  // if diff is zero, then the bottom has been reached
            mIfBottomReached = true;
        }

        //listener for customized scrollview
        double diff = y - oldY;
        int maxOverscrollDistance = mScrollView.getMaxYOverscrollDistance();

        View decorView = getActivity().getWindow().getDecorView();
        //deal with autohide
        if (diff < -20 || y <= mTitle.getHeight()) {
            int uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
            decorView.setSystemUiVisibility(uiOptions);
        }
        else if (diff > 20) {
            // Hide the status bar.
            int uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                    | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                    | View.SYSTEM_UI_FLAG_IMMERSIVE;

            decorView.setSystemUiVisibility(uiOptions);
        }


        //deal with auto change background paint transpancy
        if (y == 0) {mProgressDrawable.setBackgroundPaintAlpha(255);}
        else if (y > 0 && y <= 200){
            mProgressDrawable.setBackgroundPaintAlpha(255 - y);
        }
        else if (y >= 256){
            mProgressDrawable.setBackgroundPaintAlpha(55);
        }

        /*
        //deal with pull to refresh
        if (y < 0 && !mProgressDrawable.isAnimationRunning()) {
            mProgressDrawable.setProgress((float) Math.abs(y) / (maxOverscrollDistance * 6));
            //only trigger the update when reaching the top
            if (y == -maxOverscrollDistance){
                Log.v("trigger:", "trigger on");
                //show a toast to remind the user
                Toast.makeText(getActivity(), R.string.main_refresh_trigger_tip, Toast.LENGTH_SHORT).show();
                mUpdateTrigger = true;
            }
        }
        else if(!mProgressDrawable.isAnimationRunning()){
            mProgressDrawable.setProgress(0);
            if (mUpdateTrigger == true){
                mUpdateTrigger = false;
                Log.v("trigger:", "firing!");
                doUpdate();
            }
        }
        */
    }

    public boolean ismIfBottomReached(){
        return mIfBottomReached;
    }
    public void resetmIfBottomReached() {mIfBottomReached = false;}

    public void fetchNew(){
        resetmIfBottomReached();

        //check if the network is available
        ConnectivityManager connMgr = (ConnectivityManager)
                getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            //scroll to the top to: 1. get out the actionbar 2. reset the view
            mScrollView.scrollTo(0, 0);
            //deal with animation
            mProgressDrawable.animate();
            FetchArticleTask task = new FetchArticleTask();
            task.execute(true);
        } else {
            hideContentWithoutAnim();
            showError();
        }
    }

    public void updateContent(Article article){
        hideContentWithoutAnim();

        mTitle.setText(article.title);
        mAuthor.setText(article.author);
        mContent.setText(article.content);
        mLink = article.link.toString();

        showContent();
    }

    public void showContent(){
        mContentLayout.setVisibility(View.VISIBLE);
        Animation slideIn = AnimationUtils.loadAnimation(getActivity(), R.anim.slide_in_animation);
        mContentLayout.startAnimation(slideIn);

    }

    public void hideContentWithoutAnim(){
        mContentLayout.setVisibility(View.INVISIBLE);
    }
    public void showContentWithoutAnim(){
        mContentLayout.setVisibility(View.VISIBLE);
    }

    public void showError(){
        mErrorLayout.setVisibility(View.VISIBLE);
        Animation slideIn = AnimationUtils.loadAnimation(getActivity(), R.anim.slide_in_animation);
        mErrorLayout.startAnimation(slideIn);

        View decorView = getActivity().getWindow().getDecorView();
        //show the navigation bar and actionbar
        int uiOptions =  View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
    }

    public void hideError(){
        mErrorLayout.setVisibility(View.GONE);
    }


    public class FetchArticleTask extends AsyncTask<Boolean, Void, Article> {

        protected final String LOG_TAG = FetchArticleTask.class.getSimpleName();

        @Override
        protected Article doInBackground(Boolean...params){
                /*
                * return a json string of the article
                * */

            try{
                final boolean fetchNewOne = params[0];
                //for testing purpose
                final String ARTICLE_BASE_URL = "http://once21zhou.sinaapp.com/random/.json";
                final String ID_PARAM = "id";
                final String TYPE_PARAM = "type";

                Uri buildUri;
                if(fetchNewOne) {
                    buildUri = Uri.parse(ARTICLE_BASE_URL).buildUpon().appendQueryParameter(TYPE_PARAM, "random").build();
                }
                else{
                    buildUri = Uri.parse(ARTICLE_BASE_URL).buildUpon().appendQueryParameter(ID_PARAM, mCurrentArticle.id).build();
                }
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
                return getArticleDataFromJson(resString);


            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (ClientProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;

        }

        @Override
        protected void onPostExecute(Article article) {
            mProgressDrawable.endAnimation();
            if (article != null) {
                updateContent(article);
            }
            else{
                hideContentWithoutAnim();
                showError();
            }
        }

        private Article getArticleDataFromJson(String articleJsonStr) throws JSONException{
            final String OWM_ID = "id";
            final String OWM_TITLE = "title";
            final String OWM_AUTHOR = "author";
            final String OWM_LINK = "link";
            final String OWM_CONTENT = "content";

            //for now just get once random article
            String id;
            String imageUrl = "http://img3.douban.com/view/photo/photo/public/p2187119623.jpg";
            String title;
            String author;
            String link;
            String content;

            JSONObject article = new JSONObject(articleJsonStr);
            id = article.getString(OWM_ID);
            title = article.getString(OWM_TITLE);
            author = article.getString(OWM_AUTHOR);
            link = article.getString(OWM_LINK);
            content = article.getString(OWM_CONTENT);

            UrlImageParser imgGetter = new UrlImageParser(mContent, getActivity());
            Spanned contentSpanned = Html.fromHtml(content, imgGetter, null);
            Spannable linkSpanned = makeSpannableFromLink(link, title);

            Article OBJArticle = new Article(id, imageUrl, title, author, linkSpanned, contentSpanned);
            mCurrentArticle = OBJArticle;

            return OBJArticle;
        }

        private Spannable makeSpannableFromLink(String link, String title){
            String markUp = String.format("<a href=\"%s\">%s%s</a>", link, getResources().getString(R.string.main_link_view_text), title);

            Spannable spannedText = Spannable.Factory.getInstance().newSpannable(
                    Html.fromHtml(markUp));
            return removeUnderlines(spannedText);
        }

        private Spannable removeUnderlines(Spannable p_Text) {
            URLSpan[] spans = p_Text.getSpans(0, p_Text.length(), URLSpan.class);
            for (URLSpan span : spans) {
                int start = p_Text.getSpanStart(span);
                int end = p_Text.getSpanEnd(span);
                p_Text.removeSpan(span);
                span = new URLSpanNoUnderline(span.getURL());
                p_Text.setSpan(span, start, end, 0);
            }
            return p_Text;
        }

        private class URLSpanNoUnderline extends URLSpan {
            public URLSpanNoUnderline(String p_Url) {
                super(p_Url);
            }

            public void updateDrawState(TextPaint p_DrawState) {
                super.updateDrawState(p_DrawState);
                p_DrawState.setUnderlineText(false);
            }
        }

    }

    private class Article {

        public String id;
        public String title;
        public String author;
        public Spannable link;
        public Spanned content;
        public String imageUrl;

        public Article(String id, String imageUrl, String title, String author, Spannable link, Spanned content){
            this.id = id;
            this.imageUrl = imageUrl;
            this.title = title;
            this.author = author;
            this.link = link;
            this.content = content;
        }
    }

}