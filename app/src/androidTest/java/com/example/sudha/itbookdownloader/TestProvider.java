package com.example.sudha.itbookdownloader;

import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.test.AndroidTestCase;
import android.util.Log;

import com.example.sudha.itbookdownloader.data.ITBookDownloaderContract.AuthorEntry;
import com.example.sudha.itbookdownloader.data.ITBookDownloaderContract.BookEntry;

import java.util.ArrayList;
import java.util.List;

public class TestProvider extends AndroidTestCase
{

    public static final String LOG_TAG = TestProvider.class.getSimpleName();
    static final long TEST_BOOK_ID = 840578312L;
    //private static final int RESULTS_PER_PAGE = 10;

    // brings our database to an empty state
    public void testADeleteAllRecords()
{
    mContext.getContentResolver().delete(BookEntry.BOOKS_CONTENT_URI,null,null);
    mContext.getContentResolver().delete(AuthorEntry.AUTHORS_CONTENT_URI,null,null);

    Cursor cursor = mContext.getContentResolver().query(BookEntry.buildBookCollectionUri(),null,null,null,null);
    assertEquals(0, cursor.getCount());
    cursor.close();

    cursor = mContext.getContentResolver().query(AuthorEntry.buildAuthorsCollectionUri(),null,null,null,null);
    assertEquals(0, cursor.getCount());
    cursor.close();
}

    // Since we want each test to start with a clean slate, run deleteAllRecords
    // in setUp (called by the test runner before each test).
    public void setUp()
    {
        //testADeleteAllRecords();
    }

    public void testBInsertAndQueryProvider()
    {
        ContentValues TestBookInfoValues = TestDb.createBookInfoValues();

        Uri BookInfoUri = mContext.getContentResolver().insert(BookEntry.buildBookCollectionUri(), TestBookInfoValues);
        long BookInfoBookId = ContentUris.parseId(BookInfoUri);
        assertEquals(BookInfoBookId,TestDb.TEST_BOOK_ID);
        Log.d(LOG_TAG, "Books Insert works");

        Cursor BooksCursor = mContext.getContentResolver().query(BookEntry.BOOKS_CONTENT_URI,null,null,null,null);
        TestDb.validateCursor(BooksCursor, TestBookInfoValues,true);
        Log.d(LOG_TAG, "Querying Books Inserted values works");

        ContentValues AuthorValues = TestDb.createAuthorValues(TestDb.TEST_BOOK_ID);

        Uri AuthorInsertUri = mContext.getContentResolver().insert(AuthorEntry.buildAuthorsBookIdUri(TestDb.TEST_BOOK_ID), AuthorValues);
        assertTrue(AuthorInsertUri != null);
        Log.d(LOG_TAG, "Author Insert works");

        Cursor AuthorCursor = mContext.getContentResolver().query(AuthorEntry.buildAuthorsBookIdUri(TestDb.TEST_BOOK_ID), null, null, null, null);
        TestDb.validateCursor(AuthorCursor, AuthorValues,true);
        Log.d(LOG_TAG, "Querying Books Inserted values works");

        // Add the Author values in with the Book data so that we can make sure that the join worked and we actually get all the values back
        addAllContentValues(TestBookInfoValues, AuthorValues);
        // Now see if we can successfully query both tables if we include the Book Id
        Cursor BookIdQueryCursor = mContext.getContentResolver().query(BookEntry.buildJoinBookIdUri(TestDb.TEST_BOOK_ID),null,null,null,null);
        TestDb.validateCursor(BookIdQueryCursor, TestBookInfoValues,true);
    }

    public void testCDeleteWithBookId()
    {
        mContext.getContentResolver().delete(BookEntry.buildJoinBookIdUri(TestDb.TEST_BOOK_ID),null,null);

        Cursor cursor = mContext.getContentResolver().query(BookEntry.buildJoinBookIdUri(TestDb.TEST_BOOK_ID),null,null,null,null);
        assertEquals(0, cursor.getCount());
        cursor.close();
    }

