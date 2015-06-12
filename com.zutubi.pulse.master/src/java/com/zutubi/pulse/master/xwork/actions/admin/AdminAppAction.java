package com.zutubi.pulse.master.xwork.actions.admin;

import com.zutubi.pulse.master.xwork.actions.ActionSupport;

/**
 * Trivial action that serves as an entry point to the Pulse 3 admin application. The UI is all
 * controlled from the client side, talking to APIs.
 */
public class AdminAppAction extends ActionSupport
{
    private String path;

    public String getPath()
    {
        return path;
    }

    public void setPath(String path)
    {
        this.path = path;
    }
}
