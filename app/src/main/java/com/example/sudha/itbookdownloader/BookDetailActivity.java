package com.example.sudha.itbookdownloader;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import static com.example.sudha.itbookdownloader.data.ITBookDownloaderContract.BookEntry;
import static com.example.sudha.itbookdownloader.data.ITBookDownloaderContract.AuthorEntry;


public class BookDetailActivity extends ActionBarActivity
{
    private final String LOG_TAG = BookDetailActivity.class.getSimpleName();
    public static String BookId;
    private static final String BOOK_ID_LABEL = "BookId";


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
            if (Intent.ACTION_SEARCH.equals(intent.getAction()))
            {
                bundle = intent.getExtras();
                String BookIdLabel = this.getString(R.string.book_id_label);
                BookId = bundle.getString(BookIdLabel);
                Log.d(LOG_TAG, "Received BookId from the Intent : " + BookId);
                bookDetailsFragment.setArguments(bundle); //passing the BookId in bundle to fragment to process
            }
            getSupportFragmentManager().beginTransaction().add(R.id.book_detail_activity, bookDetailsFragment).commit();
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
    public static class BookDetailsFragment extends Fragment
    {
        private static final String[] BOOK_DETAILS_COLUMNS = {   //BookEntry.TABLE_NAME + "." + BookEntry.COLUMN_BOOK_ID,
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
        private TextView DetailBookPage;
        private TextView DetailBookPublisher;
        private TextView DetailBookDescriptionView;

        public BookDetailsFragment()
        {
            setHasOptionsMenu(true);
        }

        @Override
        public void onSaveInstanceState(Bundle outState)
        {
            outState.putString(BOOK_ID_LABEL, BookId);
            super.onSaveInstanceState(outState);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
        {
            View rootView = inflater.inflate(R.layout.fragment_book_detail, container, false);
            //TextView bookDetailsTextView = (TextView) rootView.findViewById(R.id.book_details_fragment_text_view);
            //bookDetailsTextView.setText(R.string.book_details_text);
            return rootView;
        }
    }
}
