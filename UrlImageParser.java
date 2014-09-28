package com.once.zhou.once;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.text.Html.ImageGetter;
import android.widget.TextView;

import java.net.URL;

public class UrlImageParser implements ImageGetter {
    TextView textView;
    Context context;
    public UrlImageParser(TextView textView, Context contxt) {
        this.context = contxt;
        this.textView = textView;
    }

    @Override
    public Drawable getDrawable(String paramString) {
        URLDrawable urlDrawable = new URLDrawable(context);

        ImageGetterAsyncTask getterTask = new ImageGetterAsyncTask(urlDrawable);
        getterTask.execute(paramString);
        return urlDrawable;
    }



    public class ImageGetterAsyncTask extends AsyncTask<String, Void, Drawable> {
        URLDrawable urlDrawable;

        public ImageGetterAsyncTask(URLDrawable drawable) {
            this.urlDrawable = drawable;
        }

        @Override
        protected void onPostExecute(Drawable result) {
            if (result != null) {

                this.urlDrawable.drawable = result;

                // redraw the image by invalidating the container
                textView.invalidate();

            }
        }

        @Override
        protected Drawable doInBackground(String... params) {
            String source = params[0];
            return fetchDrawable(source);
        }

        public Drawable fetchDrawable(String url) {
            BitmapDrawable drawable = null;
            URL Url;
            try {
                Url = new URL(url);
                drawable = (BitmapDrawable)Drawable.createFromStream(Url.openStream(), "");
                //do the math to calculate the new bounds
                Rect bound = getDefaultImageBounds();
                int width = drawable.getIntrinsicWidth();
                int height = drawable.getIntrinsicHeight();
                int new_width = bound.width();
                int new_height = bound.height();
                if (width >= height) {
                    new_height = (int) ((double) new_width / (double) width * (double) height);
                }
                else{
                    new_width = (int) ((double) new_height / (double) height * (double) width);
                }
                int new_l = (int) ((double)(bound.width() - new_width) / 2);
                int new_t = (int) ((double)(bound.height() - new_height) / 2);
                int new_r = (int) ((double)(bound.width() + new_width) / 2);
                int new_b = (int) ((double)(bound.height() + new_height) / 2);

                Rect bounds = new Rect(new_l, new_t, new_r, new_b);
                drawable.setBounds(bounds);
                return drawable;

            } catch (Exception e) {
                return null;
            }

        }

    }

    //default 4:3
    @SuppressWarnings("deprecation")
    public Rect getDefaultImageBounds() {
        //Display display = ((Activity)context).getWindowManager().getDefaultDisplay();
        int width = textView.getWidth();
        int height = (int) (width * 3 / 4);

        Rect bounds = new Rect(0, 0, width, height);
        return bounds;
    }

    public class URLDrawable extends BitmapDrawable {
        protected Drawable drawable;

        @SuppressWarnings("deprecation")
        public URLDrawable(Context context)
        {
            this.setBounds(getDefaultImageBounds());
            //draw the default loading image(@todo could be an animation in the future?)
            drawable = context.getResources().getDrawable(R.drawable.loading);
            drawable.setBounds(getDefaultImageBounds());
        }

        @Override
        public void draw(Canvas canvas) {
            if (drawable != null) {
                drawable.draw(canvas);
            }
        }

    }
}