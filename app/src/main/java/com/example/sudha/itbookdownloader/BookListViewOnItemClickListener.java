package com.example.sudha.itbookdownloader;

import android.content.Intent;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;

/**
 * Created by Sudha on 1/6/2015.
 */
public class BookListViewOnItemClickListener implements android.widget.AdapterView.OnItemClickListener
{
    final String LOG_TAG = BookListViewOnItemClickListener.class.getSimpleName();
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id)
    {

        Toast bookDetailsForItemClick = Toast.makeText(parent.getContext(),"Book Details",Toast.LENGTH_SHORT );
        bookDetailsForItemClick.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
        bookDetailsForItemClick.show();
        Log.d(LOG_TAG, "Toast is shown");

        Intent showBookDetailIntent = new Intent(view.getContext(), BookDetailActivity.class);
        showBookDetailIntent.setType(Intent.ACTION_VIEW);
        showBookDetailIntent.setType("text/plain");
        Log.d(LOG_TAG, "showBookDetailIntent is ready");
        view.getContext().startActivity(showBookDetailIntent);

    }
}
