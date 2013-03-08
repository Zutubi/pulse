package com.zutubi.pulse.core.dependency.ivy;

import org.apache.ivy.util.url.CredentialsStore;

import java.util.concurrent.Callable;

/**
 * This is a helper class that handles setting up the authentication credentials
 * for ivy related actions that require remote server authorisation.
 */
public class AuthenticatedAction
{
    public static final String USER = "pulse";
    public static final String REALM = "Pulse";

    public static synchronized <T> T execute(String host, String password, Callable<T> function) throws Exception
    {
        CredentialsStore.INSTANCE.addCredentials(REALM, host, USER, password);
        try
        {
            return function.call();
        }
        finally
        {
            CredentialsStore.INSTANCE.addCredentials(REALM, host, USER, "");
        }
    }
}
