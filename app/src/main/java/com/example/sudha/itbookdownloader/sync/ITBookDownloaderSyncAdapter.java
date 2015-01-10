package com.example.sudha.itbookdownloader.sync;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.SyncResult;
import android.os.Bundle;

/**
 * Created by Sudha on 1/6/2015.
 */
public class ITBookDownloaderSyncAdapter extends AbstractThreadedSyncAdapter
{
    public final String LOG_TAG = ITBookDownloaderSyncAdapter.class.getSimpleName();

    public ITBookDownloaderSyncAdapter(Context context, boolean autoInitialize)
    {
        super(context, autoInitialize);
    }

    public ITBookDownloaderSyncAdapter( Context context, boolean autoInitialize, boolean allowParallelSyncs)
    {
        super(context, autoInitialize, allowParallelSyncs);
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult)
    {

    }
}
