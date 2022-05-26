package com.example.forummpt;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.widget.ImageView;

import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import java.net.MalformedURLException;
import java.net.URL;

public class GlideLoad {

    public void glideUpload(Context context, String url, ImageView imageView) throws MalformedURLException {
        URL newUrl = new URL(url);
        Glide.with(context).load(newUrl).encodeFormat(Bitmap.CompressFormat.JPEG).listener(requestListener).into(imageView);
    }

    private RequestListener<Drawable> requestListener = new RequestListener<Drawable>() {
        @Override
        public boolean onLoadFailed(@Nullable GlideException e,
                                    Object model, Target<Drawable> target, boolean isFirstResource) {
            Log.d("glide-error", e.toString());
            return false;
        }

        @Override
        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {

            return false;
        }
    };

}
