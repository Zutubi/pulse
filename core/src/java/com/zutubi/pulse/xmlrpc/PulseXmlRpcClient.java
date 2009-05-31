package com.zutubi.pulse.xmlrpc;

import com.zutubi.pulse.scm.SCMConfiguration;
import org.apache.xmlrpc.XmlRpcClient;
import org.apache.xmlrpc.XmlRpcClientException;
import org.apache.xmlrpc.XmlRpcTransport;
import org.apache.xmlrpc.XmlRpcTransportFactory;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Hashtable;
import java.util.Map;
import java.util.Vector;

/**
 * A Java wrapper for convenient invocation of the Pulse XML-RPC API.
 */
public class PulseXmlRpcClient
{
    private XmlRpcClient client;

    public PulseXmlRpcClient(String pulseURL) throws MalformedURLException
    {
        this(pulseURL, null, 0);
    }

    public PulseXmlRpcClient(String pulseUrl, final String proxyHost, final int proxyPort) throws MalformedURLException
    {
        if(!pulseUrl.endsWith("/"))
        {
            pulseUrl += "/";
        }

        pulseUrl += "xmlrpc";
        final URL url = new URL(pulseUrl);
        client = new XmlRpcClient(url, new XmlRpcTransportFactory()
        {
            public XmlRpcTransport createTransport() throws XmlRpcClientException
            {
                return new PulseXmlRpcTransport(url, proxyHost, proxyPort);
            }

            public void setProperty(String propertyName, Object value)
            {
                // No properties supported.
            }
        });
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
            throw new PulseXmlRpcException(e.getMessage(), e);
        }
    }
}
