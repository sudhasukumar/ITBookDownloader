package com.example.sudha.itbookdownloader;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;

import java.io.File;
import java.util.List;

/**
 * Created by Sudha on 2/5/2015.
 */
public class OpenDownloadedBookActivity extends Activity
{
    private final String LOG_TAG = OpenDownloadedBookActivity.class.getSimpleName();

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        Bundle bundle;
        if ( savedInstanceState == null )
        {
            Intent intent = getIntent();
            if ( Intent.ACTION_VIEW.equals(intent.getAction()) )
            {
                bundle = intent.getExtras();
                String DownloadedFileAbsoluteUrl = bundle.getString(this.getString(R.string.downloaded_file_url_label));
                showPdf(DownloadedFileAbsoluteUrl);
                //Log.d(LOG_TAG, "OpenDownloadedBookActivity Opening File at : " + DownloadedFileAbsoluteUrl);
            }
        }
    }

    public void showPdf(String mDownloadedFileAbsoluteUrl)
    {
        File file = new File(mDownloadedFileAbsoluteUrl);
        PackageManager packageManager = getPackageManager();
        Intent OpenBookIntent = new Intent(Intent.ACTION_VIEW);
        OpenBookIntent.setType("application/pdf");
        List list = packageManager.queryIntentActivities(OpenBookIntent, PackageManager.MATCH_DEFAULT_ONLY);
        if( list.size() > 0 )
        {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_VIEW);
            Uri uri = Uri.fromFile(file);
            intent.setDataAndType(uri, "application/pdf");
            startActivity(intent);
        }
    }

}
