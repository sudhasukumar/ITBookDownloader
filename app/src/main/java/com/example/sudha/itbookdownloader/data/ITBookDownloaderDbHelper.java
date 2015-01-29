package com.example.sudha.itbookdownloader.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.example.sudha.itbookdownloader.data.ITBookDownloaderContract.BookEntry;
import com.example.sudha.itbookdownloader.data.ITBookDownloaderContract.AuthorEntry;

/**
 * Created by Sudha on 1/6/2015.
 */
public class ITBookDownloaderDbHelper extends SQLiteOpenHelper
{
    private static final String LOG_TAG = ITBookDownloaderDbHelper.class.getName();
    private static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "bookdownloader.db";
    SQLiteDatabase sqLiteDatabase;

    public ITBookDownloaderDbHelper(Context context)
    {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase)
    {
        final String SQL_CREATE_BOOKINFO_TABLE = "CREATE TABLE " + BookEntry.TABLE_NAME                     + " (" +
                                                                 BookEntry.COLUMN_BOOK_ID                   + " INTEGER PRIMARY KEY ," +
                                                                 BookEntry.COLUMN_BOOK_SEARCH_QUERY         + " TEXT NOT NULL ," +
                                                                 BookEntry.COLUMN_TITLE                     + " TEXT NOT NULL , " +
                                                                 BookEntry.COLUMN_SUBTITLE                  + " TEXT NOT NULL , " +
                                                                 BookEntry.COLUMN_DESCRIPTION               + " TEXT NOT NULL , " +
                                                                 BookEntry.COLUMN_ISBN                      + " INTEGER NOT NULL , " +
                                                                 BookEntry.COLUMN_IMAGE_LINK                + " TEXT NOT NULL , " +
                                                    " UNIQUE (" + BookEntry.COLUMN_BOOK_ID + ", " + BookEntry.COLUMN_ISBN         + ") ON CONFLICT REPLACE );";

        final String SQL_CREATE_AUTHOR_TABLE = "CREATE TABLE " + AuthorEntry.TABLE_NAME             + " (" +
                                                                AuthorEntry.COLUMN_BOOK_ID          + " INTEGER PRIMARY KEY, " +
                                                                AuthorEntry.COLUMN_AUTHORNAME       + " TEXT NOT NULL, " +
                                                                AuthorEntry.COLUMN_YEAR             + " INTEGER NOT NULL, " +
                                                                AuthorEntry.COLUMN_PAGE             + " INTEGER NOT NULL, " +
                                                                AuthorEntry.COLUMN_PUBLISHER        + " TEXT NOT NULL, " +
                                                                AuthorEntry.COLUMN_DOWNLOAD_LINK    + " TEXT NOT NULL, " +
                                                                AuthorEntry.COLUMN_FILE_PATHNAME    + " TEXT NOT NULL, " +
                     " FOREIGN KEY (" + AuthorEntry.COLUMN_BOOK_ID          + ") REFERENCES " + BookEntry.TABLE_NAME + " (" + BookEntry.COLUMN_BOOK_ID + ") ON DELETE CASCADE , " +
                     " UNIQUE (" + AuthorEntry.COLUMN_BOOK_ID + ", " + AuthorEntry.COLUMN_DOWNLOAD_LINK + ", "  + AuthorEntry.COLUMN_FILE_PATHNAME + ") ON CONFLICT REPLACE);";

        sqLiteDatabase.execSQL(SQL_CREATE_BOOKINFO_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_AUTHOR_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + BookEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + AuthorEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }

    @Override
    public void onConfigure(SQLiteDatabase db)
    {
        //to turn on the foreign key constraints
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }
}
