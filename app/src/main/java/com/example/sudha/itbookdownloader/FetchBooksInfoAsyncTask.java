package com.example.sudha.itbookdownloader;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

/**
 * Created by Sudha on 1/19/2015.
 */
public class FetchBooksInfoAsyncTask extends AsyncTask<String, Void, String>
{
    public static final String LOG_TAG = FetchBooksInfoAsyncTask.class.getSimpleName();
    public static final int SEARCH_QUERY = 0;
    public static final int ISBN = 1;
    public static final int BOOK_ID = 2;
    public  FetchBooksForSearchQueryListener asyncResponseDelegate = null;
    private Context                          context               = null;

    public FetchBooksInfoAsyncTask(Context mContext)
    {
        this.context = mContext;
        //this.asyncResponseDelegate = mAsyncResponseDelegate;
    }

    @Override
    protected String doInBackground(String... params)
    {
        Utility utility = new Utility(context);
        try
        {
            utility.prepareInputForAsyncTask(params[SEARCH_QUERY], params[ISBN], params[BOOK_ID]);
        }
        catch ( Exception e )
        {
            //Log.d(LOG_TAG, " FetchBooksInfoAsyncTask doInBackground catch exception : " + e.getMessage());
            e.printStackTrace();
        }

        return "FetchBooksInfoAsyncTask Complete";
    }

    @Override
    protected void onPostExecute(String result)
    {
        //Log.d(LOG_TAG, "FetchBooksInfoAsyncTask onPostExecute : " + result);
        asyncResponseDelegate.onFetchBooksForSearchQuery("Data Changed in Content Provider : ");
    }

}
