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
import android.widget.SearchView;


public class MainActivity extends ActionBarActivity
{

    private final String LOG_TAG = MainActivity.class.getSimpleName();


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState == null)
        {
            getSupportFragmentManager().beginTransaction().add(R.id.container, new MainFragment()).commit();
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.menu_main, menu);
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

    public static class MainFragment extends Fragment
    {
        private final String LOG_TAG = MainFragment.class.getSimpleName();
        SearchView startSearchView;

        public MainFragment()
        {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
        {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            startSearchView = (SearchView) rootView.findViewById(R.id.startSearchView);
            StartSearchListener startSearchListener = new StartSearchListener(getActivity());
            startSearchView.setOnQueryTextListener(startSearchListener);
            /*startSearchView.setOnQueryTextFocusChangeListener(startSearchListener);
            startSearchView.setOnSuggestionListener(startSearchListener);
            startSearchView.setOnClickListener(startSearchListener);*/
            //Log.d(LOG_TAG, "StartSearchListener is set");
            return rootView;
        }
    }
}
