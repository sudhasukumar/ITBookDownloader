package com.example.sudha.itbookdownloader.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.SyncResult;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import com.example.sudha.itbookdownloader.R;

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
 * Created by Sudha on 1/6/2015.
 */
public class ITBDSyncAdapter extends AbstractThreadedSyncAdapter
{
    // Global variables
    public final String LOG_TAG = ITBDSyncAdapter.class.getSimpleName();
    ContentResolver itbdContentResolver;

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

    private static final int RESULTS_PER_PAGE = 10;

    public ITBDSyncAdapter(Context context, boolean autoInitialize)
    {
        super(context, autoInitialize);
        itbdContentResolver = context.getContentResolver();
    }

    public ITBDSyncAdapter(Context context, boolean autoInitialize, boolean allowParallelSyncs)
    {
        super(context, autoInitialize, allowParallelSyncs);
        itbdContentResolver = context.getContentResolver();
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult)
    {
        CopyOnWriteArrayList<ContentValues> ContentValueArrayList;
        String BookSearchListJSONString;
        //Log.d(LOG_TAG, "Starting sync");
        //get SearchQuery or BookId from extras Bundle // BookId is cast to long for ease of method over loading to differentiate web api url formats
        String SearchQuery = extras.getString(getContext().getString(R.string.search_query_label));
        long BookId = Long.parseLong(extras.getString(getContext().getString(R.string.book_id_label)));

        if (BookId == 0) //If its a SearchQuery
        {
            if (SearchQuery == null) //If its a default initialisation...SearchQuery is set to "android" as default.....otherwise use User Supplied Search Query
            {
                SearchQuery = getContext().getString(R.string.search_query_string_default);
            }
            BookSearchListJSONString = FetchBookSearchResults(SearchQuery); //Fetch Book Search List for Search Query
            //Log.d(LOG_TAG, "BookSearchListJSONString for SearchQuery : " + SearchQuery + " *** " + BookSearchListJSONString);
            if (BookSearchListJSONString.length() != 0) //When you have a list from Web Api Call
            {
                ContentValueArrayList = getBookSearchListDataFromJson(BookSearchListJSONString, SearchQuery); //Get the Content Values from JSON
                //Log.d(LOG_TAG, "BulkInsert initiated for SearchQuery : " + SearchQuery);
                if (!ContentValueArrayList.isEmpty())
                    storeDataInITBDProvider(ContentValueArrayList); // Initiate Bulk Insert into Books table
            }
        }
        else // If its a BookId Search
        {
            BookSearchListJSONString = FetchBookSearchResults(BookId);
            // Check if the BookId is present in the Books table...It should be there since the search with BookId originated from that info
            Cursor AuthorCursor = getContext().getContentResolver().query(AuthorEntry.buildAuthorsBookIdUri(BookId),  new String[]{BookEntry._ID}, null, null, null);
            if (AuthorCursor.moveToFirst()) // Assuming the Book Id is present when the cursor moves to first row
            {
                int BookIdIndex = AuthorCursor.getColumnIndex(BookEntry._ID);
                long LongBookIdFromJson = AuthorCursor.getLong(BookIdIndex);
                if (BookSearchListJSONString.length() != 0)//BookId in Books Table ...Now Fetch Author data
                {
                    ContentValues AuthorValues = getBookIdAuthorDataFromJson(BookSearchListJSONString); //...get Authors Info from JSON
                    //Log.d(LOG_TAG, "Author Insert initiated for BookId : " + BookId);
                    if (AuthorValues.size() != 0)
                        storeDataInITBDProvider(AuthorValues,LongBookIdFromJson); //Insert the JSON info into Authors Table.
                }
                else //Book Id is present in Books Table But web Api call doesnt return anything
                {
                    //Log.d(LOG_TAG, "BookSearchListJSONString is empty for BookId : " + BookId);
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
                    //Log.d(LOG_TAG, "There is no such BookId in the Books Table and Web Api Call for : " + BookId);
                }


            }

        }

        //Log.d(LOG_TAG, "Completed sync");
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
        final String JSON_ISBN = "isbn";
        ContentValues BookInfoValues = new ContentValues();
        JSONObject BookInfoDataJsonObject;
        try
        {
            BookInfoDataJsonObject = new JSONObject(bookInfoDataJSONString);
            String JSONError = BookInfoDataJsonObject.getString(JSON_ERROR);
            if (JSONError.equalsIgnoreCase(JSON_ZERO_SUCCESS_CODE)) //"Error": "0"
            {
                long BookId = Long.parseLong(BookInfoDataJsonObject.getString(JSON_ID));
                BookInfoValues.put(BookEntry.COLUMN_BOOK_ID,BookId);
                String Title = BookInfoDataJsonObject.getString(JSON_TITLE);
                BookInfoValues.put(BookEntry.COLUMN_TITLE,Title);
                String Subtitle = BookInfoDataJsonObject.getString(JSON_SUBTITLE);
                BookInfoValues.put(BookEntry.COLUMN_SUBTITLE,Subtitle);
                String Description = BookInfoDataJsonObject.getString(JSON_DESCRIPTION);
                BookInfoValues.put(BookEntry.COLUMN_DESCRIPTION,Description);
                long ISBN = BookInfoDataJsonObject.getLong(JSON_ISBN);
                BookInfoValues.put(BookEntry.COLUMN_ISBN,ISBN);
                String ImageLink = BookInfoDataJsonObject.getString(JSON_IMAGELINK);
                BookInfoValues.put(BookEntry.COLUMN_IMAGE_LINK,ImageLink);
                //If the Book is stored based on ID in the Books table then default the Search Query column to Book Title
                BookInfoValues.put(BookEntry.COLUMN_BOOK_SEARCH_QUERY,Title);
            }
            else //{"Error":"Book not found!"}
            {
                //Log.d(LOG_TAG, " getBookIdBookDataFromJson Error From Web Api Call : " + JSONError);
            }
        }
        catch (JSONException e)
        {
            //Log.d(LOG_TAG, "getBookIdBookDataFromJson JSON Parsing Error : " + e.getMessage());
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
                long BookId = Long.parseLong(BookIdJsonObject.getString(JSON_ID));
                AuthorValues.put(AuthorEntry.COLUMN_BOOK_ID, BookId);
                String AuthorName = BookIdJsonObject.getString(JSON_AUTHOR);
                AuthorValues.put(AuthorEntry.COLUMN_AUTHORNAME, AuthorName);
                long Year = Long.parseLong(BookIdJsonObject.getString(JSON_YEAR));
                AuthorValues.put(AuthorEntry.COLUMN_YEAR, Year);
                long Page = Long.parseLong(BookIdJsonObject.getString(JSON_PAGE));
                AuthorValues.put(AuthorEntry.COLUMN_PAGE, Page);
                String Publisher = BookIdJsonObject.getString(JSON_PUBLISHER);
                AuthorValues.put(AuthorEntry.COLUMN_PUBLISHER, Publisher);
                String DownloadLink = BookIdJsonObject.getString(JSON_DOWNLOADLINK);
                AuthorValues.put(AuthorEntry.COLUMN_DOWNLOAD_LINK, DownloadLink);
                AuthorValues.put(AuthorEntry.COLUMN_FILE_PATHNAME, "file Path name TBD");
            }
            else //{"Error":"Book not found!"}
            {
                //Log.d(LOG_TAG, " getBookIdAuthorDataFromJson Error From Web Api Call : " + JSONError);
            }
        }
        catch (JSONException e)
        {
            //Log.d(LOG_TAG, "getBookSearchListDataFromJson JSON Parsing Error : " + e.getMessage());
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
                ContentValues BookInfoValues = new ContentValues();
                BooksArray = BookSearchListJsonObject.getJSONArray(JSON_BOOKS);
                //Log.d(LOG_TAG, " BooksArray : " + BooksArray.toString());
                for (int i = 0; i < BooksArray.length(); i++)
                {
                    BookInfoValues.clear();
                    JSONObject BookInfoJsonObject = BooksArray.getJSONObject(i);
                    long BookId = Long.parseLong(BookInfoJsonObject.getString(JSON_ID));
                    BookInfoValues.put(BookEntry.COLUMN_BOOK_ID,BookId);
                    //Log.d(LOG_TAG, "*** BookId : " + BookId);

                    String Title = BookInfoJsonObject.getString(JSON_TITLE);
                    BookInfoValues.put(BookEntry.COLUMN_TITLE,Title);
                    //Log.d(LOG_TAG, " Title : " + Title);

                    String Subtitle = BookInfoJsonObject.getString(JSON_SUBTITLE);
                    BookInfoValues.put(BookEntry.COLUMN_SUBTITLE,Subtitle);
                    //Log.d(LOG_TAG, " Subtitle : " + Subtitle);
                    String Description = BookInfoJsonObject.getString(JSON_DESCRIPTION);
                    BookInfoValues.put(BookEntry.COLUMN_DESCRIPTION,Description);
                    //Log.d(LOG_TAG, " Description : " + Description);
                    long ISBN = BookInfoJsonObject.getLong(JSON_ISBN);
                    BookInfoValues.put(BookEntry.COLUMN_ISBN,ISBN);
                    //Log.d(LOG_TAG, " ISBN : " + ISBN);
                    String ImageLink = BookInfoJsonObject.getString(JSON_IMAGELINK);
                    BookInfoValues.put(BookEntry.COLUMN_IMAGE_LINK,ImageLink);
                    //Log.d(LOG_TAG, " ImageLink : " + ImageLink);
                    BookInfoValues.put(BookEntry.COLUMN_BOOK_SEARCH_QUERY,mSearchQuery);
                    //Log.d(LOG_TAG, " mSearchQuery : " + mSearchQuery);

                    mContentValueArrayList.add(i,BookInfoValues);
                    ////Log.d(LOG_TAG, " BookInfoValues : " + BookInfoValues.toString());
                }
            }
            else //{"Error":"Book not found!"}
            {
                //Log.d(LOG_TAG, " getBookSearchListDataFromJson Error From Web Api Call : " + JSONError);
            }

        }
        catch (JSONException e)
        {
            //Log.d(LOG_TAG, "getBookSearchListDataFromJson JSON Parsing Error : " + e.getMessage());
            e.printStackTrace();
        }
        return mContentValueArrayList;

    }

    private void storeDataInITBDProvider(CopyOnWriteArrayList<ContentValues> contentValueArrayList) // This method is overloaded
    {
        ContentValues[] myCV = new ContentValues[RESULTS_PER_PAGE];
        int rowCount = getContext().getContentResolver().bulkInsert(BookEntry.BOOKS_CONTENT_URI,contentValueArrayList.toArray(myCV));
        //Log.d(LOG_TAG, "BulkInsert done for row count : " + rowCount);
    }

    private void storeDataInITBDProvider(ContentValues mContentValues, long longBookId) // This method is overloaded
    {
        Uri AuthorInsertUri = getContext().getContentResolver().insert(AuthorEntry.buildAuthorsBookIdUri(longBookId), mContentValues);
        //Log.d(LOG_TAG, "Author Insert complete for URI : " + AuthorInsertUri);
    }

    private String FetchBookSearchResults(long mBookId)
    {
        Uri.Builder bookQueryUri = Uri.parse(getContext().getString(R.string.api_book_id)).buildUpon();
        //eventually store mBookId in preferences and make it a suggestion for the searchView
        bookQueryUri.appendPath(String.valueOf(mBookId));
        return makeNetworkApiCall(bookQueryUri.toString());
    }

    private String FetchBookSearchResults(String mSearchQuery)
    {
        Uri.Builder searchQueryUri = Uri.parse(getContext().getString(R.string.api_book_search)).buildUpon();
        //String searchQuery = Utility.getPreferredLocation(getContext()); //eventually store it in preferences and make it a suggestion for the searchView
        searchQueryUri.appendPath(mSearchQuery);
        return makeNetworkApiCall(searchQueryUri.toString());
    }

    private String makeNetworkApiCall(String ITEbooksInfoUrl)
    {
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        StringBuffer buffer = new StringBuffer();
        URL fetchBookTaskUrl = null;
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
                //Log.d(LOG_TAG, "buffer.toString() : " + buffer.toString());
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



    //Helper method to have the sync adapter sync immediately.... @param context The context used to access the account service
    public static void syncImmediately(Context context,String mSearchQuery, String mBookId)
    {
        Bundle bundle = new Bundle();
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);

        if (mSearchQuery == null)
            mSearchQuery = "";
        else if (mSearchQuery.isEmpty())
            mSearchQuery = context.getString(R.string.search_query_string_default);
        if (mBookId == null)
            mBookId = "0";
        else if (mBookId.isEmpty())
            mBookId = context.getString(R.string.book_id_default);

        bundle.putString(context.getString(R.string.search_query_label), mSearchQuery);
        bundle.putString(context.getString(R.string.book_id_label), mBookId);

        ContentResolver.requestSync(getSyncAccount(context), context.getString(R.string.content_authority), bundle);
    }


    private static Account getSyncAccount(Context context)
    {
        AccountManager accountManager = (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);
        Account newAccount = new Account(context.getString(R.string.app_name), context.getString(R.string.sync_account_type));
        String password = accountManager.getPassword(newAccount);
        // If the password doesn't exist, the account doesn't exist
        if (password == null)
        {
        // Add the account and account type, no password or user data If successful, return the Account object, otherwise report an error.
            if (!accountManager.addAccountExplicitly(newAccount, "", null))
                return null;
            ContentResolver.setSyncAutomatically(newAccount, context.getString(R.string.content_authority), true); //Without calling setSyncAutomatically, our periodic sync will not be enabled.
        }
        return newAccount;
    }

    public static void initializeSyncAdapter(Context context)
    {
        getSyncAccount(context);
    }

}
