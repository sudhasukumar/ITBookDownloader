package com.example.sudha.itbookdownloader;


import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
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
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;

import static com.example.sudha.itbookdownloader.BookDetailActivity.BookDetailsFragment;
import static com.example.sudha.itbookdownloader.data.ITBookDownloaderContract.BookEntry;


public class BookListActivity extends ActionBarActivity
{
    private final        String LOG_TAG                     = BookListActivity.class.getSimpleName();
    private static       String SearchQuery                 = null;
    private static       String ISBN                        = null;
    private static       String BookId                      = null;

    private boolean mTwoPane;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        Log.d(LOG_TAG,"BookListActivity onCreate : ");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_list);
        FragmentManager fragmentManager = getFragmentManager();
        BookListFragment bookListFragment = new BookListFragment();
        if (findViewById(R.id.book_detail_container) != null)
        {
            mTwoPane = true;
            if (savedInstanceState == null)
            {
                fragmentManager.beginTransaction().replace(R.id.book_list_container, bookListFragment).commit();
                fragmentManager.beginTransaction().replace(R.id.book_detail_container, new BookDetailsFragment()).commit();
            }
        } else
        {
            mTwoPane = false;
            fragmentManager.beginTransaction().add(R.id.book_list_activity , bookListFragment , "BookListFragment").commit();
        }



        if (bookListFragment != null)
        {
            Log.d(LOG_TAG, " Book List Fragment is not null");
        }

        if (bookListFragment.isAdded())
        {
            Log.d(LOG_TAG, " Book List Fragment is added already");
        }


        Bundle bundle;
        if ( savedInstanceState == null )
        {
            Intent intent = getIntent();
            if ( Intent.ACTION_SEARCH.equals(intent.getAction()) )
            {
                bundle = intent.getExtras();
                String SearchQueryLabel = this.getString(R.string.search_query_label);
                SearchQuery = bundle.getString(SearchQueryLabel);
                //DefaultSearchQuery = this.getString(R.string.search_query_string_default);
                Log.d(LOG_TAG, "Received Search Query from the Intent : " + SearchQuery);
                bookListFragment.setArguments(bundle); //passing the search query in bundle to fragment to process
            }
            //ITBDSyncAdapter.initializeSyncAdapter(this);
            //ITBDSyncAdapter.syncImmediately(this,SearchQuery,null);
        }
    }

    public void onItemSelected(String mBookId)
    {
        Log.d(LOG_TAG,"BookListActivity onItemSelected : ");
        if (mTwoPane)
        {
            // In two-pane mode, show the detail view in this activity by adding or replacing the detail fragment using a fragment transaction.
            Bundle args = new Bundle();
            args.putString(BookDetailActivity.BookId, mBookId);

            BookDetailsFragment bookDetailsFragment = new BookDetailsFragment();
            bookDetailsFragment.setArguments(args);

            getFragmentManager().beginTransaction().replace(R.id.book_detail_container, bookDetailsFragment).commit();
        }
        else
        {
            Intent showBookDetailIntent = new Intent(this, BookDetailActivity.class);
            showBookDetailIntent.setAction(Intent.ACTION_VIEW);
            showBookDetailIntent.setType("text/plain");
            String BookIdLabel = this.getString(R.string.book_id_label);
            showBookDetailIntent.putExtra(BookIdLabel, BookId);
            String IsbnLabel = this.getString(R.string.isbn_label);
            showBookDetailIntent.putExtra(IsbnLabel, ISBN);
            Log.d(LOG_TAG, "showBookDetailIntent is ready");
            startActivity(showBookDetailIntent);
         }
    }

    /*@Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        Log.d(LOG_TAG,"BookListActivity onCreateOptionsMenu : ");
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_book_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        Log.d(LOG_TAG,"BookListActivity onOptionsItemSelected : ");
        int id = item.getItemId();
        if (id == R.id.action_settings)
        {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }*/

    public static class BookListFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>  , FetchBooksInfoAsyncTaskListener
    {
        public static final String LOG_TAG = BookListFragment.class.getSimpleName();
        //FetchBooksInfoAsyncTask fetchBooksInfoAsyncTask;
        private ProgressBar BookListProgressBar;
        private ITBDBookSearchAdapter mITBDBookSearchAdapter;
        private ListView BookListView;
        private static final int BOOK_SEARCH_LOADER = 0;
        private static final String[] BOOK_SEARCH_COLUMNS = {
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
            Log.d(LOG_TAG,"BookListFragment() : ");
        }

        @Override
        public void onCreate(Bundle savedInstanceState)
        {
            Log.d(LOG_TAG,"BookListFragment onCreate : ");
            super.onCreate(savedInstanceState);
            updateSearchBookList(SearchQuery, ISBN, BookId);
            //setHasOptionsMenu(true);
            //setRetainInstance(true);
        }

        @Override
        public void onStop()
        {
            Log.d(LOG_TAG,"BookListFragment onStop : ");
            super.onStop();
            //fetchBooksInfoAsyncTask.cancel(true);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
        {

            Log.d(LOG_TAG, "BookListFragment onCreateView : ");
            mITBDBookSearchAdapter = new ITBDBookSearchAdapter(getActivity(), null, 0);
            View rootView = inflater.inflate(R.layout.fragment_book_list, container, false);
            BookListProgressBar = (ProgressBar) rootView.findViewById(R.id.BookListProgressBar);
            BookListView = (ListView) rootView.findViewById(R.id.listview_book_search);
            //updateSearchBookList(SearchQuery, ISBN, BookId);
            BookListView.setAdapter(mITBDBookSearchAdapter);
            BookListView.setClickable(true);

            BookListView.setOnItemClickListener(
                                                            new AdapterView.OnItemClickListener()
                                                            {
                                                                @Override
                                                                public void onItemClick(AdapterView<?> parent, View view, int position, long id)
                                                                {
                                                                    Cursor cursor = mITBDBookSearchAdapter.getCursor();

                                                                    ISBN = cursor.getString(COL_ISBN);
                                                                    BookId = cursor.getString(COL_ID);

                                                                    Log.d(LOG_TAG, " The Chosen Book ID is : " + BookId + " The Chosen Book ISBN is : " + ISBN);
                                                                    if (cursor.moveToPosition(position))
                                                                    {
                                                                        Activity myBookListActivity = getActivity();
                                                                        if (myBookListActivity instanceof BookListActivity)
                                                                            ((BookListActivity) getActivity()).onItemSelected(BookId);

                                                                    }

                                                                }
                                                            });


            return rootView;
        }

        private void updateSearchBookList(String searchQuery, String isbn, String bookId)
        {
            Log.d(LOG_TAG,"BookListFragment updateSearchBookList : ");
            FetchBooksInfoAsyncTask fetchBooksInfoAsyncTask = new FetchBooksInfoAsyncTask(getActivity());
            fetchBooksInfoAsyncTask.asyncResponseDelegate = this;
            fetchBooksInfoAsyncTask.execute(searchQuery, isbn , bookId);
            //fetchBooksInfoAsyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,searchQuery,isbn,bookId);
        }

        @Override
        public void onFetchBooksInfoStarted(Boolean isStarted)
        {
            Log.d(LOG_TAG,"BookListFragment onFetchBooksInfoStarted : ");
            if ((BookListProgressBar == null))
            {
                BookListProgressBar = new ProgressBar(getActivity());
                BookListProgressBar.setVisibility(View.VISIBLE);
            }
            else
            {
                BookListProgressBar.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public void onFetchBooksInfoProgressUpdate(Integer ProgressUpdate)
        {
            Log.d(LOG_TAG,"BookListFragment onFetchBooksInfoProgressUpdate : ");
            BookListProgressBar.setProgress(ProgressUpdate);
        }

        @Override
        public void onFetchBooksInfoComplete(String result)
        {
            Log.d(LOG_TAG,"BookListFragment onFetchBooksInfoComplete : ");
            Log.d(LOG_TAG, "Check if Data has changed in List view " + result);
            BookListProgressBar.setVisibility(View.GONE);
            //getLoaderManager().restartLoader(BOOK_SEARCH_LOADER, null, this);
            getLoaderManager().initLoader(BOOK_SEARCH_LOADER, null, this);
            mITBDBookSearchAdapter.notifyDataSetChanged();
        }

        @Override
        public void onFetchBooksInfoCancelled()
        {
            Log.d(LOG_TAG,"BookListFragment onFetchBooksInfoCancelled : ");
            BookListProgressBar.setVisibility(View.GONE);
        }

        @Override
        public void onActivityCreated(Bundle savedInstanceState)
        {
            Log.d(LOG_TAG,"BookListFragment onActivityCreated : ");
            super.onActivityCreated(savedInstanceState);
            // Initialise Loader here...Loader's lifecycle is bound to Activity Lifecycle not Fragment
            //getLoaderManager().initLoader(BOOK_SEARCH_LOADER, null, this);

        }

        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args)
        {
            Log.d(LOG_TAG,"BookListFragment onCreateLoader : ");
            final String SORT_TITLE_ASC = BookEntry.COLUMN_TITLE + " ASC";
            Uri BOOK_SEARCH_URI = BookEntry.buildBookSearchUriForSearchQuery(SearchQuery);
            return new CursorLoader(getActivity(), BOOK_SEARCH_URI, BOOK_SEARCH_COLUMNS, null, null, SORT_TITLE_ASC);
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data)
        {
            Log.d(LOG_TAG,"BookListFragment onLoadFinished : ");
            mITBDBookSearchAdapter.swapCursor(data);
        }

        @Override
        public void onLoaderReset(Loader loader)
        {
            Log.d(LOG_TAG,"BookListFragment onLoaderReset : ");
            mITBDBookSearchAdapter.swapCursor(null);
        }

    }
}


/*Intent showBookDetailIntent = new Intent(view.getContext(), BookDetailActivity.class);
showBookDetailIntent.setAction(Intent.ACTION_VIEW);
showBookDetailIntent.setType("text/plain");
showBookDetailIntent.putExtra(getActivity().getString(R.string.book_id_label), BookId);
Log.d(LOG_TAG, "showBookDetailIntent is ready");
view.getContext().startActivity(showBookDetailIntent);*/