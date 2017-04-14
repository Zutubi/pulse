/* Copyright 2017 Zutubi Pty Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zutubi.pulse.dev.xmlrpc;

import com.zutubi.pulse.core.plugins.repository.PluginList;
import com.zutubi.pulse.core.scm.PersonalBuildInfo;
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

    public PersonalBuildInfo preparePersonalBuild(String token, String projectName)
    {
        Hashtable<String, Object> result = execute("RemoteApi.preparePersonalBuild", token, projectName);
        String scmType = (String) result.get(PersonalBuildInfo.SCM_TYPE);
        String scmLocation = (String) result.get(PersonalBuildInfo.SCM_LOCATION);
        @SuppressWarnings({"unchecked"})
        Vector<Hashtable<String, Object>> hashes = (Vector<Hashtable<String, Object>>) result.get(PersonalBuildInfo.PLUGINS);
        return new PersonalBuildInfo(scmType, scmLocation, PluginList.infosFromHashes(hashes));
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
