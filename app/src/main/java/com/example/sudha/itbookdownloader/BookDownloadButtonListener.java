package com.example.sudha.itbookdownloader;

import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

/**
 * Created by Sudha on 2/4/2015.
 */
public class BookDownloadButtonListener implements View.OnClickListener
{
    //private static String LOG_TAG         = BookDownloadButtonListener.class.getSimpleName();
    private static String FileName        = "ITBDDownloadedFile.pdf";
    private static String FileDownloadUrl = "http://filepi.com/i/vCDduoE";
    private static String  WebsiteBookNumber;
    private static String  FileFormat;
    private static Context context;
    private static boolean mExternalStorageAvailable = false;
    private static boolean mExternalStorageWriteable = false;

    public BookDownloadButtonListener(Context mContext, String mFileName, String mFileDownloadUrl, String mWebsiteBookNumber, String fileFormat)
    {
        context = mContext;
        FileName = mFileName;
        FileDownloadUrl = mFileDownloadUrl;
        WebsiteBookNumber = mWebsiteBookNumber;
        FileFormat = fileFormat;
    }

    @Override
    public void onClick(View v)
    {
        checkExternalMedia();
        if ( mExternalStorageAvailable && mExternalStorageWriteable )
        {
            Toast DownloadInitiateToast = Toast.makeText(context," Initiating " + FileName+ "." + FileFormat.toLowerCase() + " Download ",Toast.LENGTH_SHORT);
            DownloadInitiateToast.show();

            Intent BookDownloadServiceIntent = new Intent(context, ITBookDownloadService.class);
            BookDownloadServiceIntent.setAction(Constants.ACTION_DOWNLOAD_BOOK);
            BookDownloadServiceIntent.setType("text/plain");
            BookDownloadServiceIntent.putExtra(context.getString(R.string.website_book_number_label), WebsiteBookNumber);
            BookDownloadServiceIntent.putExtra(context.getString(R.string.file_download_url_label), FileDownloadUrl);
            BookDownloadServiceIntent.putExtra(context.getString(R.string.file_name_label), FileName);
            BookDownloadServiceIntent.putExtra(context.getString(R.string.file_format_label), FileFormat);
            //Log.d(LOG_TAG, "BookDownloadServiceIntent is ready");
            context.startService(BookDownloadServiceIntent);
        }
        else
        {
            Toast SdCardNotAvailableToast = Toast.makeText(context," Sd Card is not available for writing the downloaded PDF file ",Toast.LENGTH_LONG);
            SdCardNotAvailableToast.show();
        }

    }

    private void checkExternalMedia()
    {
        String state = Environment.getExternalStorageState();

        switch (state)
        {
            case Environment.MEDIA_MOUNTED:
                mExternalStorageAvailable = mExternalStorageWriteable = true;       // Can read and write the media
                break;

            case Environment.MEDIA_MOUNTED_READ_ONLY:
                mExternalStorageAvailable = true;                               // Can only read the media
                mExternalStorageWriteable = false;
                break;

            default:
                mExternalStorageAvailable = mExternalStorageWriteable = false;  // Can't read or write
                break;
        }
        //Log.d(LOG_TAG, "External Media: readable = " + mExternalStorageAvailable + " writable = " + mExternalStorageWriteable);
    }

}
