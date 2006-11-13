package com.zutubi.pulse.model;

import com.opensymphony.util.TextUtils;
import com.zutubi.pulse.scm.SCMConfiguration;
import com.zutubi.pulse.scm.SCMException;
import com.zutubi.pulse.scm.SCMServer;
import com.zutubi.pulse.scm.svn.SVNServer;
import com.zutubi.pulse.scm.svn.SvnConstants;

import java.util.Map;
import java.util.TreeMap;

/**
 * 
 *
 */
public class Svn extends Scm
{
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
        result.put(SvnConstants.PROPERTY_URL, getUrl());
        return result;
    }

    public String getUrl()
    {
        return (String) getProperties().get(SvnConstants.PROPERTY_URL);
    }

    public void setUrl(String url)
    {
        getProperties().put(SvnConstants.PROPERTY_URL, url);
    }

    public String getUsername()
    {
        return (String) getProperties().get(SvnConstants.PROPERTY_USERNAME);
    }

    public void setUsername(String username)
    {
        getProperties().put(SvnConstants.PROPERTY_USERNAME, username);
    }

    public String getPassword()
    {
        return (String) getProperties().get(SvnConstants.PROPERTY_PASSWORD);
    }

    public void setPassword(String password)
    {
        getProperties().put(SvnConstants.PROPERTY_PASSWORD, password);
    }

    public String getKeyfile()
    {
        return (String) getProperties().get(SvnConstants.PROPERTY_KEYFILE);
    }

    public void setKeyfile(String keyfile)
    {
        getProperties().put(SvnConstants.PROPERTY_KEYFILE, keyfile);
    }

    public String getPassphrase()
    {
        return (String) getProperties().get(SvnConstants.PROPERTY_PASSPHRASE);
    }

    public void setPassphrase(String passphrase)
    {
        getProperties().put(SvnConstants.PROPERTY_PASSPHRASE, passphrase);
    }
}
