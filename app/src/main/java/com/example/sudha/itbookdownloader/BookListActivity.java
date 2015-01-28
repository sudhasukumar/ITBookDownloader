package com.example.sudha.itbookdownloader;


import android.app.Activity;
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
import android.widget.AdapterView;
import android.widget.ListView;

import static com.example.sudha.itbookdownloader.BookDetailActivity.BookDetailsFragment;
import static com.example.sudha.itbookdownloader.data.ITBookDownloaderContract.BookEntry;


public class BookListActivity extends ActionBarActivity
{
    private final String LOG_TAG = BookListActivity.class.getSimpleName();
    private static String DefaultSearchQuery = null;
    private static String SearchQuery = null;
    private static final String SEARCH_QUERY_LABEL = "SearchQuery";
    private static final String BOOK_ID_LABEL = "BookId";
    private static String BookId = null;
    private boolean mTwoPane;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_list);
        BookListFragment bookListFragment = new BookListFragment();
        Bundle bundle;
        if (savedInstanceState == null)
        {
            Intent intent = getIntent();
            if (Intent.ACTION_SEARCH.equals(intent.getAction()))
            {
                bundle = intent.getExtras();
                String SearchQueryLabel = this.getString(R.string.search_query_label);
                SearchQuery = bundle.getString(SearchQueryLabel);
                DefaultSearchQuery = this.getString(R.string.search_query_string_default);
                Log.d(LOG_TAG, "Received Search Query from the Intent : " + SearchQuery);
                bookListFragment.setArguments(bundle); //passing the search query in bundle to fragment to process
            }

            getFragmentManager().beginTransaction().add(R.id.book_list_activity, bookListFragment).commit();
            //ITBDSyncAdapter.initializeSyncAdapter(this);
            //ITBDSyncAdapter.syncImmediately(this,SearchQuery,null);
        }

    }

    public void onItemSelected(String bookId)
    {
        if (mTwoPane)
        {
            // In two-pane mode, show the detail view in this activity by adding or replacing the detail fragment using a fragment transaction.
            Bundle args = new Bundle();
            args.putString(BookDetailActivity.BookId, bookId);

            BookDetailsFragment bookDetailsFragment = new BookDetailsFragment();
            bookDetailsFragment.setArguments(args);

            getFragmentManager().beginTransaction().replace(R.id.book_detail_activity, bookDetailsFragment).commit();
        }
        else
        {
            Intent ViewBookDetailIntent = new Intent(this, BookDetailActivity.class).putExtra(BookDetailActivity.BookId, BookId);
            ViewBookDetailIntent.setAction(Intent.ACTION_VIEW);
            startActivity(ViewBookDetailIntent);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_book_list, menu);
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

    public static class BookListFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>, FetchBooksForSearchQueryListener
    {
        public static final String LOG_TAG = BookListFragment.class.getSimpleName();
        private ITBDBookSearchAdapter mITBDBookSearchAdapter;
        private ListView BookListView;
        private int USER_LIST_VIEW_POSITION = ListView.INVALID_POSITION;
        private boolean isHighlightedBookLayout;
        private static final String USER_LIST_VIEW_POSITION_LABEL = "user_selected_position";
        private static final int BOOK_SEARCH_LOADER = 0;
        private static final String[] BOOK_SEARCH_COLUMNS = {   //BookEntry.TABLE_NAME + "." + BookEntry.COLUMN_BOOK_ID,
                                                                BookEntry._ID,
                                                                BookEntry.COLUMN_TITLE,
                                                                BookEntry.COLUMN_SUBTITLE,
                                                                BookEntry.COLUMN_ISBN,
                                                                BookEntry.COLUMN_IMAGE_LINK,
                                                                BookEntry.COLUMN_DESCRIPTION,
                                                                BookEntry.COLUMN_BOOK_SEARCH_QUERY
        };
        // These indices are tied to BOOK_SEARCH_COLUMNS.  If BOOK_SEARCH_COLUMNS changes, these must change.
        public static final int COL_ID = 0;
        public static final int COL_TITLE = 1;
        public static final int COL_SUBTITLE = 2;
        public static final int COL_ISBN = 3;
        public static final int COL_IMAGE_LINK = 4;
        //public static final int COL_DESCRIPTION = 5;
        //public static final int COL_BOOK_SEARCH_QUERY = 6;


        public BookListFragment()
        {
            setHasOptionsMenu(true);
        }

        @Override
        public void onActivityCreated(Bundle savedInstanceState)
        {
            // Initialise Loader here...Loader's lifecycle is bound to Activity Lifecycle not Fragment
            getLoaderManager().initLoader(BOOK_SEARCH_LOADER, null, this);
            super.onActivityCreated(savedInstanceState);
        }

        @Override
        public void onResume()
        {
            super.onResume();

            if ((SearchQuery != null) && (SearchQuery.equals(DefaultSearchQuery))) //&& !mLocation.equals(Utility.getPreferredLocation(getActivity())))
            {
                SearchQuery = getArguments().getString(getString(R.string.search_query_label));
                getLoaderManager().restartLoader(BOOK_SEARCH_LOADER, null, this);
            }
        }

        @Override
        public void onSaveInstanceState(Bundle outState)
        {
            // When tablets rotate, the currently selected list item needs to be saved. When no item is selected, mPosition will be set to Listview.INVALID_POSITION, so check for that before storing.
            if (USER_LIST_VIEW_POSITION != ListView.INVALID_POSITION)
            {
                outState.putInt(USER_LIST_VIEW_POSITION_LABEL, USER_LIST_VIEW_POSITION);
            }
            outState.putString(SEARCH_QUERY_LABEL, SearchQuery);
            outState.putString(BOOK_ID_LABEL, BookId);
            super.onSaveInstanceState(outState);
        }

        @Override
        public void onCreate(Bundle savedInstanceState)
        {
            super.onCreate(savedInstanceState);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
        {
            mITBDBookSearchAdapter = new ITBDBookSearchAdapter(getActivity(), null, 0);
            View rootView = inflater.inflate(R.layout.fragment_book_list, container, false);
            BookListView = (ListView) rootView.findViewById(R.id.listview_book_search);
            updateSearchBookList(SearchQuery, BookId);
            BookListView.setAdapter(mITBDBookSearchAdapter);
            BookListView.setClickable(true);
            BookListView.setOnItemClickListener(
                    new AdapterView.OnItemClickListener()
                    {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id)
                        {
                            Cursor cursor = mITBDBookSearchAdapter.getCursor();
                            BookId = cursor.getString(COL_ID);
                            if (cursor.moveToPosition(position))
                            {
                                Activity myBookListActivity = getActivity();
                                if (myBookListActivity instanceof BookListActivity)
                                    ((BookListActivity) getActivity()).onItemSelected(BookId);

                            }
                            USER_LIST_VIEW_POSITION = position;
                        }
                    });

            if (savedInstanceState != null && savedInstanceState.containsKey(USER_LIST_VIEW_POSITION_LABEL))
            {
                USER_LIST_VIEW_POSITION = savedInstanceState.getInt(USER_LIST_VIEW_POSITION_LABEL);
            }

            mITBDBookSearchAdapter.setHighlightedBookLayout(isHighlightedBookLayout);

            return rootView;
        }

        private void updateSearchBookList(String searchQuery, String bookId)
        {
            FetchBooksForSearchQueryTask fetchBooksForSearchQueryTask = new FetchBooksForSearchQueryTask(getActivity());
            fetchBooksForSearchQueryTask.asyncResponseDelegate = this;
            fetchBooksForSearchQueryTask.execute(searchQuery, bookId);
        }

        @Override
        public void onFetchBooksForSearchQuery(String result)
        {
            Log.d(LOG_TAG, "Check if Data has changed in List view " + result);
        }

        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args)
        {
            // Sort order:  Ascending, by Book Title.
            final String SORT_TITLE_ASC = BookEntry.COLUMN_TITLE + " ASC";
            Uri BOOK_SEARCH_URI = BookEntry.buildBookSearchUriForSearchQuery(SearchQuery);
            // Now create and return a CursorLoader that will take care of creating a Cursor for the data being displayed.
            return new CursorLoader(getActivity(), BOOK_SEARCH_URI, BOOK_SEARCH_COLUMNS, null, null, SORT_TITLE_ASC);
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data)
        {
            mITBDBookSearchAdapter.swapCursor(data);
            if (USER_LIST_VIEW_POSITION != ListView.INVALID_POSITION)
                BookListView.smoothScrollToPosition(USER_LIST_VIEW_POSITION);
        }

        @Override
        public void onLoaderReset(Loader loader)
        {
            mITBDBookSearchAdapter.swapCursor(null);
        }

        public void setUseHighlightedBookLayout(boolean isHighlightedBookLayoutParam)
        {
            isHighlightedBookLayout = isHighlightedBookLayoutParam;
            if (mITBDBookSearchAdapter != null)
            {
                mITBDBookSearchAdapter.setHighlightedBookLayout(isHighlightedBookLayout);
            }
        }
    }
}


/*Intent showBookDetailIntent = new Intent(view.getContext(), BookDetailActivity.class);
showBookDetailIntent.setType(Intent.ACTION_VIEW);
showBookDetailIntent.setType("text/plain");
showBookDetailIntent.putExtra(getActivity().getString(R.string.book_id_label), BookId);
Log.d(LOG_TAG, "showBookDetailIntent is ready");
view.getContext().startActivity(showBookDetailIntent);*/