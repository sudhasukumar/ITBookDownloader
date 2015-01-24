package com.example.sudha.itbookdownloader;

import java.util.HashMap;
import java.util.List;

/**
 * Created by Sudha on 1/19/2015.
 */
public interface FetchBooksForSearchQueryListener
{
    void onFetchBooksForSearchQuery(List<HashMap<String, String>> ArrayListOfStrings);
}
