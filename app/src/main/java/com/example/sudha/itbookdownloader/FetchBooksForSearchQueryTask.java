package com.example.sudha.itbookdownloader;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.concurrent.CopyOnWriteArrayList;

import static com.example.sudha.itbookdownloader.data.ITBookDownloaderContract.AuthorEntry;
import static com.example.sudha.itbookdownloader.data.ITBookDownloaderContract.BookEntry;

/**
 * Created by Sudha on 1/19/2015.
 */
public class FetchBooksForSearchQueryTask extends AsyncTask<String, Void, String>
{
    public static final String LOG_TAG = FetchBooksForSearchQueryTask.class.getSimpleName();

    public FetchBooksForSearchQueryListener asyncResponseDelegate = null;
    //private ITBDBookSearchAdapter itbdBookSearchAdapter;
    private final Context context;
    //List<HashMap<String,String>> SearchBooksArrayList;
    private String SearchQuery;
    private String BookId;
    private static final int RESULTS_PER_PAGE = 10;

    public FetchBooksForSearchQueryTask(Context mContext)
    {
        this.context = mContext;
        //this.itbdBookSearchAdapter = mITBDBookSearchAdapter;
    }


    @Override
    protected String doInBackground(String... params)
    {
        SearchQuery = params[0];
        BookId = params[1];
        if (SearchQuery == null)
            SearchQuery = "";
        else if (SearchQuery.isEmpty())
            SearchQuery = context.getString(R.string.search_query_string_default);
        if (BookId == null)
            BookId = "0";
        else if (BookId.isEmpty())
            BookId = context.getString(R.string.book_id_default);

        fetchDataParseJSONStoreData(SearchQuery,BookId);/*
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        StringBuffer buffer = new StringBuffer();

        Uri.Builder searchQueryUri = Uri.parse("http://it-ebooks-api.info/v1/search/").buildUpon();
        //String SearchQuery = params[0];
        searchQueryUri.appendPath(SearchQuery);
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
        return SearchBooksArrayList;*/
        return "";
    }

    @Override
    protected void onPostExecute(String result)
    {
        //super.onPostExecute();
        asyncResponseDelegate.onFetchBooksForSearchQuery("Data Changed in Content Provider : Update CursorAdaptor");
    }

