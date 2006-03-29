package com.cinnamonbob.web.ajax;

import com.cinnamonbob.model.P4;
import com.cinnamonbob.model.Scm;

/**
 * An ajax request to test perforce settings and send a fragment of HTML
 * with results.
 */
public class TestP4Action extends BaseTestScmAction
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

    public Scm getScm()
    {
        P4 perforceConnection = new P4();
        perforceConnection.setPort(port);
        perforceConnection.setUser(user);
        perforceConnection.setClient(client);
        perforceConnection.setPassword(password);
        return perforceConnection;
    }
}
