package com.example.sudha.itbookdownloader.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

// Created by Sudha on 1/21/2015.
public class ITBDAuthenticatorService extends Service
{
    // Instance field that stores the authenticator object
    private ITBDAuthenticator itbdAuthenticator;
    public final String LOG_TAG = ITBDAuthenticatorService.class.getSimpleName();

    @Override
    public void onCreate()
    {
        //Log.d(LOG_TAG, "ITBDAuthenticatorService created");
        itbdAuthenticator = new ITBDAuthenticator(this); // Create a new authenticator object
    }

    @Override
    public void onDestroy()
    {
        //Log.d(LOG_TAG, "ITBDAuthenticatorService destroyed");
    }
    @Override //When the system binds to this Service to make the RPC call return the authenticator's IBinder.
    public IBinder onBind(Intent intent)
    {
        //Log.d(LOG_TAG, "ITBDAuthenticatorService onBind");
        return null;
    }
}