    private void fetchDataParseJSONStoreData(String mSearchQuery, String mBookId)
    {
        CopyOnWriteArrayList<ContentValues> ContentValueArrayList;
        String BookSearchListJSONString;
        Log.d(LOG_TAG, "Starting fetchDataParseJSONStoreData");
        //get SearchQuery or BookId from extras Bundle // BookId is cast to long for ease of method over loading to differentiate web api url formats
        String SearchQuery = mSearchQuery;
        long BookId = Long.parseLong(mBookId);

        if (BookId == 0) //If its a SearchQuery
        {
            if (SearchQuery == null) //If its a default initialisation...SearchQuery is set to "android" as default.....otherwise use User Supplied Search Query
            {
                SearchQuery = context.getString(R.string.search_query_string_default);
            }
            BookSearchListJSONString = FetchBookSearchResults(SearchQuery); //Fetch Book Search List for Search Query
            Log.d(LOG_TAG, "BookSearchListJSONString for SearchQuery : " + SearchQuery + " *** " + BookSearchListJSONString);
            if (BookSearchListJSONString.length() != 0) //When you have a list from Web Api Call
            {
                ContentValueArrayList = getBookSearchListDataFromJson(BookSearchListJSONString, SearchQuery); //Get the Content Values from JSON
                Log.d(LOG_TAG, "BulkInsert initiated for SearchQuery : " + SearchQuery);
                if (!ContentValueArrayList.isEmpty())
                    storeDataInITBDProvider(ContentValueArrayList); // Initiate Bulk Insert into Books table
            }
        }
        else // If its a BookId Search
        {
            BookSearchListJSONString = FetchBookSearchResults(BookId);
            // Check if the BookId is present in the Books table...It should be there since the search with BookId originated from that info
            Cursor AuthorCursor = context.getContentResolver().query(AuthorEntry.buildAuthorBookIdUri(BookId),  new String[]{BookEntry._ID}, null, null, null);
            if (AuthorCursor.moveToFirst()) // Assuming the Book Id is present when the cursor moves to first row
            {
                int BookIdIndex = AuthorCursor.getColumnIndex(BookEntry._ID);
                long LongBookIdFromJson = AuthorCursor.getLong(BookIdIndex);
                if (BookSearchListJSONString.length() != 0)//BookId in Books Table ...Now Fetch Author data
                {
                    ContentValues AuthorValues = getBookIdAuthorDataFromJson(BookSearchListJSONString); //...get Authors Info from JSON
                    Log.d(LOG_TAG, "Author Insert initiated for BookId : " + BookId);
                    if (AuthorValues.size() != 0)
                        storeDataInITBDProvider(AuthorValues,LongBookIdFromJson); //Insert the JSON info into Authors Table.
                }
                else //Book Id is present in Books Table But web Api call doesnt return anything
                {
                    Log.d(LOG_TAG, "BookSearchListJSONString is empty for BookId : " + BookId);
                }
            }
            else // The Book Id cannot be found in the Books table
            {
                if (BookSearchListJSONString.length() != 0) //...But may be found in the web api call
                {
                    // Then get Book and Author CV from Json
                    ContentValues BookInfoValues = getBookIdBookDataFromJson(BookSearchListJSONString); //...get Book Info from JSON
                    ContentValues AuthorValues = getBookIdAuthorDataFromJson(BookSearchListJSONString); //...get Authors Info from JSON
                    //If Cvs are not empty then insert both Book and Author Info for Book Id in DB
                    if ((BookInfoValues.size() != 0)&&(AuthorValues.size() != 0))
                    {
                        storeDataInITBDProvider(BookInfoValues,BookId);
                        storeDataInITBDProvider(AuthorValues,BookId);
                    }

                }
                else //Book Id not found in Books Table and Web Api doesnt return any thing.
                {
                    //Need not implement this for now because BookId is considered Private data and when the execution comes to this point means the origin of BookId is suspicious.
                    //There is no data to proceed further with that Book Id. Just Log the ID for debugging
                    Log.d(LOG_TAG, "There is no such BookId in the Books Table and Web Api Call for : " + BookId);
                }


            }

        }

        Log.d(LOG_TAG, "Completed fetchDataParseJSONStoreData");
    }

