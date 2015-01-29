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
    public static final String PATH_SEARCH = "search";
    public static final String PATH_BOOK = "book";
    public static final String PATH_AUTHORS = "authors";
/*
    /books              - BOOK_COLLECTION_URI
	/books/search/*     - BOOK_COLLECTION_FOR_SEARCH_QUERY
	/books/book/#       - BOOK_ID_IN_BOOKS
	/authors            - AUTHOR_COLLECTION_URI
	/authors/book/#     - BOOK_ID_IN_AUTHORS
	/book/#             - BOOK_ID_IN_BOOKS_AND_AUTHORS
*/

    public static final class BookEntry implements BaseColumns
    {
        public static final Uri BOOKS_CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_BOOKS).build();                                    // /books
        public static final Uri BOOKS_SEARCH_CONTENT_URI = BOOKS_CONTENT_URI.buildUpon().appendPath(PATH_SEARCH).build();                           // /books/search/*
        public static final Uri BOOKS_BOOK_ID_CONTENT_URI = BOOKS_CONTENT_URI.buildUpon().appendPath(PATH_BOOK).build();                            // /books/book/#
        public static final Uri JOIN_BOOK_ID_CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_BOOK).build();                              // /book/#

        public static final String CONTENT_BOOKS_DIR_TYPE = "vnd.android.cursor.dir/" + CONTENT_AUTHORITY + "." + BookEntry.TABLE_NAME;
        public static final String CONTENT_BOOKS_ITEM_TYPE = "vnd.android.cursor.item/" + CONTENT_AUTHORITY + "." + BookEntry.TABLE_NAME;

        public static final String TABLE_NAME = "Books";
        public static final String COLUMN_BOOK_ID = "BookId";
        public static final String COLUMN_TITLE = "BookTitle";
        public static final String COLUMN_SUBTITLE = "BookSubtitle";
        public static final String COLUMN_DESCRIPTION = "BookDescription";
        public static final String COLUMN_ISBN = "ISBN";
        public static final String COLUMN_IMAGE_LINK = "Imagelink";
        public static final String COLUMN_BOOK_SEARCH_QUERY = "BookSearchQuery";

        public static Uri buildBookCollectionUri()                                                  // /books
        {
            return BookEntry.BOOKS_CONTENT_URI;                                                     //content://com.example.sudha.itbookdownloader/books
        }

        public static Uri buildBookSearchUriForSearchQuery(String SearchQuery)                      // /books/search/*
        {
            return BookEntry.BOOKS_SEARCH_CONTENT_URI.buildUpon().appendPath(SearchQuery).build();  //content://com.example.sudha.itbookdownloader/books/search/{android}
        }

        public static Uri buildBooksIdUri(long BookId)                                              // /books/book/#
        {
            return ContentUris.withAppendedId(BOOKS_BOOK_ID_CONTENT_URI, BookId);                   //content://com.example.sudha.itbookdownloader/books/book/{533598665}
        }

        public static Uri buildJoinBookIdUri(long BookId)                                               // /book/#
        {
            return ContentUris.withAppendedId(JOIN_BOOK_ID_CONTENT_URI, BookId);                    //content://com.example.sudha.itbookdownloader/book/{533598665}
        }

        //This method returns BookId in String format to accomodate Selection Args requirement of String[]
        public static String getBookIdFromUri(Uri uri)                                              //content://com.example.sudha.itbookdownloader/books/book/{533598665}
        {
            return String.valueOf(ContentUris.parseId(uri));                                        // {533598665}
        }

        public static String getBookSearchQueryFromUri(Uri uri)                                     //content://com.example.sudha.itbookdownloader/books/search/{android}
        {
            return uri.getLastPathSegment();                                                        // {android}
        }
    }

    public static final class AuthorEntry implements BaseColumns
    {
        public static final Uri AUTHORS_CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_AUTHORS).build();                //  /authors - AUTHOR_COLLECTION_URI
        public static final Uri AUTHORS_BOOK_ID_CONTENT_URI = AUTHORS_CONTENT_URI.buildUpon().appendPath(PATH_BOOK).build();        //  /authors/book/# - BOOK_ID_IN_AUTHORS

        public static final String CONTENT_AUTHORS_DIR_TYPE = "vnd.android.cursor.dir/" + CONTENT_AUTHORITY + "." + AuthorEntry.TABLE_NAME;
        public static final String CONTENT_AUTHORS_ITEM_TYPE = "vnd.android.cursor.item/" + CONTENT_AUTHORITY + "." + AuthorEntry.TABLE_NAME;

        public static final String TABLE_NAME = "Authors";
        public static final String COLUMN_BOOK_ID = "BookId"; //Foreign key for Books.COLUMN_BOOK_ID
        public static final String COLUMN_AUTHORNAME = "AuthorName";
        public static final String COLUMN_YEAR = "Year";
        public static final String COLUMN_PAGE = "Page";
        public static final String COLUMN_PUBLISHER = "Publisher";
        public static final String COLUMN_DOWNLOAD_LINK = "Downloadlink";
        public static final String COLUMN_FILE_PATHNAME = "FilePathName";

        public static Uri buildAuthorsCollectionUri()
        {
            return AuthorEntry.AUTHORS_CONTENT_URI;
        }
        public static Uri buildAuthorsBookIdUri(long BookId)                                            // {533598665}
        {
            return ContentUris.withAppendedId(AUTHORS_BOOK_ID_CONTENT_URI, BookId);                     //content://com.example.sudha.itbookdownloader/authors/book/{533598665}
        }

        //This method returns BookId in String format to accomodate Selection Args requirement of String[]
        public static String getAuthorsBookIdFromUri(Uri uri)                                           //content://com.example.sudha.itbookdownloader/authors/book/{533598665}
        {
            return String.valueOf(ContentUris.parseId(uri));                                            // {533598665}
        }


    }

}


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
	/books/book/#       - BOOK_ID_IN_BOOKS
	/authors            - AUTHOR_COLLECTION_URI
	/authors/book/#     - BOOK_ID_IN_AUTHORS
	/book/#             - BOOK_ID_IN_BOOKS_AND_AUTHORS

	MAIN OPERATIONS IN CONTENT PROVIDER :

    1.Delete all records in Books and Authors table to start clean
    /books - delete
    /authors - delete

    2.Bulk insert all the books fetched from web API
    /books/search/* - insert

    3.Bulk query to fetch all the  Books
    /books/search/* - query

    4. Query If that particular book id is in Books table
    /books/book/# - query

    5. Insert that particular book id in the Authors table
    /authors/book/# - insert

    6. Join Query to fetch complete book information
    /book/# - query

    For example, if a provider's authority is com.example.app.provider, and it exposes a table named table1, the MIME type for multiple rows in table1 is:

    vnd.android.cursor.dir/vnd.com.example.provider.table1

    For a single row of table1, the MIME type is:

    vnd.android.cursor.item/vnd.com.example.provider.table1
*/