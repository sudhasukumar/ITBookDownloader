package com.example.sudha.itbookdownloader.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;

import java.util.HashMap;

import static com.example.sudha.itbookdownloader.data.ITBookDownloaderContract.AuthorEntry;
import static com.example.sudha.itbookdownloader.data.ITBookDownloaderContract.BookEntry;

/**
 * Created by Sudha on 1/6/2015.
 */
public class ITBookDownloaderProvider extends ContentProvider
{
    private static final String LOG_TAG = ITBookDownloaderProvider.class.getName();

    private ITBookDownloaderDbHelper DbHelper;
    private static final UriMatcher uriMatcher = buildUriMatcher();

    private static final int INCOMING_BOOK_COLLECTION_URI_INDICATOR = 1;
    private static final int INCOMING_AUTHOR_COLLECTION_URI_INDICATOR = 2;
    private static final int INCOMING_SINGLE_AUTHOR_URI_INDICATOR = 3;
    private static final int INCOMING_SINGLE_BOOK_URI_INDICATOR = 4;
    private static final int INCOMING_BOOK_SEARCH_COLLECTION_URI_INDICATOR = 5;

    private static final SQLiteQueryBuilder BookAndAuthorQueryBuilder;
    private static final SQLiteQueryBuilder BookSearchQueryBuilder;

    private static final HashMap<String,String> ITBDBookSearchProjectionMap = new HashMap<>();

    private static final HashMap<String,String> ITBDSingleBookDetailsProjectionMap = new HashMap<>();

    static
    {
        BookAndAuthorQueryBuilder = new SQLiteQueryBuilder();
        BookAndAuthorQueryBuilder.setTables(BookEntry.TABLE_NAME + " INNER JOIN " + AuthorEntry.TABLE_NAME
                                            + " ON " + BookEntry.TABLE_NAME + "." + BookEntry.COLUMN_BOOK_ID + " = " +
                                            AuthorEntry.TABLE_NAME + "." + AuthorEntry.COLUMN_BOOK_ID);


        BookSearchQueryBuilder = new SQLiteQueryBuilder();
        BookSearchQueryBuilder.setTables(BookEntry.TABLE_NAME);
        BookSearchQueryBuilder.setProjectionMap(ITBDBookSearchProjectionMap);



        ITBDBookSearchProjectionMap.put(    BookEntry._ID ,                     BookEntry.TABLE_NAME + "." + BookEntry.COLUMN_BOOK_ID + " AS " + BookEntry._ID);
        ITBDBookSearchProjectionMap.put(    BookEntry.COLUMN_TITLE,             BookEntry.COLUMN_TITLE);
        ITBDBookSearchProjectionMap.put(    BookEntry.COLUMN_SUBTITLE,          BookEntry.COLUMN_SUBTITLE);
        ITBDBookSearchProjectionMap.put(    BookEntry.COLUMN_DESCRIPTION,       BookEntry.COLUMN_DESCRIPTION);
        ITBDBookSearchProjectionMap.put(    BookEntry.COLUMN_ISBN,              BookEntry.COLUMN_ISBN);
        ITBDBookSearchProjectionMap.put(    BookEntry.COLUMN_IMAGE_LINK,        BookEntry.COLUMN_IMAGE_LINK);
        ITBDBookSearchProjectionMap.put(    BookEntry.COLUMN_BOOK_SEARCH_QUERY, BookEntry.COLUMN_BOOK_SEARCH_QUERY );



        ITBDSingleBookDetailsProjectionMap.put(    BookEntry._ID ,                     BookEntry.TABLE_NAME + "." + BookEntry.COLUMN_BOOK_ID + " AS " + BookEntry._ID);
        ITBDSingleBookDetailsProjectionMap.put(    BookEntry.COLUMN_TITLE,             BookEntry.COLUMN_TITLE);
        ITBDSingleBookDetailsProjectionMap.put(    BookEntry.COLUMN_SUBTITLE,          BookEntry.COLUMN_SUBTITLE);
        ITBDSingleBookDetailsProjectionMap.put(    BookEntry.COLUMN_DESCRIPTION,       BookEntry.COLUMN_DESCRIPTION);
        ITBDSingleBookDetailsProjectionMap.put(    BookEntry.COLUMN_ISBN,              BookEntry.COLUMN_ISBN);
        ITBDSingleBookDetailsProjectionMap.put(    BookEntry.COLUMN_IMAGE_LINK,        BookEntry.COLUMN_IMAGE_LINK);
        ITBDSingleBookDetailsProjectionMap.put(    BookEntry.COLUMN_BOOK_SEARCH_QUERY, BookEntry.COLUMN_BOOK_SEARCH_QUERY );
        ITBDSingleBookDetailsProjectionMap.put(    AuthorEntry.COLUMN_AUTHORNAME,      AuthorEntry.COLUMN_AUTHORNAME);
        ITBDSingleBookDetailsProjectionMap.put(    AuthorEntry.COLUMN_YEAR,            AuthorEntry.COLUMN_YEAR);
        ITBDSingleBookDetailsProjectionMap.put(    AuthorEntry.COLUMN_PAGE,            AuthorEntry.COLUMN_PAGE);
        ITBDSingleBookDetailsProjectionMap.put(    AuthorEntry.COLUMN_PUBLISHER,       AuthorEntry.COLUMN_PUBLISHER);
        ITBDSingleBookDetailsProjectionMap.put(    AuthorEntry.COLUMN_DOWNLOAD_LINK,   AuthorEntry.COLUMN_DOWNLOAD_LINK);
        ITBDSingleBookDetailsProjectionMap.put(    AuthorEntry.COLUMN_FILE_PATHNAME,   AuthorEntry.COLUMN_FILE_PATHNAME);

    }

