package com.example.sudha.itbookdownloader.data;

import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by Sudha on 1/6/2015.
 */
public class ITBookDownloaderContract
{
    private static final String LOG_TAG = ITBookDownloaderContract.class.getName();
    public static final String CONTENT_AUTHORITY = "com.example.sudha.itbookdownloader";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static final String PATH_BOOKS = "books";
    public static final String PATH_BOOK = "book";
    public static final String PATH_AUTHORS = "authors";

    public static final class BookEntry implements BaseColumns
    {
        public static final Uri BOOKS_CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_BOOKS).build();
        public static final Uri BOOK_ID_CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_BOOK).build();
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/" + CONTENT_AUTHORITY + "/" + PATH_BOOKS;
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/" + CONTENT_AUTHORITY + "/" + PATH_BOOK;

        public static final String TABLE_NAME = "Books";
        public static final String COLUMN_BOOK_ID = "BookId";
        public static final String COLUMN_TITLE = "BookTitle";
        public static final String COLUMN_SUBTITLE = "BookSubtitle";
        public static final String COLUMN_DESCRIPTION = "BookDescription";
        public static final String COLUMN_ISBN = "ISBN";
        public static final String COLUMN_IMAGE_LINK = "Imagelink";
        public static final String COLUMN_BOOK_SEARCH_QUERY = "BookSearchQuery";

        public static Uri buildBookIdUri(long BookId)
        {
            return ContentUris.withAppendedId(BOOK_ID_CONTENT_URI, BookId);
        }

        public static String getBookIdFromUri(Uri uri)
        {
            return String.valueOf(ContentUris.parseId(uri));
        }
    }

    public static final class AuthorEntry implements BaseColumns
    {
        public static final Uri AUTHORS_CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_AUTHORS).build();
        public static final Uri BOOK_ID_CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_BOOK).build();
        public static final String CONTENT_AUTHORS_TYPE = "vnd.android.cursor.dir/" + CONTENT_AUTHORITY + "/" + PATH_AUTHORS;
        //public static final String CONTENT_TYPE = "vnd.android.cursor.dir/" + CONTENT_AUTHORITY + "/" + PATH_BOOKS;
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/" + CONTENT_AUTHORITY + "/" + PATH_BOOK;

        public static final String TABLE_NAME = "Author";
        public static final String COLUMN_BOOK_ID = "BookId"; //Foreign key for BookInfo COLUMN_BOOK_ID from BookInfo table
        public static final String COLUMN_AUTHORNAME = "AuthorName";
        public static final String COLUMN_YEAR = "Year";
        public static final String COLUMN_PAGE = "Page";
        public static final String COLUMN_PUBLISHER = "Publisher";
        public static final String COLUMN_DOWNLOAD_LINK = "Downloadlink";
        public static final String COLUMN_FILE_PATHNAME = "FilePathName";

        public static Uri buildAuthorBookIdUri(long BookId)
        {
            return ContentUris.withAppendedId(BOOK_ID_CONTENT_URI, BookId);
        }

        public static String getBookIdFromUri(Uri uri)
        {
            return String.valueOf(ContentUris.parseId(uri));
        }
    }

}
