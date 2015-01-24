package com.example.sudha.itbookdownloader;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;

/**
 * Created by Sudha on 1/22/2015.
 */
public class ITBDBookSearchAdapter extends CursorAdapter
{
    public ITBDBookSearchAdapter(Context context, Cursor c, int flags)
    {
        super(context, c, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent)
    {
        return null;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor)
    {

    }
}
