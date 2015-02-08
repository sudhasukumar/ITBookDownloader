package com.example.sudha.itbookdownloader.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

/**
 * Created by Sudha on 1/24/2015.
 */
public class ITBDSyncService extends Service
{
    private static final String LOG_TAG = ITBDSyncService.class.getSimpleName();
    private static final Object sSyncAdapterLock = new Object();
    private static ITBDSyncAdapter itbdSyncAdapter = null;

    @Override
    public void onCreate()
    {
        //Log.d(LOG_TAG, "ITBDSyncService created");
        synchronized (sSyncAdapterLock)
        {
            if (itbdSyncAdapter == null)
            {
                itbdSyncAdapter = new ITBDSyncAdapter(getApplicationContext(), true);
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent)
    {
        //Log.d(LOG_TAG, "ITBDSyncService onBind");
        return itbdSyncAdapter.getSyncAdapterBinder();
    }
}
