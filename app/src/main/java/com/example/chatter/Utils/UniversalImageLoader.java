package com.example.chatter.Utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.example.chatter.R;
import com.nostra13.universalimageloader.cache.memory.impl.WeakMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

public class UniversalImageLoader {

    // properties
    private Context context;

    // constructor
    public UniversalImageLoader(Context context) {
        this.context = context;
    }

    // methods

    public ImageLoaderConfiguration getConfiguration() {

        DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
                .showImageForEmptyUri(R.drawable.icon_circle_2)
                .showImageOnFail(R.drawable.icon_circle_2)
                .resetViewBeforeLoading(false)
                .delayBeforeLoading(1000)
                .cacheInMemory(true)
                .cacheOnDisk(true)
        		.considerExifParams(false)
                .imageScaleType(ImageScaleType.EXACTLY)
                .handler(new Handler())
                .build();


        return new ImageLoaderConfiguration.Builder(context)
                .defaultDisplayImageOptions(defaultOptions)
                .memoryCache(new WeakMemoryCache())
                .diskCacheSize(100*1024*1024)
                .build();
    }

    public static void setImage(String imgURL, ImageView imageView, final ProgressBar progressBar) {

        if(progressBar!=null)
            progressBar.setVisibility(View.VISIBLE);

        if (!imgURL.equals("")) {

            ImageLoader.getInstance().displayImage(imgURL, imageView, new ImageLoadingListener() {
                @Override
                public void onLoadingStarted(String imageUri, View view) {
                    if(progressBar!=null)
                        progressBar.setVisibility(View.VISIBLE);
                }

                @Override
                public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                    if(progressBar!=null)
                        progressBar.setVisibility(View.GONE);
                }

                @Override
                public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                    if(progressBar!=null)
                        progressBar.setVisibility(View.GONE);
                }

                @Override
                public void onLoadingCancelled(String imageUri, View view) {
                    if(progressBar!=null)
                        progressBar.setVisibility(View.GONE);
                }
            });
        }

        else {
            progressBar.setVisibility(View.GONE);
        }
    }
}
