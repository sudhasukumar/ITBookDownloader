package com.example.sudha.itbookdownloader.volley;

import android.content.Context;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;

/**
 * Created by Sudha on 2/7/2015.
 */
public class MyVolley
{
    private static RequestQueue mRequestQueue;
    private static ImageLoader  mImageLoader;


    private MyVolley()
    {
        // no instances
    }


    public static void init(Context context)
    {
        mRequestQueue = Volley.newRequestQueue(context);
        //mImageLoader = new ImageLoader(mRequestQueue, new LruBitmapCache(getLruCacheSize(context)));
        mImageLoader = new ImageLoader(mRequestQueue, new LruBitmapCache(20));
    }


    public static RequestQueue getRequestQueue()
    {
        if ( mRequestQueue != null )
        {
            return mRequestQueue;
        }
        else
        {
            throw new IllegalStateException("RequestQueue not initialized");
        }
    }

    public static ImageLoader getImageLoader()
    {
        if ( mImageLoader != null )
        {
            return mImageLoader;
        }
        else
        {
            throw new IllegalStateException("ImageLoader not initialized");
        }
    }

}