    private static final String BOOKENTRY_BOOK_ID_SELECTION = BookEntry.TABLE_NAME + "." + BookEntry.COLUMN_BOOK_ID + " = ? ";
    private static final String AUTHORENTRY_BOOK_ID_SELECTION = AuthorEntry.TABLE_NAME + "." + AuthorEntry.COLUMN_BOOK_ID + " = ? ";
    private static final String BOOKENTRY_SEARCH_QUERY_SELECTION = BookEntry.TABLE_NAME + "." + BookEntry.COLUMN_BOOK_SEARCH_QUERY + " LIKE ? ";

    private Cursor getBookCollection(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder)
    {
        Log.d(LOG_TAG, "getBookCollection : ");
        return BookSearchQueryBuilder.query(DbHelper.getReadableDatabase(), projection,selection,selectionArgs,null,null,sortOrder);
    }

    private Cursor getBookInfoAndAuthor(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder)
    {
        Log.d(LOG_TAG, "getBookInfoAndAuthor : ");
        selection = BOOKENTRY_BOOK_ID_SELECTION;
        String BookId = BookEntry.getBookIdFromUri(uri);
        selectionArgs = new String[]{BookId};
        BookAndAuthorQueryBuilder.setProjectionMap(ITBDSingleBookDetailsProjectionMap);
        return BookAndAuthorQueryBuilder.query(DbHelper.getReadableDatabase(), projection, selection, selectionArgs, null, null, sortOrder);
    }

    private Cursor getBookCollectionForSearchQuery(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder)
    {
        selection = BOOKENTRY_SEARCH_QUERY_SELECTION;
        String SearchQuery = BookEntry.getBookSearchQueryFromUri(uri);
        selectionArgs = new String[]{"%"+SearchQuery+"%"};
        Log.d(LOG_TAG, "getBookCollectionForSearchQuery : " + SearchQuery);
        return BookSearchQueryBuilder.query(DbHelper.getReadableDatabase(), projection,selection,selectionArgs,null,null,sortOrder);
        //return DbHelper.getReadableDatabase().query(BookEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
    }

    @Override
    public boolean onCreate()
    {
        DbHelper = new ITBookDownloaderDbHelper(getContext());
        Log.d(LOG_TAG, "onCreate : ");
        return true;
    }

    private static UriMatcher buildUriMatcher()
    {
        final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(ITBookDownloaderContract.CONTENT_AUTHORITY, "books", INCOMING_BOOK_COLLECTION_URI_INDICATOR); // to insert into books table
        uriMatcher.addURI(ITBookDownloaderContract.CONTENT_AUTHORITY, "/books/search/*", INCOMING_BOOK_SEARCH_COLLECTION_URI_INDICATOR); // to query into books table that match searchQuery String
        uriMatcher.addURI(ITBookDownloaderContract.CONTENT_AUTHORITY, "authors", INCOMING_AUTHOR_COLLECTION_URI_INDICATOR); //only to delete all records in the authors table
        uriMatcher.addURI(ITBookDownloaderContract.CONTENT_AUTHORITY, "book", INCOMING_SINGLE_AUTHOR_URI_INDICATOR); // only to insert values into Author table
        uriMatcher.addURI(ITBookDownloaderContract.CONTENT_AUTHORITY, "book/#", INCOMING_SINGLE_BOOK_URI_INDICATOR); // to query both books and author table for complete book information
        Log.d(LOG_TAG, "buildUriMatcher : ");
        return uriMatcher;
    }

