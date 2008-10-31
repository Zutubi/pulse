package com.zutubi.pulse.dev.xmlrpc;

import com.zutubi.pulse.core.scm.ScmLocation;
import org.apache.xmlrpc.XmlRpcClient;

import java.net.MalformedURLException;
import java.util.Hashtable;
import java.util.Vector;

/**
 * A Java wrapper for convenient invocation of the Pulse XML-RPC API.
 */
public class PulseXmlRpcClient
{
    private XmlRpcClient client;

    public PulseXmlRpcClient(String url) throws MalformedURLException
    {
        if(!url.endsWith("/"))
        {
            url += "/";
        }

        url += "xmlrpc";
        client = new XmlRpcClient(url);
    }

    public int getVersion()
    {
        return (Integer) execute("RemoteApi.getVersion");
    }

    public String login(String username, String password)
    {
        return (String) execute("RemoteApi.login", username, password);
    }

    public boolean logout(String token)
    {
        return (Boolean) execute("RemoteApi.logout", token);
    }

    public void failSafeLogout(String token)
    {
        if (token != null)
        {
            try
            {
                logout(token);
            }
            catch(Exception e)
            {
                // Ignore
            }
        }
    }

    public ScmLocation preparePersonalBuild(String token, String projectName)
    {
        Hashtable<String, String> result = execute("RemoteApi.preparePersonalBuild", token, projectName);
        return new ScmLocation(result.get(ScmLocation.TYPE), result.get(ScmLocation.LOCATION));
    }

    @SuppressWarnings({"unchecked"})
    private <T> T execute(String method, String... args)
    {
        Vector v = new Vector(args.length);
        for(String arg: args)
        {
            v.add(arg);
        }

        try
        {
            return (T) client.execute(method, v);
        }
        catch (Exception e)
        {
            throw new PulseXmlRpcException(e.getMessage(), e);
        }
    }
}
