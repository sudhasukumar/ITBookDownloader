package com.example.sudha.itbookdownloader.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SyncResult;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import com.example.sudha.itbookdownloader.R;
import com.example.sudha.itbookdownloader.Utility;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

import static com.example.sudha.itbookdownloader.data.ITBookDownloaderContract.BookEntry;

/**
 * Created by Sudha on 1/6/2015.
 */
public class ITBDSyncAdapter extends AbstractThreadedSyncAdapter
{

    public final String LOG_TAG = ITBDSyncAdapter.class.getSimpleName();
    private static final String[] NOTIFY_BOOK_DOWNLOAD_PROJECTION = new String[]{
            BookEntry._ID,
            BookEntry.COLUMN_TITLE,
            BookEntry.COLUMN_SUBTITLE,
            BookEntry.COLUMN_ISBN
    };

    // these indices must match the projection
    private static final int INDEX_BOOK_ID = 0;
    private static final int INDEX_TITLE = 1;
    private static final int INDEX_SUBTITLE = 2;
    private static final int INDEX_ISBN = 3;

    public ITBDSyncAdapter(Context context, boolean autoInitialize)
    {
        super(context, autoInitialize);
    }

    public ITBDSyncAdapter(Context context, boolean autoInitialize, boolean allowParallelSyncs)
    {
        super(context, autoInitialize, allowParallelSyncs);
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult)
    {
        Log.d(LOG_TAG, "Starting sync");
        FetchBookSearchResults();

    }

    private void FetchBookSearchResults()
    {
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        StringBuffer buffer = new StringBuffer();

        Uri.Builder searchQueryUri = Uri.parse("http://it-ebooks-api.info/v1/search/").buildUpon();
        //String locationQuery = Utility.getPreferredLocation(getContext());
        String searchQuery = Utility.getPreferredLocation(getContext());
        searchQueryUri.appendPath(searchQuery);
        String searchURL = searchQueryUri.toString();
        URL weatherTaskUrl = null;
        try
        {
            weatherTaskUrl = new URL(searchURL);
            urlConnection = (HttpURLConnection) weatherTaskUrl.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();
            InputStream inputStream = urlConnection.getInputStream();

            if (inputStream != null)
            {
                reader = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                while ((line = reader.readLine()) != null)
                {
                    buffer.append(line);
                    buffer.append("\n");
                }
            }

            if (buffer.length() != 0)
            {
                //WeatherDataHolder.setWeatherDataFromApiCall(buffer.toString());
                //SearchBooksArrayList = new SearchBooksDataParser().getWeatherListDataFromJson();
                Log.d(LOG_TAG, "buffer.toString() : " + buffer.toString());

            }
        }
        catch (MalformedURLException e)
        {
            e.printStackTrace();
        }
        catch (ProtocolException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        finally
        {
            if (urlConnection != null)
                urlConnection.disconnect();

            if (reader != null)
            {
                try
                {
                    reader.close();
                } catch (final IOException e)
                {
                    Log.e(LOG_TAG, "Error closing stream", e);
                }
            }
        }
    }

    //Helper method to have the sync adapter sync immediately.... @param context The context used to access the account service
    public static void syncImmediately(Context context)
    {
        Bundle bundle = new Bundle();
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        bundle.putString("SearchQuery","");
        ContentResolver.requestSync(getSyncAccount(context), context.getString(R.string.content_authority), bundle);
    }

    /**
     * Helper method to get the fake account to be used with SyncAdapter, or make a new one
     * if the fake account doesn't exist yet.  If we make a new account, we call the
     * onAccountCreated method so we can initialize things.
     *
     * @param context The context used to access the account service
     * @return a fake account.
     */
    public static Account getSyncAccount(Context context)
    {
        // Get an instance of the Android account manager
        AccountManager accountManager = (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);

        // Create the account type and default account
        Account newAccount = new Account(context.getString(R.string.app_name), context.getString(R.string.sync_account_type));

        // If the password doesn't exist, the account doesn't exist
        if (null == accountManager.getPassword(newAccount))
        {

        /*
         * Add the account and account type, no password or user data
         * If successful, return the Account object, otherwise report an error.
         */
            if (!accountManager.addAccountExplicitly(newAccount, "", null))
            {
                return null;
            }
            /*
             * If you don't set android:syncable="true" in
             * in your <provider> element in the manifest,
             * then call ContentResolver.setIsSyncable(account, AUTHORITY, 1)
             * here.
             */

            onAccountCreated(newAccount, context);
        }
        return newAccount;
    }


    private static void onAccountCreated(Account newAccount, Context context)
    {
        /*
         * Since we've created an account
         */
        //ITBDSyncAdapter.configurePeriodicSync(context, SYNC_INTERVAL, SYNC_FLEXTIME);

        /*
         * Without calling setSyncAutomatically, our periodic sync will not be enabled.
         */
        //ContentResolver.setSyncAutomatically(newAccount, context.getString(R.string.content_authority), true);

        /*
         * Finally, let's do a sync to get things started
         */
        syncImmediately(context);
    }

    public static void initializeSyncAdapter(Context context)
    {
        getSyncAccount(context);
    }

}
