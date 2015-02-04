package com.example.sudha.itbookdownloader;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Created by Sudha on 2/4/2015.
 */
public class BookDownloadButtonListener implements View.OnClickListener
{
    private static String LOG_TAG         = BookDownloadButtonListener.class.getSimpleName();
    private static String FileName        = "ITBDDownloadedFile.pdf";
    private static String FileDownloadUrl = "http://filepi.com/i/vCDduoE";
    private static String FileContents    = " No Data ";
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
            if(!FileDownloadUrl.isEmpty())
            {
                DownloadFile downloadFile = new DownloadFile(context);
                downloadFile.execute(FileDownloadUrl,WebsiteBookNumber);
            }
            if (!FileContents.isEmpty())
                writeToSDFile(FileName, FileFormat, FileContents);
        }
        else
        {
            Toast SdCardNotAvailableToast = new Toast(context);
            SdCardNotAvailableToast.setDuration(Toast.LENGTH_LONG);
            SdCardNotAvailableToast.setText(" Sd Card is not available for writing the downloaded PDF file ");
            SdCardNotAvailableToast.show();
        }

    }

    private void writeToSDFile(String mFileName, String mFileFormat, String mFileContents)
    {

        try
        {

            File SdCard = android.os.Environment.getExternalStorageDirectory();
            Log.d(LOG_TAG, " External file system SdCard: " + SdCard);
            File dir = new File( SdCard.getAbsolutePath() + context.getString(R.string.download_dir_path));
            boolean isDirectoryCreated = dir.mkdirs();
            if (isDirectoryCreated)
            {
                File file = new File(dir, mFileName+"."+mFileFormat);
                FileOutputStream fos = new FileOutputStream(file);
                PrintWriter pw = new PrintWriter(fos);
                pw.print(mFileContents);
                pw.flush();
                pw.close();
                fos.close();

                Log.d(LOG_TAG, " Finished writing File to SdCard :  " + file);
                Toast.makeText(context, " Finished writing " + mFileName + " PDF file to Sd Card at location :  " + dir.getAbsolutePath(), Toast.LENGTH_LONG).show();

            }
        }
        catch ( FileNotFoundException e )
        {
            e.printStackTrace();
            Log.i(LOG_TAG, " File not found. Did you add a WRITE_EXTERNAL_STORAGE permission to the manifest?");
        }
        catch ( IOException e )
        {
            e.printStackTrace();
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
        Log.d(LOG_TAG, "External Media: readable = " + mExternalStorageAvailable + " writable = " + mExternalStorageWriteable);
    }


    private class DownloadFile extends AsyncTask<String, Void, String>
    {
        private final String LOG_TAG = DownloadFile.class.getSimpleName();
        Context DownloadFileContext;
        String Result;

        public DownloadFile(Context context)
        {
            this.DownloadFileContext = context;
        }

        @Override
        protected String doInBackground(String... strings)
        {
            try
            {
                Utility utility = new Utility(DownloadFileContext);
                String fileDownloadUrl = strings[0];
                String websiteBookNumber = strings[1];
                Result = utility.makeBookDownloadNetworkApiCall(fileDownloadUrl, websiteBookNumber);
                Log.d(LOG_TAG, "Initiated File Download : " );
            }
            catch (Exception e)
            {
                Log.e(LOG_TAG, "Error in File Download " +  e.getMessage());
                e.printStackTrace();
            }

            return Result;
        }

        @Override
        protected void onPostExecute(String result)
        {
            Log.d(LOG_TAG, " File Download Finished : " + result);
            FileContents = result;
        }
    }
}
