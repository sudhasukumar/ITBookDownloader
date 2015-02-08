package com.example.sudha.itbookdownloader;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.CopyOnWriteArrayList;

import static com.example.sudha.itbookdownloader.data.ITBookDownloaderContract.AuthorEntry;
import static com.example.sudha.itbookdownloader.data.ITBookDownloaderContract.BookEntry;

/**
 * Created by Sudha on 1/24/2015.
 */
public class Utility
{
    public static final String LOG_TAG = Utility.class.getSimpleName();
    private final Context context;

    public Utility(Context context)
    {
        this.context = context;
    }

    protected void prepareInputForAsyncTask(String mSearchQuery, String mIsbn, String mBookId)
    {
        String searchQuery = prepareInputForBookSearch(context.getString(R.string.search_query_label),mSearchQuery);
        String Isbn = prepareInputForBookSearch(context.getString(R.string.isbn_label),mIsbn);
        String bookId = prepareInputForBookSearch(context.getString(R.string.book_id_label),mBookId);
        String WebApiUriString;
        if( (Isbn.equals("0") ) && (bookId.equals("0") ) ) // SearchQuery Search
        {
            WebApiUriString = getWebApiUriString(searchQuery);
            String BookSearchListJSONString = makeNetworkApiCall(WebApiUriString);
            parseSearchQueryJsonAndStoreData(BookSearchListJSONString, searchQuery);
        }
        else if ( (!bookId.equals("0") ) && (searchQuery.equals("") ) && (!mIsbn.equals("0")) ) // ISBN Search
        {
            WebApiUriString = getWebApiUriString(context.getString(R.string.isbn_label), Isbn);
            String BookISBNSearchHTMLString = makeNetworkApiCall(WebApiUriString);
            String WebsiteBookNumber = getWebsiteBookNumber(BookISBNSearchHTMLString);
            String BookDetailsWebUri = getBookDetailsWebUri(WebsiteBookNumber);
            String BookSearchListJSONString = makeNetworkApiCall(BookDetailsWebUri);
            parseWebsiteHtmlAndStoreData(BookSearchListJSONString, mIsbn , bookId , WebsiteBookNumber);  // Store websiteBookNumber in Author Data for referer field value to download file
        }
        else if ( (Isbn.equals("0")) && (searchQuery.equals("") ) ) // BookId Search
        {
            WebApiUriString = getWebApiUriString(Long.parseLong(bookId));
            String BookSearchListJSONString = makeNetworkApiCall(WebApiUriString);
            parseAuthorsBookIdAndStoreData(BookSearchListJSONString, bookId);
        }
    }

    private void parseWebsiteHtmlAndStoreData(String mBookSearchListJSONString, String mIsbn , String mBookId, String mWebsiteBookNumber)
    {
        long BookId = Long.parseLong(mBookId);
        // Check if the BookId is present in the Books table...It should be there since the search with BookId originated from that info
        Cursor BooksBookIdCursor = context.getContentResolver().query(BookEntry.buildBooksIdUri(BookId), new String[]{BookEntry._ID}, null, null, null);
        if ( BooksBookIdCursor.moveToFirst() ) // Assuming the Book Id is present when the cursor moves to first row
        {
            int BookIdIndex = BooksBookIdCursor.getColumnIndex(BookEntry._ID);
            long LongBookIdFromJson = BooksBookIdCursor.getLong(BookIdIndex);
            if ( mBookSearchListJSONString.length() != 0 )//BookId in Books Table ...Now Fetch Author data
            {
                ContentValues AuthorValues = getWebsiteBookNumberAuthorData(mBookSearchListJSONString, mIsbn , mBookId , mWebsiteBookNumber); //...get Authors Info from WebsiteBookNumber HTML Doc
                ////Log.d(LOG_TAG, "Author Insert initiated for BookId : " + BookId);
                Cursor AuthorsBookIdCursor = context.getContentResolver().query(AuthorEntry.buildAuthorsBookIdUri(BookId), new String[]{BookEntry._ID}, null, null, null);
                int AuthorsBookIdCursorCount = AuthorsBookIdCursor.getCount();
                if (( AuthorValues.size() != 0 )&&(AuthorsBookIdCursorCount == 0))
                    storeDataInITBDProvider(AuthorEntry.TABLE_NAME, LongBookIdFromJson, AuthorValues); //Insert the JSON info into Authors Table.
            }
            else //Book Id is present in Books Table But web Api call doesnt return anything
            {
                //Log.d(LOG_TAG, "BookSearchListJSONString is empty for BookId : " + BookId);
            }
        }
    }

