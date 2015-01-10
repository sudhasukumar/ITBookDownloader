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
    public static final String CONTENT_AUTHORITY = "com.example.sudha.itbookdownloader.data";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static final String PATH_BOOKINFO = "bookinfo";
    public static final String PATH_AUTHOR = "author";

    public static final class BookEntry implements BaseColumns
    {
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_BOOKINFO).build();
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/" + CONTENT_AUTHORITY + "/" + PATH_BOOKINFO;
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/" + CONTENT_AUTHORITY + "/" + PATH_BOOKINFO;

        public static final String TABLE_NAME = "BookInfo";
        public static final String COLUMN_BOOK_ID = "BookId";
        public static final String COLUMN_TITLE = "BookTitle";
        public static final String COLUMN_SUBTITLE = "BookSubtitle";
        public static final String COLUMN_DESCRIPTION = "BookDescription";
        public static final String COLUMN_ISBN = "ISBN";
        public static final String COLUMN_IMAGE_LINK = "Imagelink";
        public static final String COLUMN_BOOK_SEARCH_QUERY = "BookSearchQuery";


        public static Uri buildBookInfoInsertUri(long id)
        {
            return ContentUris.withAppendedId(CONTENT_URI, id);
            //return null;
        }
    }

    public static final class AuthorEntry implements BaseColumns
    {
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_AUTHOR).build();
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/" + CONTENT_AUTHORITY + "/" + PATH_AUTHOR;
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/" + CONTENT_AUTHORITY + "/" + PATH_AUTHOR;

        public static final String TABLE_NAME = "Author";
        public static final String COLUMN_BOOK_ID = "BookId"; //Foreign key for BookInfo COLUMN_BOOK_ID from BookInfo table
        public static final String COLUMN_AUTHORNAME = "AuthorName";
        public static final String COLUMN_YEAR = "Year";
        public static final String COLUMN_PAGE = "Page";
        public static final String COLUMN_PUBLISHER = "Publisher";
        public static final String COLUMN_DOWNLOAD_LINK = "Downloadlink";
        public static final String COLUMN_FILE_PATHNAME = "FilePathName";


        public static Uri buildAuthorInsertUri(long id)
        {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }

}
