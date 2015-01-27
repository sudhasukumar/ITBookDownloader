package com.example.sudha.itbookdownloader;


import android.app.Fragment;
import android.app.FragmentTransaction;
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
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.example.sudha.itbookdownloader.sync.ITBDSyncAdapter;

import java.util.HashMap;
import java.util.List;

import static com.example.sudha.itbookdownloader.data.ITBookDownloaderContract.BookEntry;


public class BookListActivity extends ActionBarActivity
{
    private final String LOG_TAG = BookListActivity.class.getSimpleName();
    private static String searchQuery;

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
                String SearchQueryLabel = getString(R.string.search_query_label);
                searchQuery = bundle.getString(SearchQueryLabel);
                Log.d(LOG_TAG, "Received Search Query from the Intent : " + searchQuery);
                bookListFragment.setArguments(bundle); //passing the search query in bundle to fragment to process
            }

            FragmentTransaction bookListFragmentTransaction = getFragmentManager().beginTransaction();
            bookListFragmentTransaction.add(R.id.book_list_activity, bookListFragment);
            bookListFragmentTransaction.commit();
            ITBDSyncAdapter.initializeSyncAdapter(this);
            ITBDSyncAdapter.syncImmediately(this,searchQuery,null);
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
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public static class BookListFragment extends Fragment implements FetchBooksForSearchQueryListener, LoaderManager.LoaderCallbacks<Cursor>
    {
        public static final String LOG_TAG = BookListFragment.class.getSimpleName();
        ArrayAdapter<String> stringArrayAdapter;
        private ITBDBookSearchAdapter itbdBookSearchAdapter;
        private ListView book_listview;
        private int FavorListViewPosition = ListView.INVALID_POSITION;
        private boolean isHighlightedBookLayout;
        private static final String SELECTED_KEY = "selected_position";
        private static final int BOOK_SEARCH_LOADER = 0;
        private static final String DEFAULT_SEARCH_QUERY = "Android";
        private static String USER_BOOK_SEARCH_QUERY = DEFAULT_SEARCH_QUERY;
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
            if ((USER_BOOK_SEARCH_QUERY != null) && (USER_BOOK_SEARCH_QUERY.equals(DEFAULT_SEARCH_QUERY))) //&& !mLocation.equals(Utility.getPreferredLocation(getActivity())))
            {
                USER_BOOK_SEARCH_QUERY = getArguments().getString(getString(R.string.search_query_label));
                getLoaderManager().restartLoader(BOOK_SEARCH_LOADER, null, this);
            }
        }

        @Override
        public void onSaveInstanceState(Bundle outState)
        {
            // When tablets rotate, the currently selected list item needs to be saved. When no item is selected, mPosition will be set to Listview.INVALID_POSITION, so check for that before storing.
            if (FavorListViewPosition != ListView.INVALID_POSITION)
            {
                outState.putInt(SELECTED_KEY, FavorListViewPosition);
            }
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
            /*String bookListInfo = getString(R.string.book_list_info);
            List<String> bookTitleArrayList= new ArrayList<>();
            bookTitleArrayList.add(0, bookListInfo);
            stringArrayAdapter = new ArrayAdapter<>(getActivity(), R.layout.list_item_book_search_highlighted, R.id.list_item_book_title, bookTitleArrayList);
            USER_BOOK_SEARCH_QUERY = getArguments().getString("searchquery");        //User Search Query
            updateSearchBookList(USER_BOOK_SEARCH_QUERY, stringArrayAdapter);*/

            itbdBookSearchAdapter = new ITBDBookSearchAdapter(getActivity(), null, 0);
            View rootView = inflater.inflate(R.layout.fragment_book_list, container, false);
            book_listview = (ListView) rootView.findViewById(R.id.listview_book_search);
            book_listview.setAdapter(itbdBookSearchAdapter);
            book_listview.setClickable(true);
            book_listview.setOnItemClickListener(
                                                new AdapterView.OnItemClickListener()
                                                {
                                                    @Override
                                                    public void onItemClick(AdapterView<?> parent, View view, int position, long id)
                                                    {
                                                        Cursor cursor = itbdBookSearchAdapter.getCursor();
                                                        String BookId = cursor.getString(COL_ID);
                                                        if ((cursor != null) && cursor.moveToPosition(position))
                                                        {
                                                            Intent showBookDetailIntent = new Intent(view.getContext(), BookDetailActivity.class);
                                                            showBookDetailIntent.setType(Intent.ACTION_VIEW);
                                                            showBookDetailIntent.setType("text/plain");
                                                            showBookDetailIntent.putExtra(BookDetailActivity.BOOK_ID, BookId);
                                                            Log.d(LOG_TAG, "showBookDetailIntent is ready");
                                                            view.getContext().startActivity(showBookDetailIntent);
                                                        }
                                                        FavorListViewPosition = position;
                                                    }
                                                });

            if (savedInstanceState != null && savedInstanceState.containsKey(SELECTED_KEY))
            {
                FavorListViewPosition = savedInstanceState.getInt(SELECTED_KEY);
            }

            itbdBookSearchAdapter.setHighlightedBookLayout(isHighlightedBookLayout);

            return rootView;
        }

        private void updateSearchBookList(String searchQuery, ArrayAdapter<String> stringArrayAdapter)
        {
            FetchBooksForSearchQueryTask fetchBooksForSearchQueryTask = new FetchBooksForSearchQueryTask(getActivity(), stringArrayAdapter);
            fetchBooksForSearchQueryTask.asyncResponseDelegate = this;
            fetchBooksForSearchQueryTask.execute(searchQuery);
        }

        @Override
        public void onFetchBooksForSearchQuery(List<HashMap<String, String>> ArrayListOfStrings)
        {
            stringArrayAdapter.clear();
            //stringArrayAdapter.addAll(ArrayListOfStrings);
            Log.d(LOG_TAG, "Check if Data has changed in List view");
        }

        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args)
        {
            // Sort order:  Ascending, by Book Title.
            final String SORT_TITLE_ASC = BookEntry.COLUMN_TITLE + " ASC";
            Uri BOOK_SEARCH_URI = BookEntry.buildBookSearchUriForSearchQuery(USER_BOOK_SEARCH_QUERY);
            // Now create and return a CursorLoader that will take care of creating a Cursor for the data being displayed.
            return new CursorLoader(getActivity(), BOOK_SEARCH_URI, BOOK_SEARCH_COLUMNS, null, null, SORT_TITLE_ASC);
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data)
        {
            itbdBookSearchAdapter.swapCursor(data);
            if (FavorListViewPosition != ListView.INVALID_POSITION)
                book_listview.smoothScrollToPosition(FavorListViewPosition);
        }

        @Override
        public void onLoaderReset(Loader loader)
        {
            itbdBookSearchAdapter.swapCursor(null);
        }

        public void setUseHighlightedBookLayout(boolean isHighlightedBookLayoutParam)
        {
            isHighlightedBookLayout = isHighlightedBookLayoutParam;
            if (itbdBookSearchAdapter != null)
            {
                itbdBookSearchAdapter.setHighlightedBookLayout(isHighlightedBookLayout);
            }
        }
    }
}
