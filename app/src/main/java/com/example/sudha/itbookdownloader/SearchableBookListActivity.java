package com.example.sudha.itbookdownloader;


import android.app.ListFragment;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;


public class SearchableBookListActivity extends ActionBarActivity
{
    private final String LOG_TAG = SearchableBookListActivity.class.getSimpleName();
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_list);
        if (savedInstanceState == null)
        {
            getFragmentManager().beginTransaction().add(R.id.container, new BookListFragment()).commit();
            /*Intent intent = getIntent();
            if (Intent.ACTION_SEARCH.equals(intent.getAction()))
            {
                String searchQuery = intent.getStringExtra(SearchManager.QUERY);
                doMySearch(searchQuery);
            }*/
        }

    }

    /*private void doMySearch(String query)
    {
        Log.d(LOG_TAG, "Received Search Query from the Intent : " + query);
    }*/


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
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings)
        {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }



    public static class BookListFragment extends ListFragment
    {

        public BookListFragment()
        {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState)
        {
            View rootView = inflater.inflate(R.layout.fragment_book_list, container, false);
            return rootView;
        }
    }
}
