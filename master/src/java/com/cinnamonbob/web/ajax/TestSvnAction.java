package com.cinnamonbob.web.ajax;

import com.cinnamonbob.scm.SCMException;
import com.cinnamonbob.scm.svn.SVNServer;
import com.cinnamonbob.web.ActionSupport;
import com.opensymphony.util.TextUtils;

/**
 * An ajax request to test subversion settings and send a fragment of HTML
 * with results.
 */
public class TestSvnAction extends ActionSupport
{
    private String username;
    private String password;
    private String url;
    private String keyfile;
    private String passphrase;


    public void setUsername(String username)
    {
        this.username = username;
    }

    public void setPassword(String password)
    {
        this.password = password;
    }

    public void setUrl(String url)
    {
        this.url = url;
    }

    public void setKeyfile(String keyfile)
    {
        this.keyfile = keyfile;
    }

    public void setPassphrase(String passphrase)
    {
        this.passphrase = passphrase;
    }

    public String execute()
    {
        if (!TextUtils.stringSet(username))
        {
            addActionError("username is required");
        }

        if (!TextUtils.stringSet(url))
        {
            addActionError("url is required");
        }

        if (!TextUtils.stringSet(password))
        {
            password = null;
        }

        if (!TextUtils.stringSet(keyfile))
        {
            keyfile = null;
        }

        if (!TextUtils.stringSet(passphrase))
        {
            passphrase = null;
        }

        if (hasErrors())
        {
            // We are just testing, we always succeed in testing, even if the
            // result is a test failure!
            return SUCCESS;
        }

        try
        {
            SVNServer server;

            if (keyfile == null)
            {
                server = new SVNServer(url, username, password);
            }
            else if (passphrase == null)
            {
                server = new SVNServer(url, username, password, keyfile);
            }
            else
            {
                server = new SVNServer(url, username, password, keyfile, passphrase);
            }

            server.testConnection();
        }
        catch (SCMException e)
        {
            addActionError(e.getMessage());
        }

        return SUCCESS;
    }
}
