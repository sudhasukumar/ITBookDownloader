package com.example.sudha.itbookdownloader.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * Created by Sudha on 1/21/2015.
 */
public class ITBDAuthenticatorService extends Service
{
    // Instance field that stores the authenticator object
    private ITBDAuthenticator itbdAuthenticator;

    @Override
    public void onCreate()
    {
        // Create a new authenticator object
        itbdAuthenticator = new ITBDAuthenticator(this);
    }

    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }
}
