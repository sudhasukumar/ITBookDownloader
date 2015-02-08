package com.example.sudha.itbookdownloader;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.example.sudha.itbookdownloader.volley.MyVolley;

import static com.example.sudha.itbookdownloader.BookListActivity.BookListFragment;

/**
 * Created by Sudha on 1/22/2015.
 */
public class ITBDBookSearchAdapter extends CursorAdapter
{

    public static class ViewHolder
    {
        /* Columns fetched from the cursor BookEntry._ID, BookEntry.COLUMN_TITLE, BookEntry.COLUMN_SUBTITLE, BookEntry.COLUMN_ISBN, BookEntry.COLUMN_IMAGE_LINK, BookEntry.COLUMN_DESCRIPTION, BookEntry.COLUMN_BOOK_SEARCH_QUERY*/
        public final NetworkImageView BookThumbnail;
        public final TextView  TitleView;
        public final TextView  SubTitleView;
        public final TextView  ISBNLabelView;
        public final TextView  ISBNView;

        public ViewHolder(View view)
        {
            BookThumbnail = (NetworkImageView) view.findViewById(R.id.list_item_book_image_view);
            TitleView = (TextView) view.findViewById(R.id.list_item_book_title_textview);
            SubTitleView = (TextView) view.findViewById(R.id.list_item_book_subtitle_textview);
            ISBNLabelView = (TextView) view.findViewById(R.id.list_item_book_ISBN_textview_label);
            ISBNView = (TextView) view.findViewById(R.id.list_item_book_ISBN_textview);
        }
    }

    public ITBDBookSearchAdapter(Context context, Cursor c, int flags)
    {
        super(context, c, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent)
    {
        View view = LayoutInflater.from(context).inflate(R.layout.list_item_book_search, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);
        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor)
    {
        ViewHolder viewHolder = (ViewHolder) view.getTag();

        MyVolley.init(context.getApplicationContext()); // we need application context to make it a singleton
        ImageLoader mImageLoader = MyVolley.getImageLoader();

        viewHolder.BookThumbnail.setImageUrl(cursor.getString(BookListFragment.COL_IMAGE_LINK), mImageLoader);

        String Title = cursor.getString(BookListFragment.COL_TITLE);
        viewHolder.TitleView.setText(Title);

        viewHolder.BookThumbnail.setContentDescription(Title);

        String Subtitle = cursor.getString(BookListFragment.COL_SUBTITLE);
        viewHolder.SubTitleView.setText(Subtitle);

        String isbn = cursor.getString(BookListFragment.COL_ISBN);
        viewHolder.ISBNView.setText(isbn);
    }

}
