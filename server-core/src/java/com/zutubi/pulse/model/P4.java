package com.zutubi.pulse.model;

import com.zutubi.pulse.scm.SCMConfiguration;
import com.zutubi.pulse.scm.SCMException;
import com.zutubi.pulse.scm.SCMServer;
import com.zutubi.pulse.scm.p4.P4Server;

import java.util.Map;
import java.util.TreeMap;

/**
 * 
 *
 */
public class P4 extends Scm
{
    private final String PORT = "p4.port";
    private final String USER = "p4.user";
    private final String PASSWORD = "p4.password";
    private final String CLIENT = "p4.client";

    @Override
    public SCMServer createServer() throws SCMException
    {
        P4Server server = new P4Server(getPort(), getUser(), getPassword(), getClient());
        server.setExcludedPaths(getFilteredPaths());
        return server;
    }

    public String getType()
    {
        return SCMConfiguration.TYPE_PERFORCE;
    }

    public Map<String, String> getRepositoryProperties()
    {
        Map<String, String> result = new TreeMap<String, String>();
        result.put(PORT, getPort());
        result.put(CLIENT, getClient());
        return result;
    }

    public String getPort()
    {
        return (String) getProperties().get(PORT);
    }

    public void setPort(String port)
    {
        getProperties().put(PORT, port);
    }

    public String getUser()
    {
        return (String) getProperties().get(USER);
    }

    public void setUser(String user)
    {
        getProperties().put(USER, user);
    }

    public String getPassword()
    {
        return (String) getProperties().get(PASSWORD);
    }

    public void setPassword(String password)
    {
        getProperties().put(PASSWORD, password);
    }

    public String getClient()
    {
        return (String) getProperties().get(CLIENT);
    }

    public void setClient(String client)
    {
        getProperties().put(CLIENT, client);
    }
}
