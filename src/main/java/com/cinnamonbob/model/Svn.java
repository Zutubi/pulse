package com.cinnamonbob.model;

import com.cinnamonbob.scm.SCMException;
import com.cinnamonbob.scm.SCMServer;
import com.cinnamonbob.scm.svn.SVNServer;

/**
 * 
 *
 */
public class Svn extends Scm
{
    private final String URL = "svn.url";
    private final String USERNAME = "svn.username";
    private final String PASSWORD = "svn.password";
    private final String KEYFILE = "svn.keyfile";
    private final String PASSPHRASE = "svn.passphrase";

    @Override
    public SCMServer createServer() throws SCMException
    {
        if(getKeyfile() == null || getKeyfile().length() == 0)
        {
            return new SVNServer(getUrl(), getUsername(), getPassword());
        }
        else
        {
            if(getPassphrase() == null || getPassphrase().length() == 0)
            {
                return new SVNServer(getUrl(), getUsername(), getPassword(), getKeyfile());
            }
            else
            {
                return new SVNServer(getUrl(), getUsername(), getPassword(), getKeyfile(), getPassphrase());
            }
        }
    }

    public String getUrl()
    {
        return (String) getProperties().get(URL);
    }

    public void setUrl(String url)
    {
        getProperties().put(URL, url);
    }

    public String getUsername()
    {
        return (String) getProperties().get(USERNAME);
    }

    public void setUsername(String username)
    {
        getProperties().put(USERNAME, username);
    }

    public String getPassword()
    {
        return (String) getProperties().get(PASSWORD);
    }

    public void setPassword(String password)
    {
        getProperties().put(PASSWORD, password);
    }

    public String getKeyfile()
    {
        return (String) getProperties().get(KEYFILE);
    }

    public void setKeyfile(String keyfile)
    {
        getProperties().put(KEYFILE, keyfile);
    }

    public String getPassphrase()
    {
        return (String) getProperties().get(PASSPHRASE);
    }

    public void setPassphrase(String passphrase)
    {
        getProperties().put(PASSPHRASE, passphrase);
    }
}