    private ContentValues getBookIdBookDataFromJson(String bookInfoDataJSONString)
    {
        //JSON fields to extract info from JSON String
        final String JSON_ERROR = "Error";
        final String JSON_ZERO_SUCCESS_CODE = "0";
        final String JSON_ID = "ID";
        final String JSON_TITLE = "Title";
        final String JSON_SUBTITLE = "SubTitle";
        final String JSON_DESCRIPTION = "Description";
        final String JSON_IMAGELINK = "Image";
        final String JSON_AUTHOR_ISBN = "ISBN"; //final String JSON_ISBN = "isbn"; The BookSearch brings isbn while Book Id search brings ISBN...Web Api Fault

        ContentValues BookInfoValues = new ContentValues();
        JSONObject BookInfoDataJsonObject;
        try
        {
            BookInfoDataJsonObject = new JSONObject(bookInfoDataJSONString);
            String JSONError = BookInfoDataJsonObject.getString(JSON_ERROR);
            if (JSONError.equalsIgnoreCase(JSON_ZERO_SUCCESS_CODE)) //"Error": "0"
            {
                long BookId = Long.parseLong(getValueFromJson(BookInfoDataJsonObject,JSON_ID));
                BookInfoValues.put(BookEntry.COLUMN_BOOK_ID,BookId);

                String Title = getValueFromJson(BookInfoDataJsonObject, JSON_TITLE);
                BookInfoValues.put(BookEntry.COLUMN_TITLE,Title);

                String Subtitle = getValueFromJson(BookInfoDataJsonObject, JSON_SUBTITLE);
                BookInfoValues.put(BookEntry.COLUMN_SUBTITLE,Subtitle);

                String Description = getValueFromJson(BookInfoDataJsonObject, JSON_DESCRIPTION);
                BookInfoValues.put(BookEntry.COLUMN_DESCRIPTION,Description);

                long ISBN = Long.parseLong(getValueFromJson(BookInfoDataJsonObject,JSON_AUTHOR_ISBN));
                BookInfoValues.put(BookEntry.COLUMN_ISBN,ISBN);

                String ImageLink = getValueFromJson(BookInfoDataJsonObject, JSON_IMAGELINK);
                BookInfoValues.put(BookEntry.COLUMN_IMAGE_LINK,ImageLink);

                //If the Book is stored based on ID in the Books table then default the Search Query column to Book Title
                BookInfoValues.put(BookEntry.COLUMN_BOOK_SEARCH_QUERY,Title);
            }
            else //{"Error":"Book not found!"}
            {
                Log.d(LOG_TAG, " getBookIdBookDataFromJson Error From Web Api Call : " + JSONError);
            }
        }
        catch (JSONException e)
        {
            Log.d(LOG_TAG, "getBookIdBookDataFromJson JSON Parsing Error : " + e.getMessage());
            e.printStackTrace();
        }
        return BookInfoValues;
    }


    private ContentValues getBookIdAuthorDataFromJson(String bookIdAuthorJSONString)
    {
        final String JSON_ERROR = "Error";
        final String JSON_ZERO_SUCCESS_CODE = "0";
        final String JSON_ID = "ID";
        final String JSON_AUTHOR = "Author";
        final String JSON_YEAR = "Year";
        final String JSON_PAGE = "Page";
        final String JSON_PUBLISHER = "Publisher";
        final String JSON_DOWNLOADLINK = "isbn";
        ContentValues AuthorValues = new ContentValues();
        JSONObject BookIdJsonObject;
        try
        {
            BookIdJsonObject = new JSONObject(bookIdAuthorJSONString);
            String JSONError = BookIdJsonObject.getString(JSON_ERROR);
            AuthorValues.clear();
            if (JSONError.equalsIgnoreCase(JSON_ZERO_SUCCESS_CODE)) //"Error": "0"
            {
                long BookId = Long.parseLong(getValueFromJson(BookIdJsonObject,JSON_ID));
                AuthorValues.put(AuthorEntry.COLUMN_BOOK_ID, BookId);

                String AuthorName = getValueFromJson(BookIdJsonObject,JSON_AUTHOR);
                AuthorValues.put(AuthorEntry.COLUMN_AUTHORNAME, AuthorName);

                long Year = Long.parseLong(getValueFromJson(BookIdJsonObject,JSON_YEAR));
                AuthorValues.put(AuthorEntry.COLUMN_YEAR, Year);

                long Page = Long.parseLong(getValueFromJson(BookIdJsonObject,JSON_PAGE));
                AuthorValues.put(AuthorEntry.COLUMN_PAGE, Page);

                String Publisher = getValueFromJson(BookIdJsonObject,JSON_PUBLISHER);
                AuthorValues.put(AuthorEntry.COLUMN_PUBLISHER, Publisher);

                String DownloadLink = getValueFromJson(BookIdJsonObject,JSON_DOWNLOADLINK);
                AuthorValues.put(AuthorEntry.COLUMN_DOWNLOAD_LINK, DownloadLink);

                AuthorValues.put(AuthorEntry.COLUMN_FILE_PATHNAME, "file Path name TBD");
            }
            else //{"Error":"Book not found!"}
            {
                Log.d(LOG_TAG, " getBookIdAuthorDataFromJson Error From Web Api Call : " + JSONError);
            }
        }
        catch (JSONException e)
        {
            Log.d(LOG_TAG, "getBookSearchListDataFromJson JSON Parsing Error : " + e.getMessage());
            e.printStackTrace();
        }
        return AuthorValues;

    }

