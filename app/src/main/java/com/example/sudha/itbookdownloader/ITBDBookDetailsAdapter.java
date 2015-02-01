package com.example.sudha.itbookdownloader;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.CursorAdapter;
import android.widget.TextView;

import static com.example.sudha.itbookdownloader.BookDetailActivity.BookDetailsFragment.COLUMN_IMAGE_LINK;

/**
 * Created by Sudha on 1/22/2015.
 */
public class ITBDBookDetailsAdapter extends CursorAdapter
{

    public ITBDBookDetailsAdapter(Context context, Cursor c, int flags)
    {
        super(context, c, flags);
    }

    public static class ViewHolder
    {
        public final ImageView DetailBookImageView;
        public final TextView  DetailBookTitleView;
        public final TextView  DetailBookSubTitleView;
        public final TextView  DetailBookAuthorView;
        public final TextView  DetailBookISBNView;
        public final TextView  DetailBookYearView;
        public final TextView  DetailBookPageView;
        public final TextView  DetailBookPublisherView;
        public final TextView  DetailBookDescriptionView;

        public ViewHolder(View view)
        {
            DetailBookImageView         = (ImageView) view.findViewById(R.id.detail_book_image_view);
            DetailBookTitleView         = (TextView) view.findViewById(R.id.detail_book_title_textview);
            DetailBookSubTitleView      = (TextView) view.findViewById(R.id.detail_book_subtitle_textview);
            DetailBookAuthorView        = (TextView) view.findViewById(R.id.detail_book_author_textview);
            DetailBookISBNView          = (TextView) view.findViewById(R.id.detail_book_ISBN_textview);
            DetailBookYearView          = (TextView) view.findViewById(R.id.detail_book_year_textview);
            DetailBookPageView          = (TextView) view.findViewById(R.id.detail_book_page_textview);
            DetailBookPublisherView     = (TextView) view.findViewById(R.id.detail_book_publisher_textview);
            DetailBookDescriptionView   = (TextView) view.findViewById(R.id.detail_book_description_textview);
        }
    }



    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent)
    {
        View view = LayoutInflater.from(context).inflate(R.layout.fragment_book_detail, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);
        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor)
    {
        ViewHolder viewHolder = (ViewHolder) view.getTag();

        viewHolder.DetailBookImageView.setImageResource(Utility.getArtResourceForBookCover(cursor.getString(COLUMN_IMAGE_LINK)));

        String Title = cursor.getString(BookDetailActivity.BookDetailsFragment.COLUMN_TITLE);
        viewHolder.DetailBookTitleView.setText(Title);

        viewHolder.DetailBookImageView.setContentDescription(Title);

        String Subtitle = cursor.getString(BookDetailActivity.BookDetailsFragment.COLUMN_SUBTITLE);
        viewHolder.DetailBookSubTitleView.setText(Subtitle);

        String Author = cursor.getString(BookDetailActivity.BookDetailsFragment.COLUMN_AUTHORNAME);
        viewHolder.DetailBookAuthorView.setText(Author);

        String ISBN = cursor.getString(BookDetailActivity.BookDetailsFragment.COLUMN_ISBN);
        viewHolder.DetailBookISBNView.setText(ISBN);

        String Year = cursor.getString(BookDetailActivity.BookDetailsFragment.COLUMN_YEAR);
        viewHolder.DetailBookYearView.setText(Year);

        String Page = cursor.getString(BookDetailActivity.BookDetailsFragment.COLUMN_PAGE);
        viewHolder.DetailBookPageView.setText(Page);

        String Publisher = cursor.getString(BookDetailActivity.BookDetailsFragment.COLUMN_PUBLISHER);
        viewHolder.DetailBookPublisherView.setText(Publisher);

        String Description = cursor.getString(BookDetailActivity.BookDetailsFragment.COLUMN_DESCRIPTION);
        viewHolder.DetailBookDescriptionView.setText(Description);

    }
}