    private ContentValues getWebsiteBookNumberAuthorData(String mBookSearchListJSONString, String mIsbn, String mBookId, String mWebsiteBookNumber)
    {

        ContentValues AuthorValues = new ContentValues();

        try
        {
            Document BookIdDocument = Jsoup.parse(mBookSearchListJSONString);
            Element BookIdDocumentBody = BookIdDocument.body();
            Element TdJustifyLinkElement = BookIdDocumentBody.getElementsByClass("justify").first();
            String BookDescription = TdJustifyLinkElement.getElementsByAttributeValueMatching("itemprop", "description").first().text();
            ////Log.d(LOG_TAG, "Book Description : " + BookDescription);
            updateBookDescriptionInITBDProvider(mBookId,BookDescription); //update the new description

            String AuthorName = TdJustifyLinkElement.getElementsByAttributeValueMatching("itemprop", "author").first().text();
            ////Log.d(LOG_TAG, "AuthorName : " + AuthorName);
            String Year = TdJustifyLinkElement.getElementsByAttributeValueMatching("itemprop", "datePublished").first().text();
            ////Log.d(LOG_TAG, "Published Year : " + Year);
            String Page = TdJustifyLinkElement.getElementsByAttributeValueMatching("itemprop", "numberOfPages").first().text();
            ////Log.d(LOG_TAG, "Page : " + Page);
            String Publisher = TdJustifyLinkElement.getElementsByAttributeValueMatching("itemprop","publisher").first().text();
            ////Log.d(LOG_TAG, "Publisher : " + Publisher);
            String BookFormat = TdJustifyLinkElement.getElementsByAttributeValueMatching("itemprop","bookFormat").first().text();
            ////Log.d(LOG_TAG, "BookFormat : " + BookFormat);
            String DownloadLink = TdJustifyLinkElement.select("a[href*=filepi.com").first().attr("href");
            ////Log.d(LOG_TAG, "DownloadLink : " + DownloadLink);


            AuthorValues.put(AuthorEntry.COLUMN_BOOK_ID, mBookId);
            AuthorValues.put(AuthorEntry.COLUMN_WEBSITE_BOOK_NUMBER, mWebsiteBookNumber);
            AuthorValues.put(AuthorEntry.COLUMN_AUTHOR_ISBN, mIsbn);
            AuthorValues.put(AuthorEntry.COLUMN_AUTHORNAME, AuthorName);
            AuthorValues.put(AuthorEntry.COLUMN_YEAR, Year);
            AuthorValues.put(AuthorEntry.COLUMN_PAGE, Page);
            AuthorValues.put(AuthorEntry.COLUMN_PUBLISHER, Publisher);
            AuthorValues.put(AuthorEntry.COLUMN_DOWNLOAD_LINK, DownloadLink);
            AuthorValues.put(AuthorEntry.COLUMN_FILE_FORMAT, BookFormat);
            AuthorValues.put(AuthorEntry.COLUMN_FILE_PATHNAME, "file Path name TBD");
        }
        catch ( Exception e )
        {
            e.printStackTrace();
        }
        return AuthorValues;

    }

    private void updateBookDescriptionInITBDProvider(String mBookId, String bookDescription)
    {
        ContentValues descriptionCV = new ContentValues();
        descriptionCV.put(BookEntry.COLUMN_DESCRIPTION,bookDescription);
        context.getContentResolver().update(BookEntry.buildBooksIdUri(Long.parseLong(mBookId)), descriptionCV, null, null);
        ////Log.d(LOG_TAG," Updated Description in Books Table for Book Id : " + mBookId);
    }

