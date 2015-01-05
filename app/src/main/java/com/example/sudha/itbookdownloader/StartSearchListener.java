package com.example.sudha.itbookdownloader;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.SearchView;
import android.widget.Toast;

/**
 * Created by Sudha on 1/5/2015.
 */
public class StartSearchListener implements SearchView.OnQueryTextListener, SearchView.OnSuggestionListener, View.OnFocusChangeListener, View.OnClickListener
{
    private final String LOG_TAG = StartSearchListener.class.getSimpleName();
    Context searchContext;

    public StartSearchListener(Context context)
    {
        searchContext = context;
        Log.d(LOG_TAG, "StartSearchListener constructor");
    }

    @Override
    public boolean onQueryTextSubmit(String query)
    {
        Log.d(LOG_TAG, "StartSearchListener onQueryTextSubmit");
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText)
    {
        Log.d(LOG_TAG, "StartSearchListener onQueryTextChange");
        return false;
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus)
    {
        Log.d(LOG_TAG, "StartSearchListener onFocusChange");
        Toast.makeText(searchContext, String.valueOf(hasFocus), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onClick(View v)
    {
        Log.d(LOG_TAG, "StartSearchListener onClick");
    }

    @Override
    public boolean onSuggestionSelect(int position)
    {
        Log.d(LOG_TAG, "StartSearchListener onSuggestionSelect");
        return false;
    }

    @Override
    public boolean onSuggestionClick(int position)
    {
        Log.d(LOG_TAG, "StartSearchListener onSuggestionClick");
        return false;
    }
}
