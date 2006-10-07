package com.zutubi.pulse.model;

import com.opensymphony.util.TextUtils;
import com.zutubi.pulse.scm.SCMConfiguration;
import com.zutubi.pulse.scm.SCMException;
import com.zutubi.pulse.scm.SCMServer;
import com.zutubi.pulse.scm.svn.SVNServer;
import com.zutubi.pulse.scm.svn.SvnWorkingCopy;

import java.util.Map;
import java.util.TreeMap;

/**
 * 
 *
 */
public class Svn extends Scm
{
    private static final String USERNAME = "svn.username";
    private static final String PASSWORD = "svn.password";
    private static final String KEYFILE = "svn.keyfile";
    private static final String PASSPHRASE = "svn.passphrase";

    @Override
    public SCMServer createServer() throws SCMException
    {
        SVNServer server;
        if (!TextUtils.stringSet(getKeyfile()))
        {
            if (TextUtils.stringSet(getUsername()))
            {
                server = new SVNServer(getUrl(), getUsername(), getPassword());
            }
            else
            {
                server = new SVNServer(getUrl());
            }
        }
        else
        {
            if (TextUtils.stringSet(getPassphrase()))
            {
                server = new SVNServer(getUrl(), getUsername(), getPassword(), getKeyfile(), getPassphrase());
            }
            else
            {
                server = new SVNServer(getUrl(), getUsername(), getPassword(), getKeyfile());
            }
        }
        server.setExcludedPaths(this.getFilteredPaths());
        return server;
    }

    public String getType()
    {
        return SCMConfiguration.TYPE_SUBVERSION;
    }

    public Map<String, String> getRepositoryProperties()
    {
        Map<String, String> result = new TreeMap<String, String>();
        result.put(SvnWorkingCopy.PROPERTY_URL, getUrl());
        return result;
    }

    public String getUrl()
    {
        return (String) getProperties().get(SvnWorkingCopy.PROPERTY_URL);
    }

    public void setUrl(String url)
    {
        getProperties().put(SvnWorkingCopy.PROPERTY_URL, url);
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