    private String getBookDetailsWebUri(String websiteBookNumber)
    {
        Uri.Builder BookDetailsWebsiteUri = Uri.parse(context.getString(R.string.book_number_website_book_search)).buildUpon(); //http://www.it-ebooks.info/book/{345}/
        BookDetailsWebsiteUri.appendPath(websiteBookNumber);
        return BookDetailsWebsiteUri.toString();
    }

    private String getWebsiteBookNumber(String bookISBNSearchHTMLString)
    {
        String WebsiteBookNumber = Jsoup.parse(bookISBNSearchHTMLString).select("a[href*=/book/]").first().attr("href");
        String[] WebsiteBookNumberArray = WebsiteBookNumber.split("/"); //(6,10);
        WebsiteBookNumber = WebsiteBookNumberArray[2];
        return WebsiteBookNumber;
    }


    private String prepareInputForBookSearch(String mKey,String mValue)
    {
        if(mKey.equalsIgnoreCase(context.getString(R.string.search_query_label)))
        {
            if ( mValue == null )
                mValue = "";
            else if ( mValue.isEmpty() )
                mValue = context.getString(R.string.search_query_string_default);
        }
        else if (mKey.equalsIgnoreCase(context.getString(R.string.isbn_label)))
        {
            if ( mValue == null )
                mValue = "0";
            else if ( mValue.isEmpty() )
                mValue = context.getString(R.string.book_isbn_default);
        }
        else if (mKey.equalsIgnoreCase(context.getString(R.string.book_id_label)))
        {
            if ( mValue == null )
                mValue = "0";
            else if ( mValue.isEmpty() )
                mValue = context.getString(R.string.book_id_default);
        }
        return mValue;
    }

    protected void parseSearchQueryJsonAndStoreData(String mBookSearchListJSONString, String mSearchQuery)
    {
        CopyOnWriteArrayList<ContentValues> ContentValueArrayList;
        ////Log.d(LOG_TAG, "BookSearchListJSONString for SearchQuery : " + mSearchQuery + " *** " + mBookSearchListJSONString);
        if ( mBookSearchListJSONString.length() != 0 ) //When you have a list from Web Api Call
        {
            ContentValueArrayList = getBookSearchListDataFromJson(mBookSearchListJSONString, mSearchQuery); //Get the Content Values from JSON
            ////Log.d(LOG_TAG, "BulkInsert initiated for SearchQuery : " + mSearchQuery);
            if ( !ContentValueArrayList.isEmpty() )
                storeDataInITBDProvider(ContentValueArrayList); // Initiate Bulk Insert into Books table
        }
    }

