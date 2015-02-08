package com.example.sudha.itbookdownloader;

import android.app.Fragment;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ShareActionProvider;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.example.sudha.itbookdownloader.volley.MyVolley;

import static com.example.sudha.itbookdownloader.data.ITBookDownloaderContract.AuthorEntry;
import static com.example.sudha.itbookdownloader.data.ITBookDownloaderContract.BookEntry;


public class BookDetailActivity extends ActionBarActivity
{
    private final String LOG_TAG = BookDetailActivity.class.getSimpleName();
    public static String BookId;
    public static String BookIsbnFromList;



    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_detail);
        BookDetailsFragment bookDetailsFragment = new BookDetailsFragment();
        Bundle bundle;
        if (savedInstanceState == null)
        {
            Intent intent = getIntent();
            if (Intent.ACTION_VIEW.equals(intent.getAction()))
            {
                bundle = intent.getExtras();
                String BookIdLabel = this.getString(R.string.book_id_label);
                BookId = bundle.getString(BookIdLabel);
                //Log.d(LOG_TAG, "Received BookId from the Intent : " + BookId);
                bookDetailsFragment.setArguments(bundle); //passing the BookId in bundle to fragment to process
            }
            getFragmentManager().beginTransaction().add(R.id.book_detail_activity, bookDetailsFragment).commit();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_book_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        int id = item.getItemId();
        if (id == R.id.action_settings)
        {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class BookDetailsFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>   , FetchBooksForSearchQueryListener
    {
        //private static String LOG_TAG = BookDetailsFragment.class.getSimpleName();
        private static final String PREVIOUS_BOOK_ID_LABEL      = "PreviousBookId";
        //private ITBDBookDetailsAdapter mITBDBookDetailsAdapter;
        private static final String[] BOOK_DETAILS_COLUMNS = {
                BookEntry._ID,
                BookEntry.COLUMN_TITLE,
                BookEntry.COLUMN_SUBTITLE,
                BookEntry.COLUMN_DESCRIPTION,
                BookEntry.COLUMN_ISBN,
                BookEntry.COLUMN_IMAGE_LINK,
                BookEntry.COLUMN_BOOK_SEARCH_QUERY,
                AuthorEntry.COLUMN_WEBSITE_BOOK_NUMBER,
                AuthorEntry.COLUMN_AUTHOR_ISBN,
                AuthorEntry.COLUMN_AUTHORNAME,
                AuthorEntry.COLUMN_YEAR,
                AuthorEntry.COLUMN_PAGE,
                AuthorEntry.COLUMN_PUBLISHER,
                AuthorEntry.COLUMN_DOWNLOAD_LINK,
                AuthorEntry.COLUMN_FILE_FORMAT,
                AuthorEntry.COLUMN_FILE_PATHNAME
        };
        // These indices are tied to BOOK_DETAILS_COLUMNS.  If BOOK_DETAILS_COLUMNS changes, these must change.
        public static final int _ID = 0;
        public static final int COLUMN_TITLE = 1;
        public static final int COLUMN_SUBTITLE = 2;
        public static final int COLUMN_DESCRIPTION = 3;
        public static final int COLUMN_ISBN = 4;
        public static final int COLUMN_IMAGE_LINK = 5;
        public static final int COLUMN_BOOK_SEARCH_QUERY = 6;
        public static final int COLUMN_WEBSITE_BOOK_NUMBER = 7;
        public static final int COLUMN_AUTHOR_ISBN = 8;
        public static final int COLUMN_AUTHORNAME = 9;
        public static final int COLUMN_YEAR = 10;
        public static final int COLUMN_PAGE = 11;
        public static final int COLUMN_PUBLISHER = 12;
        public static final int COLUMN_DOWNLOAD_LINK = 13;
        public static final int COLUMN_FILE_FORMAT = 14;
        public static final int COLUMN_FILE_PATHNAME = 15;
        //      Book Image, Book Title, Book Subtitle , Author, ISBN, year, page, publisher, Description, Download Link
        private NetworkImageView DetailBookImageView;
        private TextView DetailBookTitleView;
        private TextView DetailBookSubTitleView;
        private TextView DetailBookAuthorView;
        private TextView DetailBookISBNView;
        private TextView DetailBookYearView;
        private TextView DetailBookPageView;
        private TextView DetailBookPublisherView;
        private TextView DetailBookDescriptionView;
        private Button DetailBookDownloadButton;

        private static final int BOOK_DETAILS_LOADER = 1;
        private ShareActionProvider BookDetailsShareActionProvider;
        private String BookDetailsShareString;
        private static final String BOOK_DETAILS_SHARE_HASHTAG = " #IT Book Downloader Application";

        /*private static final String[] BOOK_DETAILS_FROM_ADAPTER_COLUMNS = {

                                                                                 BookEntry.COLUMN_TITLE,
                                                                                 BookEntry.COLUMN_SUBTITLE,
                                                                                 BookEntry.COLUMN_DESCRIPTION,
                                                                                 BookEntry.COLUMN_ISBN,
                                                                                 AuthorEntry.COLUMN_AUTHORNAME,
                                                                                 AuthorEntry.COLUMN_YEAR,
                                                                                 AuthorEntry.COLUMN_PAGE,
                                                                                 AuthorEntry.COLUMN_PUBLISHER,
                                                                           };

        private static final String[] BOOK_DETAILS_TO_ADAPTER_COLUMNS = {

                                                                                  BookEntry.COLUMN_TITLE,
                                                                                  BookEntry.COLUMN_SUBTITLE,
                                                                                  BookEntry.COLUMN_DESCRIPTION,
                                                                                  BookEntry.COLUMN_ISBN,
                                                                                  AuthorEntry.COLUMN_AUTHORNAME,
                                                                                  AuthorEntry.COLUMN_YEAR,
                                                                                  AuthorEntry.COLUMN_PAGE,
                                                                                  AuthorEntry.COLUMN_PUBLISHER,
                                                                        };*/


        public BookDetailsFragment()
        {
            setHasOptionsMenu(true);
        }

        @Override
        public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
        {
            /*// Inflate the menu; this adds items to the action bar if it is present.
            inflater.inflate(R.menu.menu_book_detail, menu);

            // Retrieve the share menu item
            MenuItem menuItem = menu.findItem(R.id.action_share);

            // Get the provider and hold onto it to set/change the share intent.
            mShareActionProvider = (ShareActionProvider) MenuItem.getActionProvider(menuItem);

            // If onLoadFinished happens before this, we can go ahead and set the share intent now.
            if (mForecast != null) {
                mShareActionProvider.setShareIntent(createShareForecastIntent());
            }*/
        }

        @Override
        public void onCreate(Bundle savedInstanceState)
        {
            super.onCreate(savedInstanceState);
            Bundle bundleArguments = getArguments();
            if (bundleArguments != null)
            {
                BookIsbnFromList = bundleArguments.getString(getString(R.string.isbn_label));
                BookId = bundleArguments.getString(getString(R.string.book_id_label));
            }
            fetchBookIdDetails(BookIsbnFromList, BookId);
            //getLoaderManager().initLoader(BOOK_DETAILS_LOADER, null, this);
        }

        private void fetchBookIdDetails(String mBookIsbnFromList,String mBookId)
        {
            //Log.d(LOG_TAG,"BookDetailsFragment fetchBookIdDetails : ");
            FetchBooksInfoAsyncTask fetchBooksInfoAsyncTask = new FetchBooksInfoAsyncTask(getActivity());
            fetchBooksInfoAsyncTask.asyncResponseDelegate = this;
            fetchBooksInfoAsyncTask.execute(null, mBookIsbnFromList, mBookId);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
        {
            //Log.d(LOG_TAG,"BookDetailsFragment onCreateView : ");
            //mITBDBookDetailsAdapter = new ITBDBookDetailsAdapter(this.getActivity(),null,0);
            /*Bundle bundleArguments = getArguments();
            if (bundleArguments != null)
            {
                BookIsbnFromList = bundleArguments.getString(getString(R.string.isbn_label));
                BookId = bundleArguments.getString(getString(R.string.book_id_label));
            }
            fetchBookIdDetails(BookIsbnFromList, BookId);*/
            View rootView = inflater.inflate(R.layout.fragment_book_detail, container, false);

            DetailBookImageView = (NetworkImageView) rootView.findViewById(R.id.detail_book_image_view);

            DetailBookTitleView = (TextView) rootView.findViewById(R.id.detail_book_title_textview);
            DetailBookSubTitleView = (TextView) rootView.findViewById(R.id.detail_book_subtitle_textview);
            DetailBookAuthorView = (TextView) rootView.findViewById(R.id.detail_book_author_textview);
            DetailBookISBNView = (TextView) rootView.findViewById(R.id.detail_book_ISBN_textview);
            DetailBookYearView = (TextView) rootView.findViewById(R.id.detail_book_year_textview);
            DetailBookPageView = (TextView) rootView.findViewById(R.id.detail_book_page_textview);
            DetailBookPublisherView = (TextView) rootView.findViewById(R.id.detail_book_publisher_textview);
            DetailBookDescriptionView = (TextView) rootView.findViewById(R.id.detail_book_description_textview);
            DetailBookDownloadButton = (Button) rootView.findViewById(R.id.detail_book_download_button);


            return rootView;
        }

        @Override
        public void onActivityCreated(Bundle savedInstanceState)
        {
            //Log.d(LOG_TAG,"BookDetailsFragment onActivityCreated : ");
            super.onActivityCreated(savedInstanceState);
            getLoaderManager().initLoader(BOOK_DETAILS_LOADER, null, this);
        }

        @Override
        public void onResume()
        {
            //Log.d(LOG_TAG,"BookDetailsFragment onResume : ");
            super.onResume();
            Bundle bundleArguments = getArguments();
            if ((bundleArguments != null) && (bundleArguments.containsKey(getActivity().getString(R.string.book_id_label))))
            {
                BookId = getArguments().getString(getString(R.string.book_id_label));
                getLoaderManager().restartLoader(BOOK_DETAILS_LOADER, null, this);
            }
        }

        @Override
        public void onSaveInstanceState(Bundle outState)
        {
            //Log.d(LOG_TAG,"BookDetailsFragment onSaveInstanceState : ");
            //outState.putString(BOOK_ID_LABEL, BookId);
            outState.putString(PREVIOUS_BOOK_ID_LABEL, BookId);
            super.onSaveInstanceState(outState);
        }

        @Override
        public Loader<Cursor> onCreateLoader(int i, Bundle bundle)
        {
            //Log.d(LOG_TAG,"BookDetailsFragment onCreateLoader : ");
            Uri BOOK_ID_URI = BookEntry.buildJoinBookIdUri(Long.parseLong(BookId));
            return new CursorLoader(getActivity(), BOOK_ID_URI, BOOK_DETAILS_COLUMNS, null, null, null);
        }

        @Override
        public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursorData)
        {
            //Log.d(LOG_TAG,"BookDetailsFragment onLoadFinished : ");
            if (cursorData != null && cursorData.moveToFirst()) //we have a book to display
            {

                String BookImageLink = cursorData.getString(COLUMN_IMAGE_LINK);
                MyVolley.init(getActivity().getApplicationContext()); // we need application context to make it a singleton
                //MyVolley.getRequestQueue().getCache().invalidate(BookImageLink, true);
                ImageLoader mImageLoader = MyVolley.getImageLoader();
                DetailBookImageView.setImageUrl(BookImageLink, mImageLoader);
                DetailBookImageView.setContentDescription(BookImageLink);

                String BookTitle = cursorData.getString(COLUMN_TITLE);
                DetailBookTitleView.setText(BookTitle);
                String BookSubTitle = cursorData.getString(COLUMN_SUBTITLE);
                DetailBookSubTitleView.setText(BookSubTitle);
                String BookAuthor = cursorData.getString(COLUMN_AUTHORNAME);
                DetailBookAuthorView.setText(BookAuthor);
                String BookISBN = cursorData.getString(COLUMN_ISBN);
                DetailBookISBNView.setText(BookISBN);
                String BookYear = cursorData.getString(COLUMN_YEAR);
                DetailBookYearView.setText(BookYear);
                String BookPage = cursorData.getString(COLUMN_PAGE);
                DetailBookPageView.setText(BookPage);
                String BookPublisher = cursorData.getString(COLUMN_PUBLISHER);
                DetailBookPublisherView.setText(BookPublisher);
                String BookDescription = cursorData.getString(COLUMN_DESCRIPTION);
                DetailBookDescriptionView.setText(BookDescription);

                String BookIdFromCursor = cursorData.getString(_ID);
                String BookSearchQuery = cursorData.getString(COLUMN_BOOK_SEARCH_QUERY);
                String BookDownloadLink = cursorData.getString(COLUMN_DOWNLOAD_LINK);
                String BookFilePathName = cursorData.getString(COLUMN_FILE_PATHNAME);
                String WebsiteBookNumber = cursorData.getString(COLUMN_WEBSITE_BOOK_NUMBER);
                String AuthorIsbn = cursorData.getString(COLUMN_AUTHOR_ISBN);
                String FileFormat = cursorData.getString(COLUMN_FILE_FORMAT);
                //Log.d(LOG_TAG, " BookIdFromCursor : " + BookIdFromCursor + "  BookSearchQuery : " + BookSearchQuery );
                //Log.d(LOG_TAG, " BookDownloadLink : " + BookDownloadLink + "  BookFilePathName : " + BookFilePathName );
                //Log.d(LOG_TAG, " WebsiteBookNumber : " + WebsiteBookNumber + " AuthorIsbn : " + AuthorIsbn + "  FileFormat : " + FileFormat );

                BookDetailsShareString = String.format("%s - %s - %s", BookTitle, BookAuthor, BookISBN);

                if (BookDetailsShareActionProvider != null)
                {
                    BookDetailsShareActionProvider.setShareIntent(createShareBookDetailsIntent());
                }

                DetailBookDownloadButton.setOnClickListener(new BookDownloadButtonListener(getActivity(),BookTitle, BookDownloadLink, WebsiteBookNumber, FileFormat));
                DetailBookDownloadButton.setPadding(15,15,15,15);
                DetailBookDownloadButton.setVisibility(View.VISIBLE);
                //Log.d(LOG_TAG, " Download Button onClick Listener Should be visible now : ");
            }

        }

        private Intent createShareBookDetailsIntent()
        {
            //Log.d(LOG_TAG,"BookDetailsFragment createShareBookDetailsIntent : ");
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, BookDetailsShareString + BOOK_DETAILS_SHARE_HASHTAG);
            return shareIntent;
        }

        @Override
        public void onLoaderReset(Loader<Cursor> cursorLoader)
        {
            //Log.d(LOG_TAG,"BookDetailsFragment onLoaderReset : ");
            //Log.d(LOG_TAG, "onLoaderReset : " + cursorLoader.toString());
        }

        @Override
        public void onFetchBooksForSearchQuery(String Result)
        {
            //Log.d(LOG_TAG,"BookDetailsFragment onFetchBooksForSearchQuery : ");
            getLoaderManager().restartLoader(BOOK_DETAILS_LOADER, null, this);
            //Log.d(LOG_TAG, "Check if Data has changed in Details View" + Result);

        }
    }
}
