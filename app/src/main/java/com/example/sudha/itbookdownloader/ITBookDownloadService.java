package com.example.sudha.itbookdownloader;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.util.HashMap;

// An IntentService subclass for handling asynchronous download requests in a service on a separate handler thread.
public class ITBookDownloadService extends IntentService
{
    private static String LOG_TAG = ITBookDownloadService.class.getSimpleName();

    Notification.Builder builder;

    public ITBookDownloadService()
    {
        super(Constants.IT_BOOK_DOWNLOAD_SERVICE_LABEL);
    }

    @Override
    protected void onHandleIntent(Intent intent)
    {
        if ( intent != null )
        {
            NotificationManager DownloadCompleteNotificationMgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            final String action = intent.getAction();
            switch (action)
            {
                case Constants.ACTION_DOWNLOAD_BOOK:
                    String WebsiteBookNumber = intent.getStringExtra(this.getString(R.string.website_book_number_label));
                    String FileDownloadUrl = intent.getStringExtra(this.getString(R.string.file_download_url_label));
                    String FileName = intent.getStringExtra(this.getString(R.string.file_name_label));
                    String FileFormat = intent.getStringExtra(this.getString(R.string.file_format_label));

                    handleActionDownload(FileDownloadUrl, WebsiteBookNumber, FileName, FileFormat);
                    break;
                case Constants.ACTION_DOWNLOAD_COMPLETE:
                {
                    final String DownloadedFileUrl = intent.getStringExtra(this.getString(R.string.downloaded_file_url_label));
                    handleActionDownloadComplete(DownloadedFileUrl);
                    break;
                }
                case Constants.ACTION_OPEN_DOWNLOADED_BOOK:
                {
                    final String DownloadedFileUrl = intent.getStringExtra(this.getString(R.string.downloaded_file_url_label));
                    handleActionOpenDownloadedBook(DownloadedFileUrl);
                    break;
                }
                case Constants.ACTION_NOTIFICATION_DISMISS:
                    DownloadCompleteNotificationMgr.cancel(Constants.NOTIFICATION_ID);
                    break;
            }
        }
    }

    //  Handle action Download in the provided background thread with the provided parameters.
    private void handleActionDownload(String mFileDownloadUrl, String mWebsiteBookNumber, String mFileName, String mFileFormat)
    {
        HashMap<String,String> FileDownloadResults;

        //Utility utility = new Utility(this);
        FileDownloadResults = Utility.makeBookDownloadNetworkApiCall(this,mFileDownloadUrl, mWebsiteBookNumber, mFileName, mFileFormat);
        if (FileDownloadResults.get(Constants.FILE_DOWNLOAD_STATUS_KEY).equals(Constants.FILE_DOWNLOAD_SUCCESS))
        {
            String BookFilePath = FileDownloadResults.get(Constants.FILE_ABSOLUTE_PATH);
            if ( !BookFilePath.equals("") )
                startActionDownloadComplete(this,BookFilePath);
        }
        else
        {
            Toast UnableToDownloadFileToast = Toast.makeText(this," Unable to downloaded file ",Toast.LENGTH_LONG);
            UnableToDownloadFileToast.show();
        }

    }

    public static void startActionDownloadComplete(Context context, String mDownloadedFileUrl)
    {
        Intent InvokeDownloadCompleteIntent = new Intent(context, ITBookDownloadService.class);
        InvokeDownloadCompleteIntent.setAction(Constants.ACTION_DOWNLOAD_COMPLETE);
        InvokeDownloadCompleteIntent.putExtra(context.getString(R.string.downloaded_file_url_label), mDownloadedFileUrl);
        context.startService(InvokeDownloadCompleteIntent);
    }

    //  Handle action DownloadComplete in the provided background thread with the provided parameters.
    private void handleActionDownloadComplete(String mDownloadedFileUrl)
    {
        NotificationManager HandleDownloadCompleteNotificationMgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);


        // Sets up the Snooze and Dismiss action buttons that will appear in the expanded view of the notification.
        Intent DismissNotificationIntent = new Intent(this, ITBookDownloadService.class);
        DismissNotificationIntent.setAction(Constants.ACTION_NOTIFICATION_DISMISS);
        PendingIntent PendingDismissIntent = PendingIntent.getService(this, 0, DismissNotificationIntent, 0);

        Intent OpenBookFromNotificationIntent = new Intent(this, ITBookDownloadService.class);
        OpenBookFromNotificationIntent.setAction(Constants.ACTION_OPEN_DOWNLOADED_BOOK);
        //OpenBookFromNotificationIntent.setType("text/plain");
        OpenBookFromNotificationIntent.putExtra(this.getString(R.string.downloaded_file_url_label), mDownloadedFileUrl);
        Log.d(LOG_TAG, "OpenDownloadedBookIntent is ready");
        PendingIntent PendingOpenDocIntent = PendingIntent.getService(this, 0, OpenBookFromNotificationIntent, 0);

        // Constructs the Builder object.Sets the big view "big text" style and supplies the text that will be displayed in the detail area of the expanded notification.
        builder = new Notification.Builder(this).setSmallIcon(R.drawable.ic_stat_notification)
                                                .setContentTitle(getString(R.string.itbd_notification_context_text))
                                                .setContentText(getString(R.string.download_complete_notification_string))
                                                .setDefaults(Notification.DEFAULT_ALL)  // requires VIBRATE permission
                                                .setStyle(new Notification.BigTextStyle().bigText(getString(R.string.itbd_big_notification_text)))
                                                .addAction(R.drawable.ic_stat_dismiss, getString(R.string.notification_dismiss_label), PendingDismissIntent)
                                                .addAction(R.drawable.ic_launcher, getString(R.string.notification_open_book_label), PendingOpenDocIntent);


        HandleDownloadCompleteNotificationMgr.notify(Constants.NOTIFICATION_ID, builder.build());
    }

    private void handleActionOpenDownloadedBook(String downloadedFileUrl)
    {
        File DownloadedBookFile = new File(downloadedFileUrl);
        if ( DownloadedBookFile.exists() && !DownloadedBookFile.isDirectory() )
        {
            Uri pdfUri = Uri.parse(downloadedFileUrl);
            Intent ChooseApplicationsToOpenBookIntent = new Intent(Intent.ACTION_VIEW);
            ChooseApplicationsToOpenBookIntent.setDataAndType(pdfUri, "application/pdf");
            startActivity(ChooseApplicationsToOpenBookIntent);
            /*ChooseApplicationsToOpenBookIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            String title = getResources().getString(R.string.open_book_chooser_title);        // Always use string resources for UI text. This says something like "Share this photo with"
            Intent chooser = Intent.createChooser(ChooseApplicationsToOpenBookIntent, title);        // Create intent to show chooser
            if ( ChooseApplicationsToOpenBookIntent.resolveActivity(getPackageManager()) != null )         // Verify the intent will resolve to at least one activity
            {
                startActivity(chooser);
            }*/
        }
        else
        {
            Toast UnableToOpenFileToast = Toast.makeText(this," Unable to Open the downloaded PDF file ",Toast.LENGTH_LONG);
            UnableToOpenFileToast.show();
        }
    }
}
