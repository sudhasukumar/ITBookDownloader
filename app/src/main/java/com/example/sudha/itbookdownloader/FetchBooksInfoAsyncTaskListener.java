package com.example.sudha.itbookdownloader;

/**
 * Created by Sudha on 1/19/2015.
 */
public interface FetchBooksInfoAsyncTaskListener
{
    void onFetchBooksInfoStarted(Boolean isStarted);
    void onFetchBooksInfoProgressUpdate(Integer ProgressUpdate);
    void onFetchBooksInfoComplete(String Result);
    void onFetchBooksInfoCancelled();
}
