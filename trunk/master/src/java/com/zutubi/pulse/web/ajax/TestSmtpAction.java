package com.zutubi.pulse.web.ajax;

import com.opensymphony.util.TextUtils;
import com.zutubi.pulse.model.EmailContactPoint;
import com.zutubi.pulse.web.ActionSupport;

/**
 * An ajax request to test SMTP settings and send a fragment of HTML
 * with results.
 */
public class TestSmtpAction extends ActionSupport
{
    private String host;
    private String from;
    private String username;
    private String password;
    private String prefix;
    private String to;

    public void setHost(String host)
    {
        this.host = host;
    }

    public void setFrom(String from)
    {
        this.from = from;
    }

    public void setUsername(String username)
    {
        this.username = username;
    }

    public void setPassword(String password)
    {
        this.password = password;
    }

    public void setPrefix(String prefix)
    {
        this.prefix = prefix;
    }

    public void setTo(String to)
    {
        this.to = to;
    }

    public String execute() throws Exception
    {
        if(!TextUtils.stringSet(host))
        {
            addActionError(getText("smtp.host.required"));
        }

        if(!TextUtils.stringSet(from))
        {
            addActionError(getText("smtp.from.required"));
        }

        if(!TextUtils.stringSet(to))
        {
            addActionError(getText("smtp.to.required"));
        }

        if (!hasErrors())
        {
            try
            {
                EmailContactPoint.sendMail(to, prefix + " Test Email", "text/plain", "Welcome to Zutubi Pulse!", host, username, password, from);
            }
            catch(Exception e)
            {
                addActionError(e.getMessage());
            }
        }

        return SUCCESS;
    }
}
