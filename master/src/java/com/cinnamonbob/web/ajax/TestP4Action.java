package com.cinnamonbob.web.ajax;

import com.cinnamonbob.scm.SCMException;
import com.cinnamonbob.scm.p4.P4Server;
import com.cinnamonbob.web.ActionSupport;
import com.opensymphony.util.TextUtils;

/**
 * An ajax request to test perforce settings and send a fragment of HTML
 * with results.
 */
public class TestP4Action extends ActionSupport
{
    private String port;
    private String user;
    private String password;
    private String client;

    public void setPort(String port)
    {
        this.port = port;
    }

    public void setUser(String user)
    {
        this.user = user;
    }

    public void setPassword(String password)
    {
        this.password = password;
    }

    public void setClient(String client)
    {
        this.client = client;
    }

    public String execute()
    {
        if (!TextUtils.stringSet(port))
        {
            addActionError("port is required");
        }

        if (!TextUtils.stringSet(user))
        {
            addActionError("user is required");
        }

        if (!TextUtils.stringSet(client))
        {
            addActionError("client is required");
        }

        if (!TextUtils.stringSet(password))
        {
            password = null;
        }

        if (hasErrors())
        {
            // We are just testing, we always succeed in testing, even if the
            // result is a test failure!
            return SUCCESS;
        }

        try
        {
            P4Server server = new P4Server(port, user, password, client);
            server.testConnection();
        }
        catch (SCMException e)
        {
            addActionError(e.getMessage());
        }

        return SUCCESS;
    }
}
