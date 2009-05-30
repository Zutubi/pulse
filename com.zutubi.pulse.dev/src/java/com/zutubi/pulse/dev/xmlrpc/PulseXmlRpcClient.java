package com.zutubi.pulse.dev.xmlrpc;

import com.zutubi.pulse.core.scm.ScmLocation;
import org.apache.xmlrpc.XmlRpcClient;
import org.apache.xmlrpc.XmlRpcClientException;
import org.apache.xmlrpc.XmlRpcTransport;
import org.apache.xmlrpc.XmlRpcTransportFactory;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Hashtable;
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

    public ScmLocation preparePersonalBuild(String token, String projectName)
    {
        Hashtable<String, String> result = execute("RemoteApi.preparePersonalBuild", token, projectName);
        return new ScmLocation(result.get(ScmLocation.TYPE), result.get(ScmLocation.LOCATION));
    }

    @SuppressWarnings({"unchecked"})
    private <T> T execute(String method, String... args)
    {
        try
        {
            return (T) client.execute(method, new Vector(Arrays.asList(args)));
        }
        catch (Exception e)
        {
            throw new PulseXmlRpcException(e.getMessage(), e);
        }
    }
}