    private CopyOnWriteArrayList<ContentValues> getBookSearchListDataFromJson(String mBookSearchListJSONString, String mSearchQuery)
    {
        JSONArray BooksArray;
        //JSON fields to extract info from JSON String
        final String JSON_ERROR = "Error";
        final String JSON_ZERO_SUCCESS_CODE = "0";
        final String JSON_BOOKS = "Books";
        final String JSON_ID = "ID";
        final String JSON_TITLE = "Title";
        final String JSON_SUBTITLE = "SubTitle";
        final String JSON_DESCRIPTION = "Description";
        final String JSON_IMAGELINK = "Image";
        final String JSON_ISBN = "isbn";

        CopyOnWriteArrayList<ContentValues> mContentValueArrayList = new CopyOnWriteArrayList<>(); // size = RESULTS_PER_PAGE
        JSONObject BookSearchListJsonObject;
        try
        {
            BookSearchListJsonObject = new JSONObject(mBookSearchListJSONString);
            String JSONError = BookSearchListJsonObject.getString(JSON_ERROR);
            if (JSONError.equalsIgnoreCase(JSON_ZERO_SUCCESS_CODE)) //"Error": "0"
            {
                mContentValueArrayList.clear();
                ContentValues BookInfoValues;

                BooksArray = BookSearchListJsonObject.getJSONArray(JSON_BOOKS);
                Log.d(LOG_TAG, " BooksArray Length : " + BooksArray.length() + " BooksArray : " + BooksArray.toString());

                for (int i = 0; i < BooksArray.length() ; i++)
                {
                    BookInfoValues = new ContentValues();
                    BookInfoValues.clear();

                    try
                    {
                        JSONObject BookInfoJsonObject = BooksArray.getJSONObject(i);
                        long BookId = Long.parseLong(getValueFromJson(BookInfoJsonObject,JSON_ID));
                        BookInfoValues.put(BookEntry.COLUMN_BOOK_ID,BookId);
                        Log.d(LOG_TAG, "*** BookId : " + BookId);

                        String Title = getValueFromJson(BookInfoJsonObject, JSON_TITLE);
                        BookInfoValues.put(BookEntry.COLUMN_TITLE,Title);

                        String Subtitle = getValueFromJson(BookInfoJsonObject, JSON_SUBTITLE);
                        BookInfoValues.put(BookEntry.COLUMN_SUBTITLE,Subtitle);
                        Log.d(LOG_TAG, " Subtitle : " + Subtitle);

                        String Description = getValueFromJson(BookInfoJsonObject, JSON_DESCRIPTION);
                        BookInfoValues.put(BookEntry.COLUMN_DESCRIPTION,Description);
                        Log.d(LOG_TAG, " Description : " + Description);

                        long ISBN = Long.parseLong(getValueFromJson(BookInfoJsonObject,JSON_ISBN));
                        BookInfoValues.put(BookEntry.COLUMN_ISBN,ISBN);
                        Log.d(LOG_TAG, " ISBN : " + ISBN);

                        String ImageLink = getValueFromJson(BookInfoJsonObject, JSON_IMAGELINK);
                        BookInfoValues.put(BookEntry.COLUMN_IMAGE_LINK,ImageLink);
                        Log.d(LOG_TAG, " ImageLink : " + ImageLink);

                        BookInfoValues.put(BookEntry.COLUMN_BOOK_SEARCH_QUERY,mSearchQuery);
                        Log.d(LOG_TAG, " mSearchQuery : " + mSearchQuery);
                    }
                    catch (JSONException e)
                    {
                        Log.d(LOG_TAG, " getBookSearchListDataFromJson For Loop Error From Web Api Call : " + e.getMessage());
                        e.printStackTrace();
                    }
                    catch (NumberFormatException e)
                    {
                        Log.d(LOG_TAG, " getBookSearchListDataFromJson For Loop Error From Web Api Call : " + e.getMessage());
                        e.printStackTrace();
                    }
                    mContentValueArrayList.add(i,BookInfoValues);
                    Log.d(LOG_TAG, " BookInfoValues : " + BookInfoValues.toString());
                }
            }
            else //{"Error":"Book not found!"}
            {
                Log.d(LOG_TAG, " getBookSearchListDataFromJson Error From Web Api Call : " + JSONError);
            }

        }
        catch (JSONException e)
        {
            Log.d(LOG_TAG, "getBookSearchListDataFromJson JSON Parsing Error : " + e.getMessage());
            e.printStackTrace();
        }
        return mContentValueArrayList;

    }

