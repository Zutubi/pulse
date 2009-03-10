package com.zutubi.pulse.servercore.services;

import com.zutubi.pulse.servercore.bootstrap.ConfigurationManager;
import com.zutubi.pulse.servercore.bootstrap.UserPaths;
import com.zutubi.util.FileSystemUtils;
import com.zutubi.util.RandomUtils;
import com.zutubi.util.io.IOUtils;
import com.zutubi.util.logging.Logger;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.LinkedList;

/**
 */
public class ServiceTokenManager implements TokenManager
{
    private static final Logger LOG = Logger.getLogger(ServiceTokenManager.class);

    private static final int TOKEN_LENGTH = 64;
    private static final String TOKEN_FILE = "service.token";

    /**
     * Indicates the policy when no token exists.  If true, a new random
     * token will be generated.  If false, the first token that is valiated
     * will be accepted and stored.
     */
    private boolean generate = true;

    private UserPaths paths;
    private String token;

    private final List<TokenManagerListener> listeners = new LinkedList<TokenManagerListener>();

    public void init()
    {
        File tokenFile = getTokenFile();
        if(tokenFile.exists())
        {
            try
            {
                token = IOUtils.fileToString(tokenFile);
            }
            catch (IOException e)
            {
                LOG.severe("Unable to read service token: " + tokenFile.getAbsolutePath() + ": " + e.getMessage(), e);
            }
        }
        else if(generate)
        {
            try
            {
                token = RandomUtils.secureRandomString(TOKEN_LENGTH);
            }
            catch (GeneralSecurityException e)
            {
                token = RandomUtils.randomString(TOKEN_LENGTH);
            }

            writeToken();
        }
    }

    public void validateToken(String token) throws InvalidTokenException
    {
        if(this.token == null && !generate)
        {
            this.token = token;
            writeToken();
        }

        if(!token.equals(this.token))
        {
            if(!generate)
            {
                // Check if the token file has been remove, we will refresh if so.
                if(checkRefreshed(token))
                {
                    return;
                }
            }
            throw new InvalidTokenException();
        }
    }

    private boolean checkRefreshed(String token)
    {
        File tokenFile = getTokenFile();
        if(!tokenFile.exists())
        {
            this.token = token;
            writeToken();
            return true;
        }

        return false;
    }

    public String getToken()
    {
        return token;
    }

    private void writeToken()
    {
        File tokenFile = getTokenFile();
        try
        {
            FileSystemUtils.createFile(tokenFile, token);

            notifyListeners();
        }
        catch (IOException e)
        {
            LOG.severe("Unable to write token file: " + tokenFile.getAbsolutePath() + ": " + e.getMessage(), e);
        }
    }

    File getTokenFile()
    {
        return new File(paths.getUserConfigRoot(), TOKEN_FILE);
    }

    public void setGenerate(boolean generate)
    {
        this.generate = generate;
    }

    private void notifyListeners()
    {
        // Just in case the callback is going to take a while, we make a copy so that
        // we reduce any distruption to other threads.
        List<TokenManagerListener> listeners;
        synchronized (this.listeners)
        {
            listeners = new LinkedList<TokenManagerListener>(this.listeners);
        }

        for (TokenManagerListener listener : listeners)
        {
            listener.tokenUpdated(token);
        }
    }

    public void register(TokenManagerListener listener)
    {
        synchronized (listeners)
        {
            if (!listeners.contains(listener))
            {
                listeners.add(listener);
            }
        }
    }

    public void unregister(TokenManagerListener listener)
    {
        synchronized (listeners)
        {
            listeners.remove(listener);
        }
    }

    public void setConfigurationManager(ConfigurationManager configurationManager)
    {
        this.paths = configurationManager.getUserPaths();
    }

}
