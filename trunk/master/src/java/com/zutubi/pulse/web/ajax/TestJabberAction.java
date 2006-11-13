package com.zutubi.pulse.web.ajax;

import com.opensymphony.util.TextUtils;
import com.zutubi.pulse.jabber.JabberManager;
import com.zutubi.pulse.web.ActionSupport;

/**
 * An ajax request to test Jabber settings and send a fragment of HTML
 * with results.
 */
public class TestJabberAction extends ActionSupport
{
    private JabberManager jabberManager;

    private String host;
    private int port;
    private String username;
    private String password;
    private boolean forceSSL = false;

    public void setHost(String host)
    {
        this.host = host;
    }

    public void setPort(int port)
    {
        this.port = port;
    }

    public void setUsername(String username)
    {
        this.username = username;
    }

    public void setPassword(String password)
    {
        this.password = password;
    }

    public void setForceSSL(boolean forceSSL)
    {
        this.forceSSL = forceSSL;
    }

    public String execute() throws Exception
    {
        if(!TextUtils.stringSet(host))
        {
            addActionError(getText("jabber.host.required"));
        }

        if(!TextUtils.stringSet(username))
        {
            addActionError(getText("jabber.username.required"));
        }

        if (!hasErrors())
        {
            try
            {
                jabberManager.testConnection(host, port, username, password, forceSSL);
            }
            catch(Exception e)
            {
                addActionError(e.getMessage());
            }
        }

        return SUCCESS;
    }

    public void setJabberManager(JabberManager jabberManager)
    {
        this.jabberManager = jabberManager;
    }
}
