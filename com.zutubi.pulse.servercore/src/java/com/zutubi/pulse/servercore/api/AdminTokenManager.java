package com.zutubi.pulse.servercore.api;

import com.zutubi.pulse.core.util.FileSystemUtils;
import com.zutubi.util.RandomUtils;
import com.zutubi.util.logging.Logger;

import java.io.File;
import java.io.IOException;

/**
 * Manages a random, single-use admin token that is used by local command line
 * scripts to authenticate via the remote api.
 */
public class AdminTokenManager
{
    private static final Logger LOG = Logger.getLogger(AdminTokenManager.class);

    private static final String TOKEN_FILE = "admin.token";

    /**
     * A random token that allows a single admin login: the token is changed
     * each time it it used.
     */
    private String adminToken;
    private File configRoot;

    public static File getAdminTokenFilename(File configRoot)
    {
        return new File(configRoot, TOKEN_FILE);
    }

    /**
     * Init method. Make sure that we have an admin token available for use by
     * the command line.
     */
    public void init()
    {
        newRandomToken();
    }

    public boolean checkAdminToken(String token)
    {
        if (token.equals(adminToken))
        {
            // Matches the single-use admin token.  Allow login and generate
            // a new token.
            newRandomToken();
            return true;
        }
        return false;
    }

    private void newRandomToken()
    {
        File tokenFile = getAdminTokenFilename(configRoot);
        adminToken = RandomUtils.randomString(128);
        try
        {
            FileSystemUtils.createFile(tokenFile, adminToken);
        }
        catch (IOException e)
        {
            LOG.severe("Unable to write admin token file: " + e.getMessage(), e);
        }
    }

    public void setConfigRoot(File configRoot)
    {
        this.configRoot = configRoot;
    }
}