    public void testDUpdateFilePathNameAndReadBook()
    {
        testADeleteAllRecords(); //to freshly install some new data in the DB.
        insertBookInfoAndAuthorData();

        // Make an update to one value.
        ContentValues FilePathNameUpdateContentValues = new ContentValues();
        String newFilePathName = "This is where the file will be actually downloaded...New File Path Name";
        FilePathNameUpdateContentValues.put(AuthorEntry.COLUMN_FILE_PATHNAME, newFilePathName);
        //update DB
        mContext.getContentResolver().update(AuthorEntry.buildAuthorsBookIdUri(TEST_BOOK_ID), FilePathNameUpdateContentValues, null, null);

        // Query to get a cursor with updated value
        String[] projection = new String[]{AuthorEntry.COLUMN_FILE_PATHNAME};
        Cursor updateFilePathNameCursor = mContext.getContentResolver().query(AuthorEntry.buildAuthorsBookIdUri(TEST_BOOK_ID),projection,null,null,null);

        //validate cursor
        TestDb.validateCursor(updateFilePathNameCursor, FilePathNameUpdateContentValues,true);
    }
    public void testEBulkInsertAndQuery()
    {
        testADeleteAllRecords(); //to freshly install some new data in the DB.
        ContentValues TestDbBookInfoValues = TestDb.createBookInfoValues();
        ContentValues BookInfoValues = createBookInfoValues();
        ContentValues[] myCV = new ContentValues[]{};
        List<ContentValues> testContentValueArrayList = new ArrayList<>();
        testContentValueArrayList.add(0,TestDbBookInfoValues);
        testContentValueArrayList.add(1,BookInfoValues);
        int rowCount = mContext.getContentResolver().bulkInsert(BookEntry.BOOKS_CONTENT_URI,testContentValueArrayList.toArray(myCV));
        Log.d(LOG_TAG, "BulkInsert works");
        Cursor BooksCursor = mContext.getContentResolver().query(BookEntry.BOOKS_CONTENT_URI,null,null,null,null);
        assertEquals(rowCount, BooksCursor.getCount());
        Log.d(LOG_TAG, "Querying BulkInsert Count works");

    }

    public void testFetchBooksWithUserSearchQuery()
    {
        testADeleteAllRecords(); //to freshly install some new data in the DB.
        ContentValues TestBookInfoValues = TestDb.createBookInfoValues();

        Uri BookInfoUri = mContext.getContentResolver().insert(BookEntry.BOOKS_CONTENT_URI, TestBookInfoValues);
        long BookInfoBookId = ContentUris.parseId(BookInfoUri);
        assertEquals(BookInfoBookId,TestDb.TEST_BOOK_ID);
        Log.d(LOG_TAG, "Books Insert works");

        Cursor BooksCursor = mContext.getContentResolver().query(BookEntry.buildBookSearchUriForSearchQuery("PHP"),null,null,null,null);
        TestDb.validateCursor(BooksCursor, TestBookInfoValues,true);
        Log.d(LOG_TAG, "Querying Books with Search Query value works");

    }
    public void testGetType()
    {
        String type = mContext.getContentResolver().getType(BookEntry.BOOKS_CONTENT_URI);               //content://com.example.sudha.ITBookDownloader/books
        assertEquals(BookEntry.CONTENT_BOOKS_DIR_TYPE, type);                                                     // vnd.android.cursor.dir/com.example.sudha.ITBookDownloader/books

        type = mContext.getContentResolver().getType(BookEntry.buildBookSearchUriForSearchQuery("Android"));               //content://com.example.sudha.ITBookDownloader/search/{android}
        assertEquals(BookEntry.CONTENT_BOOKS_DIR_TYPE, type);                                                     // vnd.android.cursor.dir/com.example.sudha.ITBookDownloader/books

        type = mContext.getContentResolver().getType(AuthorEntry.AUTHORS_CONTENT_URI);                  //content://com.example.sudha.ITBookDownloader/authors
        assertEquals(AuthorEntry.CONTENT_AUTHORS_DIR_TYPE, type);                                           // vnd.android.cursor.dir/com.example.sudha.ITBookDownloader/authors

        type = mContext.getContentResolver().getType(BookEntry.buildJoinBookIdUri(TestDb.TEST_BOOK_ID));    // content://com.example.sudha.ITBookDownloader/book/#
        assertEquals(BookEntry.CONTENT_BOOKS_ITEM_TYPE, type);                                                // vnd.android.cursor.item/com.example.sudha.ITBookDownloader/books

        type = mContext.getContentResolver().getType(AuthorEntry.buildAuthorsBookIdUri(TestDb.TEST_BOOK_ID));    // content://com.example.sudha.ITBookDownloader/book/#
        assertEquals(AuthorEntry.CONTENT_AUTHORS_ITEM_TYPE, type);                                                // vnd.android.cursor.item/com.example.sudha.ITBookDownloader/books
    }

