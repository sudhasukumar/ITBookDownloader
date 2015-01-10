package com.example.sudha.itbookdownloader.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

/**
 * Created by Sudha on 1/6/2015.
 */
public class ITBookDownloaderProvider extends ContentProvider
{
    private static final String LOG_TAG = ITBookDownloaderProvider.class.getName();
    private ITBookDownloaderDbHelper DbHelper;
    private static final UriMatcher uriMatcher = buildUriMatcher();

    private static final int BOOK_INFORMATION = 1;          //...../search/{query}.../search/{query}/page/{number}...
    private static final int AUTHOR_INFORMATION = 4;        //..../book/{id}

    @Override
    public boolean onCreate()
    {
        DbHelper = new ITBookDownloaderDbHelper(getContext());
        return true;
    }

    private static UriMatcher buildUriMatcher()
    {
        final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = ITBookDownloaderContract.CONTENT_AUTHORITY;

        // For each type of URI you want to add, create a corresponding code.
        uriMatcher.addURI(authority, ITBookDownloaderContract.PATH_BOOKINFO, BOOK_INFORMATION);
        uriMatcher.addURI(authority, ITBookDownloaderContract.PATH_BOOKINFO + "/*", BOOK_INFORMATION);
        uriMatcher.addURI(authority, ITBookDownloaderContract.PATH_BOOKINFO + "/*/*", BOOK_INFORMATION);
        uriMatcher.addURI(authority, ITBookDownloaderContract.PATH_BOOKINFO + "/*/*/*/#", BOOK_INFORMATION);
        uriMatcher.addURI(authority, ITBookDownloaderContract.PATH_AUTHOR + "/book/#", AUTHOR_INFORMATION);

        return uriMatcher;
    }

    @Override
    public String getType(Uri uri)
    {
        // Use the Uri Matcher to determine what kind of URI this is.
        final int match = uriMatcher.match(uri);

        switch (match)
        {
            case BOOK_INFORMATION:
                return ITBookDownloaderContract.BookEntry.CONTENT_TYPE;
            case AUTHOR_INFORMATION:
                return ITBookDownloaderContract.AuthorEntry.CONTENT_ITEM_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder)
    {
        Cursor retCursor;
        switch (uriMatcher.match(uri))
        {
            // "search/{query}" or "search/{query}/page/{page number}"
            case BOOK_INFORMATION:
            {
                //retCursor = getWeatherByLocationSettingAndDate(uri, projection, sortOrder);
                retCursor = DbHelper.getReadableDatabase().query(ITBookDownloaderContract.BookEntry.TABLE_NAME,projection,selection,selectionArgs,null,null,sortOrder);
                break;

            }
            // "book/{BookId}"
            case AUTHOR_INFORMATION:
            {
                retCursor = DbHelper.getReadableDatabase().query(ITBookDownloaderContract.AuthorEntry.TABLE_NAME,projection,selection,selectionArgs,null,null,sortOrder);
                break;
            }
            default:
            throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values)
    {
        final SQLiteDatabase db = DbHelper.getWritableDatabase();
        final int match = uriMatcher.match(uri);
        Uri returnUri;

        switch (match)
        {
            case BOOK_INFORMATION:
            {
                long _id = db.insert(ITBookDownloaderContract.BookEntry.TABLE_NAME, null, values);
                if (_id > 0)
                    returnUri = ITBookDownloaderContract.BookEntry.buildBookInfoInsertUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            case AUTHOR_INFORMATION:
            {
                long _id = db.insert(ITBookDownloaderContract.AuthorEntry.TABLE_NAME, null, values);
                if (_id > 0)
                    returnUri = ITBookDownloaderContract.AuthorEntry.buildAuthorInsertUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs)
    {
        final SQLiteDatabase db = DbHelper.getWritableDatabase();
        final int match = uriMatcher.match(uri);
        int rowsDeleted;
        switch (match)
        {
            case BOOK_INFORMATION:
                rowsDeleted = db.delete(ITBookDownloaderContract.BookEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case AUTHOR_INFORMATION:
                rowsDeleted = db.delete(ITBookDownloaderContract.AuthorEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        // Because a null deletes all rows
        if (selection == null || rowsDeleted != 0)
        {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;
        //return 0;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs)
    {
        final SQLiteDatabase db = DbHelper.getWritableDatabase();
        final int match = uriMatcher.match(uri);
        int rowsUpdated;

        switch (match)
        {
            case BOOK_INFORMATION:
                rowsUpdated = db.update(ITBookDownloaderContract.BookEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            case AUTHOR_INFORMATION:
                rowsUpdated = db.update(ITBookDownloaderContract.AuthorEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if (rowsUpdated != 0)
        {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values)
    {
        final SQLiteDatabase db = DbHelper.getWritableDatabase();
        final int match = uriMatcher.match(uri);
        switch (match)
        {
            case BOOK_INFORMATION:
                db.beginTransaction();
                int returnCount = 0;
                try
                {
                    for (ContentValues value : values)
                    {
                        long _id = db.insert(ITBookDownloaderContract.BookEntry.TABLE_NAME, null, value);
                        if (_id != -1)
                        {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                }
                finally
                {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;
            default:
                return super.bulkInsert(uri, values);
        }
    }
}