    protected  void parseAuthorsBookIdAndStoreData(String mBookSearchListJSONString, String mBookId)
    {
        long BookId = Long.parseLong(mBookId);
        // Check if the BookId is present in the Books table...It should be there since the search with BookId originated from that info
        Cursor BooksBookIdCursor = context.getContentResolver().query(BookEntry.buildBooksIdUri(BookId), new String[]{BookEntry._ID}, null, null, null);
        if ( BooksBookIdCursor.moveToFirst() ) // Assuming the Book Id is present when the cursor moves to first row
        {
            int BookIdIndex = BooksBookIdCursor.getColumnIndex(BookEntry._ID);
            long LongBookIdFromJson = BooksBookIdCursor.getLong(BookIdIndex);
            if ( mBookSearchListJSONString.length() != 0 )//BookId in Books Table ...Now Fetch Author data
            {
                ContentValues AuthorValues = getBookIdAuthorDataFromJson(mBookSearchListJSONString); //...get Authors Info from JSON
                ////Log.d(LOG_TAG, "Author Insert initiated for BookId : " + BookId);
                if ( AuthorValues.size() != 0 )
                    storeDataInITBDProvider(AuthorEntry.TABLE_NAME, LongBookIdFromJson, AuthorValues); //Insert the JSON info into Authors Table.
            }
            else //Book Id is present in Books Table But web Api call doesnt return anything
            {
                //Log.d(LOG_TAG, "BookSearchListJSONString is empty for BookId : " + BookId);
            }
        }
        else // The Book Id cannot be found in the Books table....hmm suspicious bookId ?
        {
            if ( mBookSearchListJSONString.length() != 0 ) //...But may be found in the web api call
            {
                // Then get Book and Author CV from Json
                ContentValues BookInfoValues = getBookIdBookDataFromJson(mBookSearchListJSONString); //...get Book Info from JSON
                ContentValues AuthorValues = getBookIdAuthorDataFromJson(mBookSearchListJSONString); //...get Authors Info from JSON
                //If Cvs are not empty then insert both Book and Author Info for Book Id in DB
                if ( (BookInfoValues.size() != 0) && (AuthorValues.size() != 0) )
                {
                    storeDataInITBDProvider(BookEntry.TABLE_NAME, BookId, BookInfoValues);
                    storeDataInITBDProvider(AuthorEntry.TABLE_NAME, BookId, AuthorValues);
                }

            }
            else //Book Id not found in Books Table and Web Api doesnt return any thing.
            {
                //Need not implement this for now because BookId is considered Private data and when the execution comes to this point means the origin of BookId is suspicious.
                //There is no data to proceed further with that Book Id. Just Log the ID for debugging
                //Log.d(LOG_TAG, "There is no such BookId in the Books Table and Web Api Call for : " + BookId);
            }
        }
        BooksBookIdCursor.close();

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
            if ( JSONError.equalsIgnoreCase(JSON_ZERO_SUCCESS_CODE) ) //"Error": "0"
            {
                long BookId = Long.parseLong(getValueFromJson(BookInfoDataJsonObject, JSON_ID));
                BookInfoValues.put(BookEntry.COLUMN_BOOK_ID, BookId);

                String Title = getValueFromJson(BookInfoDataJsonObject, JSON_TITLE);
                BookInfoValues.put(BookEntry.COLUMN_TITLE, Title);

                String Subtitle = getValueFromJson(BookInfoDataJsonObject, JSON_SUBTITLE);
                BookInfoValues.put(BookEntry.COLUMN_SUBTITLE, Subtitle);

                String Description = getValueFromJson(BookInfoDataJsonObject, JSON_DESCRIPTION);
                BookInfoValues.put(BookEntry.COLUMN_DESCRIPTION, Description);

                long ISBN = Long.parseLong(getValueFromJson(BookInfoDataJsonObject, JSON_AUTHOR_ISBN));
                BookInfoValues.put(BookEntry.COLUMN_ISBN, ISBN);

                String ImageLink = getValueFromJson(BookInfoDataJsonObject, JSON_IMAGELINK);
                BookInfoValues.put(BookEntry.COLUMN_IMAGE_LINK, ImageLink);

                //If the Book is stored based on ID in the Books table then default the Search Query column to Book Title
                BookInfoValues.put(BookEntry.COLUMN_BOOK_SEARCH_QUERY, Title);
            }
            else //{"Error":"Book not found!"}
            {
                //Log.d(LOG_TAG, " getBookIdBookDataFromJson Error From Web Api Call : " + JSONError);
            }
        }
        catch ( JSONException e )
        {
            ////Log.d(LOG_TAG, "getBookIdBookDataFromJson JSON Parsing Error : " + e.getMessage());
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
        final String JSON_DOWNLOADLINK = "Download";
        ContentValues AuthorValues = new ContentValues();
        JSONObject BookIdJsonObject;
        try
        {
            BookIdJsonObject = new JSONObject(bookIdAuthorJSONString);
            String JSONError = BookIdJsonObject.getString(JSON_ERROR);
            AuthorValues.clear();
            if ( JSONError.equalsIgnoreCase(JSON_ZERO_SUCCESS_CODE) ) //"Error": "0"
            {
                long BookId = Long.parseLong(getValueFromJson(BookIdJsonObject, JSON_ID));
                AuthorValues.put(AuthorEntry.COLUMN_BOOK_ID, BookId);

                String AuthorName = getValueFromJson(BookIdJsonObject, JSON_AUTHOR);
                AuthorValues.put(AuthorEntry.COLUMN_AUTHORNAME, AuthorName);

                long Year = Long.parseLong(getValueFromJson(BookIdJsonObject, JSON_YEAR));
                AuthorValues.put(AuthorEntry.COLUMN_YEAR, Year);

                long Page = Long.parseLong(getValueFromJson(BookIdJsonObject, JSON_PAGE));
                AuthorValues.put(AuthorEntry.COLUMN_PAGE, Page);

                String Publisher = getValueFromJson(BookIdJsonObject, JSON_PUBLISHER);
                AuthorValues.put(AuthorEntry.COLUMN_PUBLISHER, Publisher);

                String DownloadLink = getValueFromJson(BookIdJsonObject, JSON_DOWNLOADLINK);
                AuthorValues.put(AuthorEntry.COLUMN_DOWNLOAD_LINK, DownloadLink);

                AuthorValues.put(AuthorEntry.COLUMN_FILE_PATHNAME, "file Path name TBD");
            }
            else //{"Error":"Book not found!"}
            {
                //Log.d(LOG_TAG, " getBookIdAuthorDataFromJson Error From Web Api Call : " + JSONError);
            }
        }
        catch ( JSONException e )
        {
            ////Log.d(LOG_TAG, "getBookSearchListDataFromJson JSON Parsing Error : " + e.getMessage());
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
            if ( JSONError.equalsIgnoreCase(JSON_ZERO_SUCCESS_CODE) ) //"Error": "0"
            {
                mContentValueArrayList.clear();
                ContentValues BookInfoValues;

                BooksArray = BookSearchListJsonObject.getJSONArray(JSON_BOOKS);
                ////Log.d(LOG_TAG, " BooksArray Length : " + BooksArray.length() + " BooksArray : " + BooksArray.toString());

                for ( int i = 0; i < BooksArray.length(); i++ )
                {
                    BookInfoValues = new ContentValues();
                    BookInfoValues.clear();

                    try
                    {
                        JSONObject BookInfoJsonObject = BooksArray.getJSONObject(i);
                        long BookId = Long.parseLong(getValueFromJson(BookInfoJsonObject, JSON_ID));
                        BookInfoValues.put(BookEntry.COLUMN_BOOK_ID, BookId);
                        ////Log.d(LOG_TAG, "*** BookId : " + BookId);

                        String Title = getValueFromJson(BookInfoJsonObject, JSON_TITLE);
                        BookInfoValues.put(BookEntry.COLUMN_TITLE, Title);

                        String Subtitle = getValueFromJson(BookInfoJsonObject, JSON_SUBTITLE);
                        BookInfoValues.put(BookEntry.COLUMN_SUBTITLE, Subtitle);
                        ////Log.d(LOG_TAG, " Subtitle : " + Subtitle);

                        String Description = getValueFromJson(BookInfoJsonObject, JSON_DESCRIPTION);
                        BookInfoValues.put(BookEntry.COLUMN_DESCRIPTION, Description);
                        ////Log.d(LOG_TAG, " Description : " + Description);

                        long ISBN = Long.parseLong(getValueFromJson(BookInfoJsonObject, JSON_ISBN));
                        BookInfoValues.put(BookEntry.COLUMN_ISBN, ISBN);
                        ////Log.d(LOG_TAG, " ISBN : " + ISBN);

                        String ImageLink = getValueFromJson(BookInfoJsonObject, JSON_IMAGELINK);
                        BookInfoValues.put(BookEntry.COLUMN_IMAGE_LINK, ImageLink);
                        ////Log.d(LOG_TAG, " ImageLink : " + ImageLink);

                        BookInfoValues.put(BookEntry.COLUMN_BOOK_SEARCH_QUERY, mSearchQuery);
                        ////Log.d(LOG_TAG, " mSearchQuery : " + mSearchQuery);
                    }
                    catch ( JSONException e )
                    {
                        ////Log.d(LOG_TAG, " getBookSearchListDataFromJson For Loop Error From Web Api Call : " + e.getMessage());
                        e.printStackTrace();
                    }
                    catch ( NumberFormatException e )
                    {
                        ////Log.d(LOG_TAG, " getBookSearchListDataFromJson For Loop Error From Web Api Call : " + e.getMessage());
                        e.printStackTrace();
                    }
                    mContentValueArrayList.add(i, BookInfoValues);
                    ////Log.d(LOG_TAG, " BookInfoValues : " + BookInfoValues.toString());
                }
            }
            else //{"Error":"Book not found!"}
            {
                //Log.d(LOG_TAG, " getBookSearchListDataFromJson Error From Web Api Call : " + JSONError);
            }

        }
        catch ( JSONException e )
        {
            ////Log.d(LOG_TAG, "getBookSearchListDataFromJson JSON Parsing Error : " + e.getMessage());
            e.printStackTrace();
        }
        return mContentValueArrayList;

    }