    // Make sure we can still delete after adding/updating stuff
    public void testZDeleteRecordsAtEnd()
    {
        testADeleteAllRecords();
    }

    void addAllContentValues(ContentValues destination, ContentValues source)
    {
        for (String key : source.keySet())
        {
            destination.put(key, source.getAsString(key));
        }
    }

    static ContentValues createBookInfoValues()
    {
        ContentValues BookInfoValues = new ContentValues();
        BookInfoValues.put(BookEntry.COLUMN_BOOK_ID, TEST_BOOK_ID); //static final long TEST_BOOK_ID = 840578312L;
        BookInfoValues.put(BookEntry.COLUMN_BOOK_SEARCH_QUERY, "Java");
        BookInfoValues.put(BookEntry.COLUMN_TITLE, "Java Cookbook, 2nd Edition");
        BookInfoValues.put(BookEntry.COLUMN_SUBTITLE, "Solutions and Examples for Java Developers");
        BookInfoValues.put(BookEntry.COLUMN_DESCRIPTION, "You have a choice: you can wade your way through lengthy Java tutorials and figure things out by trial and error, or you can pick up Java Cookbook, 2nd Edition and get to the heart of what you need to know when you need to know it.\\n\\nWith the completely revised and thoroughly updated Java Cookbook, 2nd Edition, Java developers like you will learn by example, try out new features, and use sample code to understand how new additions to the language and platform work - and how to put them to work for you.");
        BookInfoValues.put(BookEntry.COLUMN_ISBN, 9780596007010L);
        BookInfoValues.put(BookEntry.COLUMN_IMAGE_LINK, "http://s.it-ebooks-api.info/3/java_cookbook_2nd_edition.jpg");

        return BookInfoValues;
    }

    static ContentValues createAuthorValues(long BookId)
    {
        // Create a new map of values, where column names are the keys
        ContentValues AuthorValues = new ContentValues();
        AuthorValues.put(AuthorEntry.COLUMN_BOOK_ID, BookId);
        AuthorValues.put(AuthorEntry.COLUMN_AUTHORNAME, "Ian F. Darwin");
        AuthorValues.put(AuthorEntry.COLUMN_YEAR, 2004);
        AuthorValues.put(AuthorEntry.COLUMN_PAGE, 864);
        AuthorValues.put(AuthorEntry.COLUMN_PUBLISHER, "O'Reilly Media");
        AuthorValues.put(AuthorEntry.COLUMN_DOWNLOAD_LINK, "http://filepi.com/i/TfhcXwr");
        AuthorValues.put(AuthorEntry.COLUMN_FILE_PATHNAME, "file Path name");

        return AuthorValues;
    }


    protected ContentValues insertBookInfoAndAuthorData()
    {
        ContentValues BookInfoValues = createBookInfoValues();
        Uri BookInfoInsertUri = mContext.getContentResolver().insert(BookEntry.buildBooksIdUri(TEST_BOOK_ID), BookInfoValues);
        assertTrue(BookInfoInsertUri != null);

        ContentValues authorValues = createAuthorValues(TEST_BOOK_ID);
        Uri AuthorInsertUri = mContext.getContentResolver().insert(AuthorEntry.buildAuthorsBookIdUri(TEST_BOOK_ID), authorValues);
        assertTrue(AuthorInsertUri != null);

        //return combined ContentValues for comparison with cursor for inner join query
        addAllContentValues(BookInfoValues,authorValues);
        return BookInfoValues;

    }
}