    private static String getValueFromJson(JSONObject bookInfoJsonObject, String mKey) throws JSONException
    {
        String mValue = "";
        try
        {
            mValue = bookInfoJsonObject.getString(mKey);
        }
        catch (JSONException e)
        {
            Log.d(LOG_TAG, " getValueFromJson : " + e.getMessage());
            e.printStackTrace();
        }
        Log.d(LOG_TAG, " getValueFromJson : " + mKey + " : " + mValue);
        return mValue;
    }

    private void storeDataInITBDProvider(CopyOnWriteArrayList<ContentValues> contentValueArrayList) // This method is overloaded
    {
        ContentValues[] myCV = new ContentValues[RESULTS_PER_PAGE];
        //before doing a bulk insert delete all records in both tables Books and Author.
        context.getContentResolver().delete(BookEntry.BOOKS_CONTENT_URI,null,null);
        context.getContentResolver().delete(AuthorEntry.AUTHORS_CONTENT_URI,null,null);
        int rowCount = context.getContentResolver().bulkInsert(BookEntry.BOOKS_CONTENT_URI,contentValueArrayList.toArray(myCV));
        Log.d(LOG_TAG, "BulkInsert done for row count : " + rowCount);
    }

    private void storeDataInITBDProvider(ContentValues mContentValues, long longBookId) // This method is overloaded
    {
        Uri AuthorInsertUri = context.getContentResolver().insert(AuthorEntry.buildAuthorBookIdUri(longBookId), mContentValues);
        Log.d(LOG_TAG, "Author Insert complete for URI : " + AuthorInsertUri);
    }

    private String FetchBookSearchResults(long mBookId)
    {
        Uri.Builder bookQueryUri = Uri.parse(context.getString(R.string.api_book_id)).buildUpon();
        //eventually store mBookId in preferences and make it a suggestion for the searchView
        bookQueryUri.appendPath(String.valueOf(mBookId));
        return makeNetworkApiCall(bookQueryUri.toString());
    }

    private String FetchBookSearchResults(String mSearchQuery)
    {
        Uri.Builder searchQueryUri = Uri.parse(context.getString(R.string.api_book_search)).buildUpon();
        //String searchQuery = Utility.getPreferredLocation(getContext()); //eventually store it in preferences and make it a suggestion for the searchView
        searchQueryUri.appendPath(mSearchQuery);
        return makeNetworkApiCall(searchQueryUri.toString());
    }

    private String makeNetworkApiCall(String ITEbooksInfoUrl)
    {
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        StringBuffer buffer = new StringBuffer();
        URL fetchBookTaskUrl;
        try
        {
            fetchBookTaskUrl = new URL(ITEbooksInfoUrl);
            urlConnection = (HttpURLConnection) fetchBookTaskUrl.openConnection();
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
                Log.d(LOG_TAG, "buffer.toString() : " + buffer.toString());
            }
        }
        catch (MalformedURLException | ProtocolException e)
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
        return buffer.toString();
    }
}
