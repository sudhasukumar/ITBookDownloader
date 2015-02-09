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

    private static final int BOOK_COLLECTION_URI              = 1;
    private static final int BOOK_COLLECTION_FOR_SEARCH_QUERY = 2;
    private static final int BOOK_ID_IN_BOOKS                 = 3;
    private static final int AUTHOR_COLLECTION_URI            = 4;
    private static final int BOOK_ID_IN_AUTHORS               = 5;
    private static final int BOOK_ID_IN_BOOKS_AND_AUTHORS     = 6;

    private static final SQLiteQueryBuilder BooksAndAuthorsQueryBuilder;
    private static final SQLiteQueryBuilder BooksTableQueryBuilder;
    private static final SQLiteQueryBuilder AuthorsTableQueryBuilder;

    private static final HashMap<String, String> BooksProjectionMap      = new HashMap<>();
    private static final HashMap<String, String> AuthorsProjectionMap    = new HashMap<>();
    private static final HashMap<String, String> JoinBookIdProjectionMap = new HashMap<>();

    static
    {
        BooksProjectionMap.put(BookEntry._ID, BookEntry.TABLE_NAME + "." + BookEntry.COLUMN_BOOK_ID + " AS " + BookEntry._ID);
        BooksProjectionMap.put(BookEntry.COLUMN_TITLE, BookEntry.COLUMN_TITLE);
        BooksProjectionMap.put(BookEntry.COLUMN_SUBTITLE, BookEntry.COLUMN_SUBTITLE);
        BooksProjectionMap.put(BookEntry.COLUMN_DESCRIPTION, BookEntry.COLUMN_DESCRIPTION);
        BooksProjectionMap.put(BookEntry.COLUMN_ISBN, BookEntry.COLUMN_ISBN);
        BooksProjectionMap.put(BookEntry.COLUMN_IMAGE_LINK, BookEntry.COLUMN_IMAGE_LINK);
        BooksProjectionMap.put(BookEntry.COLUMN_BOOK_SEARCH_QUERY, BookEntry.COLUMN_BOOK_SEARCH_QUERY);

        AuthorsProjectionMap.put(AuthorEntry._ID, AuthorEntry.TABLE_NAME + "." + AuthorEntry.COLUMN_BOOK_ID + " AS " + AuthorEntry._ID);
        AuthorsProjectionMap.put(AuthorEntry.COLUMN_WEBSITE_BOOK_NUMBER, AuthorEntry.COLUMN_WEBSITE_BOOK_NUMBER);
        AuthorsProjectionMap.put(AuthorEntry.COLUMN_AUTHOR_ISBN, AuthorEntry.COLUMN_AUTHOR_ISBN);
        AuthorsProjectionMap.put(AuthorEntry.COLUMN_AUTHORNAME, AuthorEntry.COLUMN_AUTHORNAME);
        AuthorsProjectionMap.put(AuthorEntry.COLUMN_YEAR, AuthorEntry.COLUMN_YEAR);
        AuthorsProjectionMap.put(AuthorEntry.COLUMN_PAGE, AuthorEntry.COLUMN_PAGE);
        AuthorsProjectionMap.put(AuthorEntry.COLUMN_PUBLISHER, AuthorEntry.COLUMN_PUBLISHER);
        AuthorsProjectionMap.put(AuthorEntry.COLUMN_DOWNLOAD_LINK, AuthorEntry.COLUMN_DOWNLOAD_LINK);
        AuthorsProjectionMap.put(AuthorEntry.COLUMN_FILE_FORMAT, AuthorEntry.COLUMN_FILE_FORMAT);
        AuthorsProjectionMap.put(AuthorEntry.COLUMN_FILE_PATHNAME, AuthorEntry.COLUMN_FILE_PATHNAME);

        JoinBookIdProjectionMap.put(BookEntry._ID, BookEntry.TABLE_NAME + "." + BookEntry.COLUMN_BOOK_ID + " AS " + BookEntry._ID);
        JoinBookIdProjectionMap.put(BookEntry.COLUMN_TITLE, BookEntry.COLUMN_TITLE);
        JoinBookIdProjectionMap.put(BookEntry.COLUMN_SUBTITLE, BookEntry.COLUMN_SUBTITLE);
        JoinBookIdProjectionMap.put(BookEntry.COLUMN_DESCRIPTION, BookEntry.COLUMN_DESCRIPTION);
        JoinBookIdProjectionMap.put(BookEntry.COLUMN_ISBN, BookEntry.COLUMN_ISBN);
        JoinBookIdProjectionMap.put(BookEntry.COLUMN_IMAGE_LINK, BookEntry.COLUMN_IMAGE_LINK);
        JoinBookIdProjectionMap.put(BookEntry.COLUMN_BOOK_SEARCH_QUERY, BookEntry.COLUMN_BOOK_SEARCH_QUERY);
        JoinBookIdProjectionMap.put(AuthorEntry.COLUMN_WEBSITE_BOOK_NUMBER, AuthorEntry.COLUMN_WEBSITE_BOOK_NUMBER);
        JoinBookIdProjectionMap.put(AuthorEntry.COLUMN_AUTHOR_ISBN, AuthorEntry.COLUMN_AUTHOR_ISBN);
        JoinBookIdProjectionMap.put(AuthorEntry.COLUMN_AUTHORNAME, AuthorEntry.COLUMN_AUTHORNAME);
        JoinBookIdProjectionMap.put(AuthorEntry.COLUMN_YEAR, AuthorEntry.COLUMN_YEAR);
        JoinBookIdProjectionMap.put(AuthorEntry.COLUMN_PAGE, AuthorEntry.COLUMN_PAGE);
        JoinBookIdProjectionMap.put(AuthorEntry.COLUMN_PUBLISHER, AuthorEntry.COLUMN_PUBLISHER);
        JoinBookIdProjectionMap.put(AuthorEntry.COLUMN_DOWNLOAD_LINK, AuthorEntry.COLUMN_DOWNLOAD_LINK);
        JoinBookIdProjectionMap.put(AuthorEntry.COLUMN_FILE_FORMAT, AuthorEntry.COLUMN_FILE_FORMAT);
        JoinBookIdProjectionMap.put(AuthorEntry.COLUMN_FILE_PATHNAME, AuthorEntry.COLUMN_FILE_PATHNAME);

        BooksTableQueryBuilder = new SQLiteQueryBuilder();
        BooksTableQueryBuilder.setTables(BookEntry.TABLE_NAME);
        BooksTableQueryBuilder.setProjectionMap(BooksProjectionMap);

        AuthorsTableQueryBuilder = new SQLiteQueryBuilder();
        AuthorsTableQueryBuilder.setTables(AuthorEntry.TABLE_NAME);
        AuthorsTableQueryBuilder.setProjectionMap(AuthorsProjectionMap);

        BooksAndAuthorsQueryBuilder = new SQLiteQueryBuilder();
        BooksAndAuthorsQueryBuilder.setTables(BookEntry.TABLE_NAME + " INNER JOIN " + AuthorEntry.TABLE_NAME + " ON " +
                                              BookEntry.TABLE_NAME + "." + BookEntry.COLUMN_BOOK_ID + " = " + AuthorEntry.TABLE_NAME + "." + AuthorEntry.COLUMN_BOOK_ID);
        BooksAndAuthorsQueryBuilder.setProjectionMap(JoinBookIdProjectionMap);

    }

    private static final String BOOKS_SEARCH_QUERY_SELECTION = BookEntry.TABLE_NAME + "." + BookEntry.COLUMN_BOOK_SEARCH_QUERY + " LIKE ? ";
    private static final String BOOKS_BOOK_ID_SELECTION      = BookEntry.TABLE_NAME + "." + BookEntry.COLUMN_BOOK_ID + " = ? ";
    private static final String AUTHORS_BOOK_ID_SELECTION    = AuthorEntry.TABLE_NAME + "." + AuthorEntry.COLUMN_BOOK_ID + " = ? ";

    private Cursor getBooksCollection(String[] projection, String selection, String[] selectionArgs, String sortOrder)
    {
        Log.d(LOG_TAG, "getBooksCollection : ");
        return BooksTableQueryBuilder.query(DbHelper.getReadableDatabase(), projection, selection, selectionArgs, null, null, sortOrder);
    }

    private Cursor getAuthorsCollection(String[] projection, String selection, String[] selectionArgs, String sortOrder)
    {
        Log.d(LOG_TAG, "getAuthorsCollection : ");
        return AuthorsTableQueryBuilder.query(DbHelper.getReadableDatabase(), projection, selection, selectionArgs, null, null, sortOrder);
    }

    private Cursor getBookInfoAndAuthorForId(Uri uri, String[] projection, String sortOrder)
    {
        Log.d(LOG_TAG, "getBookInfoAndAuthorForId : ");
        String selection = BOOKS_BOOK_ID_SELECTION;
        String BookId = BookEntry.getBookIdFromUri(uri);
        String[] selectionArgs = new String[]{BookId};
        BooksAndAuthorsQueryBuilder.setProjectionMap(JoinBookIdProjectionMap);
        return BooksAndAuthorsQueryBuilder.query(DbHelper.getReadableDatabase(), projection, selection, selectionArgs, null, null, sortOrder);
    }

    private Cursor getBooksBookIdInfo(Uri uri, String[] projection, String sortOrder)
    {
        Log.d(LOG_TAG, "getBookIdInfo : ");
        String selection = BOOKS_BOOK_ID_SELECTION;
        String BookId = BookEntry.getBookIdFromUri(uri);
        String[] selectionArgs = new String[]{BookId};
        BooksTableQueryBuilder.setProjectionMap(BooksProjectionMap);
        return BooksTableQueryBuilder.query(DbHelper.getReadableDatabase(), projection, selection, selectionArgs, null, null, sortOrder);
    }

    private Cursor getAuthorsBookIdInfo(Uri uri, String[] projection, String sortOrder)
    {
        Log.d(LOG_TAG, "getBookIdInfo : ");
        String selection = AUTHORS_BOOK_ID_SELECTION;
        String BookId = AuthorEntry.getAuthorsBookIdFromUri(uri);
        String[] selectionArgs = new String[]{BookId};
        AuthorsTableQueryBuilder.setProjectionMap(AuthorsProjectionMap);
        return AuthorsTableQueryBuilder.query(DbHelper.getReadableDatabase(), projection, selection, selectionArgs, null, null, sortOrder);
    }

    private Cursor getBookCollectionForSearchQuery(Uri uri, String[] projection, String sortOrder)
    {
        String selection = BOOKS_SEARCH_QUERY_SELECTION;
        String SearchQuery = BookEntry.getBookSearchQueryFromUri(uri);
        String[] selectionArgs = new String[]{"%" + SearchQuery + "%"};
        Log.d(LOG_TAG, "getBookCollectionForSearchQuery : " + SearchQuery);
        BooksTableQueryBuilder.setProjectionMap(BooksProjectionMap);
        return BooksTableQueryBuilder
                       .query(DbHelper.getReadableDatabase(), projection, selection, selectionArgs, null, null, sortOrder);
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
/*
    /books - to query , to bulk insert and to bulk delete all,  in books table
	/books/search/* - to query into books table that match searchQuery String
	/books/book/# - to query and to insert if the book id is in books table
	/authors/book/# - to query and to insert if the book id is in the authors table
	/authors - to query, to bulk  insert and bulk delete all, in authors table
	/book/# - to join query, to join insert and join delete that book id details into both books and authors table.

	Query : 									Insert :										Delete :
	/books										/books										    /books
	/books/search/* 				            /books/search/*				                    /books/search/*
	/books/book/#					            /books/book/#					                /books/book/#
	/authors									/authors									    /authors
	/authors/book/#				                /authors/book/#				                    /authors/book/#
	/book/#									    /book/#									        /book/#

    /books              - BOOK_COLLECTION_URI
	/books/search/*     - BOOK_COLLECTION_FOR_SEARCH_QUERY
	/books/book/#       - BOOK_ID_IN_AUTHORS
	/authors            - AUTHOR_COLLECTION_URI
	/authors/book/#     - BOOK_ID_IN_AUTHORS
	/book/#             - BOOK_ID_IN_BOOKS_AND_AUTHORS
*/
        final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher
                .addURI(ITBookDownloaderContract.CONTENT_AUTHORITY, "/books", BOOK_COLLECTION_URI); // to query,insert,delete into books table
        uriMatcher
                .addURI(ITBookDownloaderContract.CONTENT_AUTHORITY, "/books/search/*", BOOK_COLLECTION_FOR_SEARCH_QUERY); // to query,insert,delete into books for searchQuery String
        uriMatcher
                .addURI(ITBookDownloaderContract.CONTENT_AUTHORITY, "/books/book/#", BOOK_ID_IN_BOOKS); // to query,insert,delete books table for book id
        uriMatcher
                .addURI(ITBookDownloaderContract.CONTENT_AUTHORITY, "/authors", AUTHOR_COLLECTION_URI); // to query,insert,delete all records in the authors table
        uriMatcher
                .addURI(ITBookDownloaderContract.CONTENT_AUTHORITY, "/authors/book/#", BOOK_ID_IN_AUTHORS); // to query,insert,delete author table for book id
        uriMatcher
                .addURI(ITBookDownloaderContract.CONTENT_AUTHORITY, "/book/#", BOOK_ID_IN_BOOKS_AND_AUTHORS); // to query,insert,delete both books and authors table for complete book information


        Log.d(LOG_TAG, "buildUriMatcher : ");
        return uriMatcher;
    }

    @Override
    public String getType(Uri uri)
    {
        switch (uriMatcher.match(uri))
        {
            case BOOK_COLLECTION_URI:
                return BookEntry.CONTENT_BOOKS_DIR_TYPE;

            case BOOK_COLLECTION_FOR_SEARCH_QUERY:
                return BookEntry.CONTENT_BOOKS_DIR_TYPE;

            case BOOK_ID_IN_BOOKS:
                return BookEntry.CONTENT_BOOKS_ITEM_TYPE;

            case AUTHOR_COLLECTION_URI:
                return AuthorEntry.CONTENT_AUTHORS_DIR_TYPE;

            case BOOK_ID_IN_AUTHORS:
                return AuthorEntry.CONTENT_AUTHORS_ITEM_TYPE;

            case BOOK_ID_IN_BOOKS_AND_AUTHORS:
                return BookEntry.CONTENT_BOOKS_ITEM_TYPE;

            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder)
    {
        Cursor retCursor;
        Log.d(LOG_TAG, "ITBookDownloaderProvider query method : ");
        final int match = uriMatcher.match(uri);
        switch (match)
        {
            case BOOK_COLLECTION_URI:
                retCursor = getBooksCollection(projection, selection, selectionArgs, sortOrder);
                break;

            case BOOK_COLLECTION_FOR_SEARCH_QUERY:
                retCursor = getBookCollectionForSearchQuery(uri, projection, sortOrder);
                break;

            case BOOK_ID_IN_BOOKS:
                retCursor = getBooksBookIdInfo(uri, projection, sortOrder);
                break;

            case BOOK_ID_IN_AUTHORS:
                retCursor = getAuthorsBookIdInfo(uri, projection, sortOrder);
                break;

            case AUTHOR_COLLECTION_URI:
                retCursor = getAuthorsCollection(projection, selection, selectionArgs, sortOrder);
                break;

            case BOOK_ID_IN_BOOKS_AND_AUTHORS:
                retCursor = getBookInfoAndAuthorForId(uri, projection, sortOrder);
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
        Uri insertedUri = uri;
        switch (match)
        {
            case BOOK_COLLECTION_URI:
                insertedUri = insertIntoBooksTable(uri, values, db);
                break;

            case BOOK_COLLECTION_FOR_SEARCH_QUERY:
                insertedUri = insertIntoBooksTable(uri, values, db);
                break;

            case BOOK_ID_IN_BOOKS:
                insertedUri = insertIntoBooksTable(uri, values, db);
                break;

            case BOOK_ID_IN_AUTHORS:
                insertedUri = insertIntoAuthorsTable(uri, values, db);
                break;

            case AUTHOR_COLLECTION_URI:
                insertedUri = insertIntoAuthorsTable(uri, values, db);
                break;

            case BOOK_ID_IN_BOOKS_AND_AUTHORS:
                Log.d(LOG_TAG, "Join Insert not implemented yet : " + uri);
                break;

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return insertedUri;
    }

    private Uri insertIntoBooksTable(Uri uri, ContentValues values, SQLiteDatabase db)
    {
        long _id = 0;
        Uri insertedBookUri;
        try
        {
            _id = db.insert(BookEntry.TABLE_NAME, null, values);
        }
        catch ( Exception e )
        {
            Log.d(LOG_TAG, " Could Not Insert into Books Table : " + e.getMessage());
            e.printStackTrace();
        }
        if ( _id > 0 )
        {
            insertedBookUri = BookEntry.buildBooksIdUri(_id);
        }
        else
        {
            throw new SQLException("Failed to insert row into Books Table : " + uri);
        }
        return insertedBookUri;
    }

    private Uri insertIntoAuthorsTable(Uri uri, ContentValues values, SQLiteDatabase db)
    {
        long _id = 0;
        Uri insertedUri;
        try
        {
            _id = db.insert(AuthorEntry.TABLE_NAME, null, values);
        }
        catch ( Exception e )
        {
            Log.d(LOG_TAG, " Could Not Insert into Authors Table : " + e.getMessage());
            e.printStackTrace();
        }
        if ( _id > 0 )
        {
            insertedUri = AuthorEntry.buildAuthorsBookIdUri(_id);
        }
        else
        {
            throw new SQLException("Failed to insert row into Authors Table : " + uri);
        }
        return insertedUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs)
    {
        Log.d(LOG_TAG, " delete : ");
        final SQLiteDatabase db = DbHelper.getWritableDatabase();
        final int match = uriMatcher.match(uri);
        int rowsDeleted;
        switch (match)
        {
            case BOOK_COLLECTION_URI:   // Deletes all records in Books Table
                rowsDeleted = db.delete(BookEntry.TABLE_NAME, selection, selectionArgs);
                break;

            case BOOK_COLLECTION_FOR_SEARCH_QUERY:  // Deletes all records matching the search query
                selection = BOOKS_SEARCH_QUERY_SELECTION;
                String SearchQuery = BookEntry.getBookSearchQueryFromUri(uri);
                selectionArgs = new String[]{SearchQuery};
                rowsDeleted = db.delete(BookEntry.TABLE_NAME, selection, selectionArgs);
                break;

            case BOOK_ID_IN_BOOKS:   // Deletes Single Book matching the Id in Books table
                selection = BOOKS_BOOK_ID_SELECTION;
                String BookId = BookEntry.getBookIdFromUri(uri);
                selectionArgs = new String[]{BookId};
                rowsDeleted = db.delete(BookEntry.TABLE_NAME, selection, selectionArgs);
                break;

            case AUTHOR_COLLECTION_URI:   // Deletes all records in Authors Table
                rowsDeleted = db.delete(AuthorEntry.TABLE_NAME, selection, selectionArgs);
                break;

            case BOOK_ID_IN_AUTHORS:    // Deletes Single Book matching the Id in Authors table
                selection = AUTHORS_BOOK_ID_SELECTION;
                BookId = AuthorEntry.getAuthorsBookIdFromUri(uri);
                selectionArgs = new String[]{BookId};
                rowsDeleted = db.delete(AuthorEntry.TABLE_NAME, selection, selectionArgs);
                break;

            case BOOK_ID_IN_BOOKS_AND_AUTHORS:  // Deletes Single Book matching the Id in Books and Authors table
                rowsDeleted = joinDelete(uri, db);
                break;


            default:
                throw new UnsupportedOperationException(" Unknown uri in delete: " + uri);
        }
        // Because a null deletes all rows
        if ( selection == null || rowsDeleted != 0 )
        {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;
    }

    private int joinDelete(Uri uri, SQLiteDatabase db)
    {
        int rowsDeleted;
        String selection = BOOKS_BOOK_ID_SELECTION;
        String BookId = BookEntry.getBookIdFromUri(uri);
        String[] selectionArgs = new String[]{BookId};
        int rowsDeletedInBooks = db.delete(BookEntry.TABLE_NAME, selection, selectionArgs);
        selection = AUTHORS_BOOK_ID_SELECTION;
        int rowsDeletedInAuthors = db.delete(AuthorEntry.TABLE_NAME, selection, selectionArgs);

        if ( (rowsDeletedInBooks == 1) && (rowsDeletedInAuthors == 1) )
        {
            rowsDeleted = 1;
        }
        else
        {
            rowsDeleted = 0;
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
/*
    case BOOK_COLLECTION_URI :
    case BOOK_COLLECTION_FOR_SEARCH_QUERY :
    case BOOK_ID_IN_BOOKS :
    case BOOK_ID_IN_AUTHORS :
    case AUTHOR_COLLECTION_URI :
    case BOOK_ID_IN_BOOKS_AND_AUTHORS :

*/
        switch (match)
        {
            case BOOK_COLLECTION_URI:
                rowsUpdated = db.update(BookEntry.TABLE_NAME, values, selection, selectionArgs);
                break;

            case BOOK_COLLECTION_FOR_SEARCH_QUERY:
                String SearchQuery = BookEntry.getBookSearchQueryFromUri(uri);
                selectionArgs = new String[]{SearchQuery};
                rowsUpdated = db.update(BookEntry.TABLE_NAME, values, BOOKS_SEARCH_QUERY_SELECTION, selectionArgs);
                break;

            case BOOK_ID_IN_BOOKS:
                String BookId = BookEntry.getBookIdFromUri(uri);
                selectionArgs = new String[]{BookId};
                rowsUpdated = db.update(BookEntry.TABLE_NAME, values, BOOKS_BOOK_ID_SELECTION, selectionArgs);
                break;

            case BOOK_ID_IN_AUTHORS:
                selection = AUTHORS_BOOK_ID_SELECTION;
                BookId = AuthorEntry.getAuthorsBookIdFromUri(uri);
                selectionArgs = new String[]{BookId};
                rowsUpdated = db.update(AuthorEntry.TABLE_NAME, values, selection, selectionArgs);
                break;

            case AUTHOR_COLLECTION_URI:
                rowsUpdated = db.update(AuthorEntry.TABLE_NAME, values, selection, selectionArgs);
                break;

            case BOOK_ID_IN_BOOKS_AND_AUTHORS:
                Log.d(LOG_TAG, "BOOK_ID_IN_BOOKS_AND_AUTHORS Not Implemented yet for Update");
                rowsUpdated = 0;
                break;

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if ( rowsUpdated != 0 )
        {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;
    }

    @Override
    public int bulkInsert(Uri uri, @NonNull ContentValues[] values)
    {
        Log.d(LOG_TAG, " BulkInsert : ");
        final SQLiteDatabase db = DbHelper.getWritableDatabase();
        final int match = uriMatcher.match(uri);
        switch (match)
        {
            case BOOK_COLLECTION_URI:
                db.beginTransaction();
                int returnCount = 0;
                try
                {
                    for ( ContentValues value : values )
                    {
                        long _id = db.insert(BookEntry.TABLE_NAME, null, value);

                        if ( _id != -1 )
                        {
                            Log.d(LOG_TAG, " BulkInsert : IDs inserted " + _id);
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
