package com.example.sudha.itbookdownloader;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;
import android.util.Log;

import com.example.sudha.itbookdownloader.data.ITBookDownloaderContract.AuthorEntry;
import com.example.sudha.itbookdownloader.data.ITBookDownloaderContract.BookEntry;
import com.example.sudha.itbookdownloader.data.ITBookDownloaderDbHelper;

import java.util.Map;
import java.util.Set;

public class TestDb extends AndroidTestCase
{

    public static final String LOG_TAG = TestDb.class.getSimpleName();
    static final long TEST_BOOK_ID = 2279690981L;

    public void testCreateDb() throws Throwable
    {
        mContext.deleteDatabase(ITBookDownloaderDbHelper.DATABASE_NAME);
        SQLiteDatabase db = new ITBookDownloaderDbHelper(this.mContext).getWritableDatabase();
        assertEquals(true, db.isOpen());
        db.close();
    }

    public void testInsertReadDb()
    {

        // If there's an error in those massive SQL table creation Strings,
        // errors will be thrown here when you try to get a writable database.
        ITBookDownloaderDbHelper DbHelper = new ITBookDownloaderDbHelper(mContext);
        SQLiteDatabase Db = DbHelper.getWritableDatabase();

        ContentValues BookInfoValues = createBookInfoValues();
        long BookInfoRowId = Db.insert(BookEntry.TABLE_NAME, null, BookInfoValues);
        assertTrue(BookInfoRowId != -1);
        Log.d(LOG_TAG, "New Book Info table insert row id: " + BookInfoRowId);

        Cursor BookInfoCursor = Db.query(BookEntry.TABLE_NAME,null,null,null,null,null,null);
        validateCursor(BookInfoCursor, BookInfoValues, false);

        ContentValues AuthorValues = createAuthorValues(TEST_BOOK_ID);
        long AuthorRowId = Db.insert(AuthorEntry.TABLE_NAME, null, AuthorValues);
        assertTrue(AuthorRowId != -1);
        Log.d(LOG_TAG, "New Author table insert row id: " + AuthorRowId);

        Cursor AuthorCursor = Db.query(AuthorEntry.TABLE_NAME,null,null,null,null,null,null);
        validateCursor(AuthorCursor, AuthorValues, false);

        DbHelper.close();
    }

    static ContentValues createBookInfoValues()
    {
        ContentValues BookInfoValues = new ContentValues();
        BookInfoValues.put(BookEntry.COLUMN_BOOK_ID, TEST_BOOK_ID); //static final long TEST_BOOK_ID = 2279690981L;
        BookInfoValues.put(BookEntry.COLUMN_BOOK_SEARCH_QUERY, "PHP & MySQL");
        BookInfoValues.put(BookEntry.COLUMN_TITLE, "PHP & MySQL: The Missing Manual");
        BookInfoValues.put(BookEntry.COLUMN_SUBTITLE, "database-driven websites with PHP and MySQL");
        BookInfoValues.put(BookEntry.COLUMN_DESCRIPTION, "If you can build websites with CSS and JavaScript, this book takes you to the next level-creating dynamic, database-driven websites with PHP and MySQL. Learn how to build a database, manage your content, and interact with users through queries and web forms. With step-by-step tutorials, real-world examples, and jargon-free explanations, you\\u2019ll soon discover the power of server-side programming.");
        BookInfoValues.put(BookEntry.COLUMN_ISBN, 9780596515867L);
        BookInfoValues.put(BookEntry.COLUMN_IMAGE_LINK, "http://s.it-ebooks-api.info/3/php__mysql_the_missing_manual.jpg");

        return BookInfoValues;
    }

    static ContentValues createAuthorValues(long BookId)
    {
        // Create a new map of values, where column names are the keys
        ContentValues AuthorValues = new ContentValues();
        AuthorValues.put(AuthorEntry.COLUMN_BOOK_ID, BookId);
        AuthorValues.put(AuthorEntry.COLUMN_AUTHORNAME, "Brett McLaughlin");
        AuthorValues.put(AuthorEntry.COLUMN_YEAR, 2011);
        AuthorValues.put(AuthorEntry.COLUMN_PAGE, 498);
        AuthorValues.put(AuthorEntry.COLUMN_PUBLISHER, "O'Reilly Media");
        AuthorValues.put(AuthorEntry.COLUMN_DOWNLOAD_LINK, "http://filepi.com/i/qqkNNW2");
        AuthorValues.put(AuthorEntry.COLUMN_FILE_PATHNAME, "file Path name");

        return AuthorValues;
    }

    static void validateCursor(Cursor valueCursor, ContentValues expectedValues,boolean isIdConversionEnabled)
    {
        assertTrue(valueCursor.moveToFirst());
        Set<Map.Entry<String, Object>> valueSet = expectedValues.valueSet();
        for (Map.Entry<String, Object> entry : valueSet)
        {
            String columnName = entry.getKey();
            if (isIdConversionEnabled && (columnName.equals("BookId"))) //This check is for Cursor fetching BookId as _id for the sake of CursorAdaptors
                columnName = "_id";

            int idx = valueCursor.getColumnIndex(columnName);
            assertFalse(idx == -1);
            String expectedValue = entry.getValue().toString();
            String cursorValue = valueCursor.getString(idx);
            assertEquals(expectedValue, cursorValue);
        }
        valueCursor.close();
    }
}