    private static String getValueFromJson(JSONObject bookInfoJsonObject, String mKey) throws JSONException
    {
        String mValue = "";
        try
        {
            Iterator<String> JSONKeysIterator = bookInfoJsonObject.keys();
            while (JSONKeysIterator.hasNext())
            {
                String KeyFromIterator = JSONKeysIterator.next();
                if (KeyFromIterator.equalsIgnoreCase(mKey))
                {
                    mValue = bookInfoJsonObject.getString(KeyFromIterator);
                }
            }
        }
        catch ( JSONException e )
        {
            ////Log.d(LOG_TAG, " getValueFromJson : " + e.getMessage());
            e.printStackTrace();
        }
        ////Log.d(LOG_TAG, " getValueFromJson : " + mKey + " : " + mValue);
        return mValue;
    }

    private void storeDataInITBDProvider(CopyOnWriteArrayList<ContentValues> contentValueArrayList) // This method is overloaded
    {
        final int RESULTS_PER_PAGE = 10;
        ContentValues[] myCV = new ContentValues[RESULTS_PER_PAGE];
        //before doing a bulk insert delete all records in both tables Books and Author.
        context.getContentResolver().delete(BookEntry.buildBookCollectionUri(), null, null);
        context.getContentResolver().delete(AuthorEntry.buildAuthorsCollectionUri(), null, null);
        int rowCount = context.getContentResolver().bulkInsert(BookEntry.buildBookCollectionUri(), contentValueArrayList.toArray(myCV));
        //Log.d(LOG_TAG, "BulkInsert done for row count : " + rowCount);
    }

