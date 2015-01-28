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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ShareActionProvider;
import android.widget.TextView;

import static com.example.sudha.itbookdownloader.data.ITBookDownloaderContract.AuthorEntry;
import static com.example.sudha.itbookdownloader.data.ITBookDownloaderContract.BookEntry;


public class BookDetailActivity extends ActionBarActivity
{
    private final String LOG_TAG = BookDetailActivity.class.getSimpleName();

    private static final String BOOK_ID_LABEL = "BookId";
    public static String BookId;
    //private static String DefaultBookId = "0";


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
                Log.d(LOG_TAG, "Received BookId from the Intent : " + BookId);
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
    public static class BookDetailsFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>, FetchBooksForSearchQueryListener
    {
        private static String LOG_TAG = BookDetailsFragment.class.getSimpleName();
        private static final String[] BOOK_DETAILS_COLUMNS = {
                BookEntry._ID,
                BookEntry.COLUMN_TITLE,
                BookEntry.COLUMN_SUBTITLE,
                BookEntry.COLUMN_DESCRIPTION,
                BookEntry.COLUMN_ISBN,
                BookEntry.COLUMN_IMAGE_LINK,
                BookEntry.COLUMN_BOOK_SEARCH_QUERY,
                AuthorEntry.COLUMN_AUTHORNAME,
                AuthorEntry.COLUMN_YEAR,
                AuthorEntry.COLUMN_PAGE,
                AuthorEntry.COLUMN_PUBLISHER,
                AuthorEntry.COLUMN_DOWNLOAD_LINK,
                AuthorEntry.COLUMN_FILE_PATHNAME
        };
        // These indices are tied to BOOK_SEARCH_COLUMNS.  If BOOK_SEARCH_COLUMNS changes, these must change.
        public static final int _ID = 0;
        public static final int COLUMN_TITLE = 1;
        public static final int COLUMN_SUBTITLE = 2;
        public static final int COLUMN_DESCRIPTION = 3;
        public static final int COLUMN_ISBN = 4;
        public static final int COLUMN_IMAGE_LINK = 5;
        public static final int COLUMN_BOOK_SEARCH_QUERY = 6;
        public static final int COLUMN_AUTHORNAME = 7;
        public static final int COLUMN_YEAR = 8;
        public static final int COLUMN_PAGE = 9;
        public static final int COLUMN_PUBLISHER = 10;
        public static final int COLUMN_DOWNLOAD_LINK = 11;
        public static final int COLUMN_FILE_PATHNAME = 12;
        //      Book Image, Book Title, Book Subtitle , Author, ISBN, year, page, publisher, Description, Download Link
        private ImageView DetailBookImageView;
        private TextView DetailBookTitleView;
        private TextView DetailBookSubTitleView;
        private TextView DetailBookAuthorView;
        private TextView DetailBookISBNView;
        private TextView DetailBookYearView;
        private TextView DetailBookPageView;
        private TextView DetailBookPublisherView;
        private TextView DetailBookDescriptionView;

        private static final int BOOK_DETAILS_LOADER = 1;
        private ShareActionProvider BookDetailsShareActionProvider;
        private String BookDetailsShareString;
        private static final String BOOK_DETAILS_SHARE_HASHTAG = " #IT Book Downloader Application";



        public BookDetailsFragment()
        {
            setHasOptionsMenu(true);
        }

