package com.example.sudha.itbookdownloader;


import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;


public class BookListActivity extends ActionBarActivity
{
    private final String LOG_TAG = BookListActivity.class.getSimpleName();
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_list);
        if (savedInstanceState == null)
        {
            getFragmentManager().beginTransaction().add(R.id.book_list_activity, new BookListFragment()).commit();
            Intent intent = getIntent();
            if (Intent.ACTION_SEARCH.equals(intent.getAction()))
            {
                Bundle bundle = intent.getExtras();
                String searchQuery = bundle.getString("searchquery");
                doMySearch(searchQuery);
            }
        }

    }

    private void doMySearch(String query)
    {
        Log.d(LOG_TAG, "Received Search Query from the Intent : " + query);

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



    public static class BookListFragment extends Fragment
    {
        ArrayAdapter <String> stringArrayAdapter;
        public BookListFragment()
        {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState)
        {
            View rootView = inflater.inflate(R.layout.fragment_book_list, container, false);
            String bookListInfo = getString(R.string.book_list_info);

            List<String> bookTitleArrayList= new ArrayList<>();

            bookTitleArrayList.add(0, bookListInfo);
            bookTitleArrayList.add(1, bookListInfo);
            bookTitleArrayList.add(2, bookListInfo);
            bookTitleArrayList.add(3, bookListInfo);
            bookTitleArrayList.add(4, bookListInfo);
            bookTitleArrayList.add(5, bookListInfo);
            bookTitleArrayList.add(6, bookListInfo);
            bookTitleArrayList.add(7, bookListInfo);
            bookTitleArrayList.add(8, bookListInfo);
            bookTitleArrayList.add(9, bookListInfo);
            bookTitleArrayList.add(10, bookListInfo);

            stringArrayAdapter = new ArrayAdapter<>(getActivity(), R.layout.list_item_book, R.id.list_item_book_title, bookTitleArrayList);

            ListView book_listview = (ListView) rootView.findViewById(R.id.fragment_book_listview);
            book_listview.setAdapter(stringArrayAdapter);

            book_listview.setClickable(true);
            book_listview.setOnItemClickListener(new BookListViewOnItemClickListener());

            return rootView;
        }
    }
}
