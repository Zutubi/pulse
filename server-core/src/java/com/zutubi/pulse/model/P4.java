package com.zutubi.pulse.model;

import com.zutubi.pulse.scm.SCMConfiguration;
import com.zutubi.pulse.scm.SCMException;
import com.zutubi.pulse.scm.ScmClient;
import static com.zutubi.pulse.scm.p4.P4Constants.*;
import com.zutubi.pulse.scm.p4.P4Server;

import java.util.Map;
import java.util.TreeMap;

/**
 * 
 *
 */
public class P4 extends Scm
{
    @Override
    public ScmClient createServer() throws SCMException
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
        result.put(PROPERTY_PORT, getPort());
        result.put(PROPERTY_CLIENT, getClient());
        return result;
    }

    public String getPort()
    {
        return (String) getProperties().get(PROPERTY_PORT);
    }

    public void setPort(String port)
    {
        getProperties().put(PROPERTY_PORT, port);
    }

    public String getUser()
    {
        return (String) getProperties().get(PROPERTY_USER);
    }

    public void setUser(String user)
    {
        getProperties().put(PROPERTY_USER, user);
    }

    public String getPassword()
    {
        return (String) getProperties().get(PROPERTY_PASSWORD);
    }

    public void setPassword(String password)
    {
        getProperties().put(PROPERTY_PASSWORD, password);
    }

    public String getClient()
    {
        return (String) getProperties().get(PROPERTY_CLIENT);
    }

    public void setClient(String client)
    {
        getProperties().put(PROPERTY_CLIENT, client);
    }
}