        /*@Override
        public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
        {
            // Inflate the menu; this adds items to the action bar if it is present.
            inflater.inflate(R.menu.detailfragment, menu);

            // Retrieve the share menu item
            MenuItem menuItem = menu.findItem(R.id.action_share);

            // Get the provider and hold onto it to set/change the share intent.
            mShareActionProvider = (ShareActionProvider) MenuItem.getActionProvider(menuItem);

            // If onLoadFinished happens before this, we can go ahead and set the share intent now.
            if (mForecast != null) {
                mShareActionProvider.setShareIntent(createShareForecastIntent());
            }
        }*/

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
        {
            Bundle bundleArguments = getArguments();
            if (bundleArguments != null)
            {
                BookId = bundleArguments.getString(getString(R.string.book_id_label));
            }
            updateBookIdDetails(BookId);
            View rootView = inflater.inflate(R.layout.fragment_book_detail, container, false);

            DetailBookImageView = (ImageView) rootView.findViewById(R.id.detail_book_image_view);
            DetailBookTitleView = (TextView) rootView.findViewById(R.id.detail_book_title_textview);
            DetailBookSubTitleView = (TextView) rootView.findViewById(R.id.detail_book_subtitle_textview);
            DetailBookAuthorView = (TextView) rootView.findViewById(R.id.detail_book_author_textview);
            DetailBookISBNView = (TextView) rootView.findViewById(R.id.detail_book_ISBN_textview);
            DetailBookYearView = (TextView) rootView.findViewById(R.id.detail_book_year_textview);
            DetailBookPageView = (TextView) rootView.findViewById(R.id.detail_book_page_textview);
            DetailBookPublisherView = (TextView) rootView.findViewById(R.id.detail_book_publisher_textview);
            DetailBookDescriptionView = (TextView) rootView.findViewById(R.id.detail_book_description_textview);

            return rootView;
        }

        private void updateBookIdDetails(String bookId)
        {
            FetchBooksForSearchQueryTask fetchBooksForSearchQueryTask = new FetchBooksForSearchQueryTask(getActivity());
            fetchBooksForSearchQueryTask.asyncResponseDelegate = this;
            fetchBooksForSearchQueryTask.execute(null, bookId);
        }

        @Override
        public void onActivityCreated(Bundle savedInstanceState)
        {
            // Initialise Loader here...Loader's lifecycle is bound to Activity Lifecycle not Fragment
            getLoaderManager().initLoader(BOOK_DETAILS_LOADER, null, this);
            super.onActivityCreated(savedInstanceState);
        }

        @Override
        public void onResume()
        {
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
            outState.putString(BOOK_ID_LABEL, BookId);
            super.onSaveInstanceState(outState);
        }

        @Override
        public Loader<Cursor> onCreateLoader(int i, Bundle bundle)
        {
            Uri BOOK_ID_URI = BookEntry.buildBookIdUri(Long.parseLong(BookId));
            return new CursorLoader(getActivity(), BOOK_ID_URI, BOOK_DETAILS_COLUMNS, null, null, null);
        }

        @Override
        public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursorData)
        {
            if (cursorData != null && cursorData.moveToFirst()) //we have a book to display
            {
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
                String BookImageLink = cursorData.getString(COLUMN_IMAGE_LINK);
                DetailBookImageView.setContentDescription(BookImageLink);
                String BookIdFromCursor = cursorData.getString(_ID);
                String BookSearchQuery = cursorData.getString(COLUMN_BOOK_SEARCH_QUERY);
                String BookDownloadLink = cursorData.getString(COLUMN_DOWNLOAD_LINK);
                String BookFilePathName = cursorData.getString(COLUMN_FILE_PATHNAME);
                Log.d(LOG_TAG, "BookIdFromCursor : " + BookIdFromCursor + "BookSearchQuery : " + BookSearchQuery +
                        "BookDownloadLink : " + BookDownloadLink + "BookFilePathName : " + BookFilePathName);

                BookDetailsShareString = String.format("%s - %s - %s", BookTitle, BookAuthor, BookISBN);

                if (BookDetailsShareActionProvider != null)
                {
                    BookDetailsShareActionProvider.setShareIntent(createShareBookDetailsIntent());
                }
            }

        }

        private Intent createShareBookDetailsIntent()
        {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, BookDetailsShareString + BOOK_DETAILS_SHARE_HASHTAG);
            return shareIntent;
        }

        @Override
        public void onLoaderReset(Loader<Cursor> cursorLoader)
        {
            Log.d(LOG_TAG, "onLoaderReset : " + cursorLoader.toString());
        }

        @Override
        public void onFetchBooksForSearchQuery(String Result)
        {
            Log.d(LOG_TAG, "Check if Data has changed in Details View" + Result);
        }
    }
}
