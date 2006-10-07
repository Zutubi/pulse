package com.zutubi.pulse.xmlrpc;

import com.zutubi.pulse.scm.SCMConfiguration;
import org.apache.xmlrpc.XmlRpcClient;

import java.net.MalformedURLException;
import java.util.Hashtable;
import java.util.Map;
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

    public SCMConfiguration preparePersonalBuild(String token, String projectName, String buildSpecification)
    {
        if(buildSpecification == null)
        {
            buildSpecification = "";
        }
        
        Hashtable<String, String> result = (Hashtable<String, String>) execute("RemoteApi.preparePersonalBuild", token, projectName, buildSpecification);

        SCMConfiguration config = new SCMConfiguration(result.get(SCMConfiguration.PROPERTY_TYPE));
        for(Map.Entry<String, String> entry: result.entrySet())
        {
            String key = entry.getKey();
            if(!key.equals(SCMConfiguration.PROPERTY_TYPE))
            {
                config.addProperty(key, entry.getValue());
            }
        }

        return config;
    }

    private Object execute(String method, String... args)
    {
        Vector v = new Vector(args.length);
        for(String arg: args)
        {
            v.add(arg);
        }

        try
        {
            return client.execute(method, v);
        }
        catch (Exception e)
        {
            throw new PulseXmlRpcException(e);
        }
    }
}
