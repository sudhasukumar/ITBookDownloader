package com.example.sudha.itbookdownloader;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import static com.example.sudha.itbookdownloader.BookListActivity.BookListFragment.COL_IMAGE_LINK;

/**
 * Created by Sudha on 1/22/2015.
 */
public class ITBDBookSearchAdapter extends CursorAdapter
{
    private static final int VIEW_TYPE_COUNT = 2;
    private static final int VIEW_TYPE_HIGHLIGHTED_BOOK = 0;
    private static final int VIEW_TYPE_NOT_HIGHLIGHTED_BOOK = 1;

    // Flag to determine if we want to use a separate view for the currently highlighted book.
    private boolean isHighlightedBookLayout = true;

    public static class ViewHolder
    {
        /* Columns fetched from the cursor BookEntry._ID, BookEntry.COLUMN_TITLE, BookEntry.COLUMN_SUBTITLE, BookEntry.COLUMN_ISBN, BookEntry.COLUMN_IMAGE_LINK, BookEntry.COLUMN_DESCRIPTION, BookEntry.COLUMN_BOOK_SEARCH_QUERY*/
        public final ImageView ImageView;
        public final TextView TitleView;
        public final TextView SubTitleView;
        public final TextView ISBNLabelView;
        public final TextView ISBNView;

        public ViewHolder(View view)
        {
            ImageView = (ImageView) view.findViewById(R.id.list_item_book_image_view);
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
        int viewType = getItemViewType(cursor.getPosition());
        int layoutId = -1;
        switch (viewType)
        {
            case VIEW_TYPE_HIGHLIGHTED_BOOK:
            {
                layoutId = R.layout.list_item_book_search_highlighted;
                break;
            }
            case VIEW_TYPE_NOT_HIGHLIGHTED_BOOK:
            {
                layoutId = R.layout.list_item_book_search;
                break;
            }
        }

        View view = LayoutInflater.from(context).inflate(layoutId, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);
        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor)
    {
        ViewHolder viewHolder = (ViewHolder) view.getTag();
        int viewType = getItemViewType(cursor.getPosition());
        switch (viewType)
        {
            case VIEW_TYPE_HIGHLIGHTED_BOOK:
            {
                viewHolder.ImageView.setImageResource(Utility.getArtResourceForBookCover(cursor.getString(COL_IMAGE_LINK)));
                break;
            }
            case VIEW_TYPE_NOT_HIGHLIGHTED_BOOK:
            {
                viewHolder.ImageView.setImageResource(Utility.getArtResourceForBookCover(cursor.getString(COL_IMAGE_LINK)));
                break;
            }
        }

        String Title = cursor.getString(BookListActivity.BookListFragment.COL_TITLE);
        viewHolder.TitleView.setText(Title);

        viewHolder.ImageView.setContentDescription(Title);

        String Subtitle = cursor.getString(BookListActivity.BookListFragment.COL_SUBTITLE);
        viewHolder.SubTitleView.setText(Subtitle);


        String isbn = cursor.getString(BookListActivity.BookListFragment.COL_ISBN);
        viewHolder.ISBNView.setText(isbn);
    }

    public void setHighlightedBookLayout(boolean isHighlightedBookLayoutParam)
    {
        isHighlightedBookLayout = isHighlightedBookLayoutParam;
    }

    @Override
    public int getItemViewType(int position)
    {
        return (isHighlightedBookLayout) ? VIEW_TYPE_HIGHLIGHTED_BOOK : VIEW_TYPE_NOT_HIGHLIGHTED_BOOK;
    }

    @Override
    public int getViewTypeCount()
    {
        return VIEW_TYPE_COUNT;
    }
}
