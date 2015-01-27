package com.example.sudha.itbookdownloader.sync;

import android.accounts.AbstractAccountAuthenticator;
import android.accounts.Account;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.NetworkErrorException;
import android.content.Context;
import android.os.Bundle;

/**
 * Created by Sudha on 1/21/2015.
 */
public class ITBDAuthenticator extends AbstractAccountAuthenticator
{
    public ITBDAuthenticator(Context context)// Simple constructor
    {
        super(context);
    }

    @Override
    public Bundle editProperties(AccountAuthenticatorResponse response, String accountType) // Editing properties is not supported
    {
        return null;
    }

    @Override // Don't add additional accounts
    public Bundle addAccount(AccountAuthenticatorResponse response, String accountType, String authTokenType, String[] requiredFeatures, Bundle options) throws NetworkErrorException
    {
        return null;
    }

    @Override // Ignore attempts to confirm credentials
    public Bundle confirmCredentials(AccountAuthenticatorResponse response, Account account, Bundle options) throws NetworkErrorException
    {
        return null;
    }

    @Override // Getting an authentication token is not supported
    public Bundle getAuthToken(AccountAuthenticatorResponse response, Account account, String authTokenType, Bundle options) throws NetworkErrorException
    {
        return null;
    }

    @Override // Getting a label for the auth token is not supported
    public String getAuthTokenLabel(String authTokenType)
    {
        return null;
    }

    @Override // Updating user credentials is not supported
    public Bundle updateCredentials(AccountAuthenticatorResponse response, Account account, String authTokenType, Bundle options) throws NetworkErrorException
    {
        return null;
    }

    @Override // Checking features for the account is not supported
    public Bundle hasFeatures(AccountAuthenticatorResponse response, Account account, String[] features) throws NetworkErrorException
    {
        return null;
    }
}
