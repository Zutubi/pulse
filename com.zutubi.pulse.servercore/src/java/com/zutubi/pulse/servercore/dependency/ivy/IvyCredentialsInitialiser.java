package com.zutubi.pulse.servercore.dependency.ivy;

import com.zutubi.pulse.servercore.services.TokenManager;
import com.zutubi.pulse.servercore.services.TokenManagerListener;
import org.apache.ivy.util.Credentials;
import org.apache.ivy.util.url.CredentialsStore;

/**
 * Initialise the ivy credentials store to allow ivy to publish artifacts
 * to the internal pulse repository.
 */
public class IvyCredentialsInitialiser implements TokenManagerListener
{
    private TokenManager tokenManager;
    private static final String USER_PULSE = "pulse";
    private static final String REALM = "Pulse";

    public void init()
    {
        // IMPLEMENTATION NOTE: it would be nice if we knew the host of the master, but that is not
        // yet available.  In fact, it is only available (on an agent) during a build.
        // IMPLEMENTATION NOTE 2: we only take a part of the token to avoid a jvm base 64 encoding bug: bug_id=6459815
        String token = tokenManager.getToken();
        if (token != null)
        {
            token = token.substring(0, 12);
        }
        CredentialsStore.INSTANCE.addCredentials(REALM, null, USER_PULSE, token);

        // The token may not yet be available.  On the agent, this means that the token
        // has not been received from the master.  This will happen as part of the
        // first ping, so listen to the manager for any change.
        tokenManager.register(this);
    }

    // IMPLEMENTATION NOTE: So since we do not have the host available on the agent at the time
    // that this initialisation occurs, we do it as part of the build, BEFORE we attempt to publish.
    public static void init(String host)
    {
        // update the store as part of every build.  This is to ensure we include the latest token
        // which may or may not have changed recently.  Unfortunatley we can not lookup the credentials
        // based on realm only, so we can not update during the tokenUpdated callback.

        CredentialsStore store = CredentialsStore.INSTANCE;

        Credentials credentials = store.getCredentials(REALM, null);
        if (credentials != null)
        {
            store.addCredentials(REALM, host, credentials.getUserName(), credentials.getPasswd());
        }
    }

    public void tokenUpdated(String token)
    {
        // update the credentials when the token changes.
        if (token != null)
        {
            token = token.substring(0, 12);
        }
        CredentialsStore.INSTANCE.addCredentials(REALM, null, USER_PULSE, token);
    }

    public void setTokenManager(TokenManager tokenManager)
    {
        this.tokenManager = tokenManager;
    }
}