    private void storeDataInITBDProvider(String mTableName, long longBookId, ContentValues mContentValues) // This method is overloaded
    {
        Uri BookIdInsertUri = Uri.EMPTY;
        if ( mTableName.equals(BookEntry.TABLE_NAME) )
        {
            BookIdInsertUri = context.getContentResolver().insert(BookEntry.buildBooksIdUri(longBookId), mContentValues);
        }
        else if ( mTableName.equals(AuthorEntry.TABLE_NAME) )
        {
            BookIdInsertUri = context.getContentResolver().insert(AuthorEntry.buildAuthorsBookIdUri(longBookId), mContentValues);
        }
        //Log.d(LOG_TAG, " Insert complete for URI : " + BookIdInsertUri);
    }

    private String getWebApiUriString(long mBookId)
    {
        Uri.Builder bookQueryUri = Uri.parse(context.getString(R.string.api_book_id)).buildUpon();
        bookQueryUri.appendPath(String.valueOf(mBookId));
        return bookQueryUri.toString();
    }

    private String getWebApiUriString(String mSearchQuery)
    {
        Uri.Builder searchQueryUri = Uri.parse(context.getString(R.string.api_book_search)).buildUpon();
        searchQueryUri.appendPath(mSearchQuery);
        return searchQueryUri.toString();
    }

