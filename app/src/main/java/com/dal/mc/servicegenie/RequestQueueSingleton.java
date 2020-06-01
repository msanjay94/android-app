package com.dal.mc.servicegenie;

/*
    Title: RequestQueueSingleton
    Author: Bola Okesanjo, Archanaapriya Nallasivan & Deepan Shankar
    Date: 2019
    Code version: 1.0
    Availability: CSCI5708-CurrencyApp_Fall2019 [Slide 28],
                  https://dal.brightspace.com/d2l/le/content/100143/viewContent/1488863/View?ou=100143
 */

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

public class RequestQueueSingleton {

    private static RequestQueueSingleton mInstance;
    private RequestQueue mRequestQueue;
    private Context mCtx;

    private RequestQueueSingleton(Context context) {
        mCtx = context.getApplicationContext();
        mRequestQueue = getRequestQueue();
    }

    public static synchronized RequestQueueSingleton getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new RequestQueueSingleton(context.getApplicationContext());
        }
        return mInstance;
    }

    public RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            mRequestQueue = Volley.newRequestQueue(mCtx.getApplicationContext());
        }
        return mRequestQueue;
    }

    public <T> void addToRequestQueue(Request<T> req) {
        getRequestQueue().add(req);
    }
}
