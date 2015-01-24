package com.example.sudha.itbookdownloader;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ArrayAdapter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Sudha on 1/19/2015.
 */
public class FetchBooksForSearchQueryTask extends AsyncTask<String, Void, List<HashMap<String, String>>>
{
    public static final String LOG_TAG = FetchBooksForSearchQueryTask.class.getSimpleName();
    public FetchBooksForSearchQueryListener asyncResponseDelegate = null;
    private ArrayAdapter<String> forecastAdapter;
    private final Context context;
    List<HashMap<String,String>> SearchBooksArrayList;

    public FetchBooksForSearchQueryTask(Context mContext, ArrayAdapter<String> mForecastAdapter)
    {
        this.context = mContext;
        this.forecastAdapter = mForecastAdapter;
    }


    @Override
    protected List<HashMap<String, String>> doInBackground(String... params)
    {
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        StringBuffer buffer = new StringBuffer();

        Uri.Builder searchQueryUri = Uri.parse("http://it-ebooks-api.info/v1/search/").buildUpon();
        String searchQuery = params[0];
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
        return SearchBooksArrayList;
    }

    @Override
    protected void onPostExecute(List<HashMap<String, String>> BooksSearchResultArrayList)
    {
        //super.onPostExecute();
        asyncResponseDelegate.onFetchBooksForSearchQuery(BooksSearchResultArrayList);
    }
}