    private String getWebApiUriString(String isbnLabel, String isbn)
    {
        Uri.Builder IsbnQueryUri = Uri.parse(context.getString(R.string.isbn_website_book_search_url)).buildUpon(); //http://it-ebooks.info/search/?q=9781430238317&type=isbn
        IsbnQueryUri.appendQueryParameter("q", isbn);
        IsbnQueryUri.appendQueryParameter("type","isbn");
        //Log.d(LOG_TAG, isbnLabel + " : " + isbn + " " + IsbnQueryUri.toString());
        return IsbnQueryUri.toString();
    }

    String makeNetworkApiCall(String ITEbooksInfoUrl)
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

            if ( inputStream != null )
            {
                reader = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                while ( (line = reader.readLine()) != null )
                {
                    buffer.append(line);
                    buffer.append("\n");
                }
            }

            /*if ( buffer.length() != 0 )
            {
                ////Log.d(LOG_TAG, "buffer.toString() : " + buffer.toString());
            }*/
        }
        catch ( IOException e )
        {
            e.printStackTrace();
        }
        finally
        {
            if ( urlConnection != null )
                urlConnection.disconnect();

            if ( reader != null )
            {
                try
                {
                    reader.close();
                }
                catch ( final IOException e )
                {
                    Log.e(LOG_TAG, "Error closing stream", e);
                }
            }
        }
        return buffer.toString();
    }

    HashMap<String,String> makeBookDownloadNetworkApiCall(String mFileDownloadUrl, String mWebsiteBookNumber, String mFileName, String mFileFormat)
    {
        HashMap<String,String> FileDownloadResults = new HashMap<>(2);

        HttpURLConnection urlConnection = null;
        FileOutputStream BookFileOutputStream = null;
        String DownloadStatus;
        File BookFile = null;
        try
        {
            //File output            File(File dir, String name)
            String SDCardRoot = Environment.getExternalStorageDirectory().getAbsolutePath();
            File ITBDDir = new File(SDCardRoot + File.separator + context.getString(R.string.ITBD_download_dir_path));
            ITBDDir.mkdirs();
            String FileName = mFileName.trim() + "." + mFileFormat.toLowerCase();
            BookFile = new File(ITBDDir,FileName);
            BookFile.createNewFile();

            BookFileOutputStream = new FileOutputStream(BookFile);
            //Log.d(LOG_TAG, " The Book is written to location : " + BookFile.getAbsolutePath());

            // URL input
            URL fetchBookTaskUrl = new URL(mFileDownloadUrl);
            urlConnection = (HttpURLConnection) fetchBookTaskUrl.openConnection();
            urlConnection.setRequestMethod("GET");
            String RefererLabel = context.getString(R.string.referer_property_field);
            String WebsiteBookRefererURL = "http://it-ebooks.info/book/" + mWebsiteBookNumber + "/";
            urlConnection.setRequestProperty(RefererLabel, WebsiteBookRefererURL);
            urlConnection.connect();
            InputStream inputStream = urlConnection.getInputStream();

            // Read from URL and write to file
            byte[] buffer = new byte[1024];
            int len1 = 0;
            while ( (len1 = inputStream.read(buffer)) > 0 )
            {
                BookFileOutputStream.write(buffer, 0, len1);
            }
            DownloadStatus = Constants.FILE_DOWNLOAD_SUCCESS;

        }
        catch(Exception e)
        {
            e.printStackTrace();
            DownloadStatus = Constants.FILE_DOWNLOAD_EXCEPTION;
        }
        finally
        {
            if ( urlConnection != null )
                urlConnection.disconnect();
            if ( BookFileOutputStream != null )
            {
                try
                {
                    BookFileOutputStream.close();
                }
                catch ( IOException e )
                {
                    e.printStackTrace();
                    DownloadStatus = Constants.FILE_DOWNLOAD_EXCEPTION;
                }
            }
        }

        FileDownloadResults.put(Constants.FILE_DOWNLOAD_STATUS_KEY,DownloadStatus);
        if (BookFile != null)
            FileDownloadResults.put(Constants.FILE_ABSOLUTE_PATH,BookFile.getAbsolutePath());
        else
            FileDownloadResults.put(Constants.FILE_ABSOLUTE_PATH,"");
        return FileDownloadResults;
    }

}