    @Override
    public String getType(Uri uri)
    {
        switch (uriMatcher.match(uri))
        {
            case INCOMING_BOOK_COLLECTION_URI_INDICATOR:
                return BookEntry.CONTENT_TYPE;

            case INCOMING_BOOK_SEARCH_COLLECTION_URI_INDICATOR:
                return BookEntry.CONTENT_TYPE;

            case INCOMING_AUTHOR_COLLECTION_URI_INDICATOR:
                return AuthorEntry.CONTENT_AUTHORS_TYPE;

            case INCOMING_SINGLE_AUTHOR_URI_INDICATOR:
                return AuthorEntry.CONTENT_ITEM_TYPE;

            case INCOMING_SINGLE_BOOK_URI_INDICATOR:
                return BookEntry.CONTENT_ITEM_TYPE;

            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder)
    {
        Cursor retCursor;
        Log.d(LOG_TAG, "query : ");
        final SQLiteDatabase db = DbHelper.getReadableDatabase();
        final int match = uriMatcher.match(uri);
        switch (match)
        {
            case INCOMING_BOOK_COLLECTION_URI_INDICATOR:
                retCursor = getBookCollection(uri, projection, selection, selectionArgs, sortOrder);
                break;

            case INCOMING_BOOK_SEARCH_COLLECTION_URI_INDICATOR:
                retCursor = getBookCollectionForSearchQuery(uri, projection, selection, selectionArgs, sortOrder);
                break;

            case INCOMING_SINGLE_BOOK_URI_INDICATOR:
                retCursor = getBookInfoAndAuthor(uri, projection, selection, selectionArgs, sortOrder);
                break;

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        retCursor.setNotificationUri(getContext().getContentResolver(), uri);

        return retCursor;
    }




    @Override
    public Uri insert(Uri uri, ContentValues values)
    {
        Log.d(LOG_TAG, "insert : ");
        final SQLiteDatabase db = DbHelper.getWritableDatabase();
        final int match = uriMatcher.match(uri);
        Uri insertedBookUri = uri ;

        switch (match)
        {
            case INCOMING_BOOK_COLLECTION_URI_INDICATOR:
            {
                long _id = db.insert(BookEntry.TABLE_NAME, null, values);

                if (_id > 0)
                    insertedBookUri  = BookEntry.buildBookIdUri(_id);
                else
                    throw new SQLException("Failed to insert row into : " + uri);
                break;
            }

            case INCOMING_SINGLE_BOOK_URI_INDICATOR:
            {
                long _id = 0;

                if (values.getAsString(AuthorEntry.COLUMN_AUTHORNAME)!= null)
                {
                    _id = db.insert(AuthorEntry.TABLE_NAME, null, values);
                    if (_id > 0)
                        insertedBookUri  = AuthorEntry.buildAuthorBookIdUri(_id);
                    else
                        throw new SQLException("Failed to insert row into : " + uri);
                }
                else if (values.getAsLong(BookEntry.COLUMN_ISBN)!= null)
                {
                    _id = db.insert(BookEntry.TABLE_NAME, null, values);
                    if (_id > 0)
                        insertedBookUri  = BookEntry.buildBookIdUri(_id);
                    else
                        throw new SQLException("Failed to insert row into : " + uri);
                }


                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return insertedBookUri ;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs)
    {
        Log.d(LOG_TAG, "delete : ");
        final SQLiteDatabase db = DbHelper.getWritableDatabase();
        final int match = uriMatcher.match(uri);
        int rowsDeleted;
        switch (match)
        {
            case INCOMING_BOOK_COLLECTION_URI_INDICATOR:
                rowsDeleted = db.delete(BookEntry.TABLE_NAME, selection, selectionArgs);
                break;

            case INCOMING_AUTHOR_COLLECTION_URI_INDICATOR:
                rowsDeleted = db.delete(AuthorEntry.TABLE_NAME, selection, selectionArgs);
                break;

            case INCOMING_SINGLE_BOOK_URI_INDICATOR:
                selection = BOOKENTRY_BOOK_ID_SELECTION;
                String BookId = BookEntry.getBookIdFromUri(uri);
                selectionArgs = new String[]{BookId};
                rowsDeleted = db.delete(BookEntry.TABLE_NAME, selection, selectionArgs);
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
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs)
    {
        Log.d(LOG_TAG, "update : ");
        final SQLiteDatabase db = DbHelper.getWritableDatabase();
        final int match = uriMatcher.match(uri);
        int rowsUpdated;

        switch (match)
        {
            case INCOMING_BOOK_COLLECTION_URI_INDICATOR:
                rowsUpdated = db.update(BookEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            case INCOMING_SINGLE_BOOK_URI_INDICATOR:
                selection = AUTHORENTRY_BOOK_ID_SELECTION;
                String BookId = AuthorEntry.getBookIdFromUri(uri);
                selectionArgs = new String[]{BookId};
                rowsUpdated = db.update(AuthorEntry.TABLE_NAME, values, selection, selectionArgs);
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
    public int bulkInsert(Uri uri, @NonNull ContentValues[] values)
    {
        Log.d(LOG_TAG, "bulkInsert : ");
        final SQLiteDatabase db = DbHelper.getWritableDatabase();
        final int match = uriMatcher.match(uri);
        switch (match)
        {
            case INCOMING_BOOK_COLLECTION_URI_INDICATOR:
                db.beginTransaction();
                int returnCount = 0;
                try
                {
                    for (ContentValues value : values)
                    {
                        long _id = db.insert(BookEntry.TABLE_NAME, null, value);
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
