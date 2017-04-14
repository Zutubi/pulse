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

package com.zutubi.pulse.core.plugins.repository.http;

import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import com.zutubi.pulse.core.plugins.repository.*;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;

/**
 * A plugin repository that is stored on an HTTP server.  The repository is
 * modelled as files on a filesystem:
 * <ul>
 *     <li>available.xml - a document describing the available plugins</li>
 *     <li>plugins/ - a directory holding plugin jars</li>
 *     <li>plugins/&lt;id&gt;-&lt;version&gt;.jar - the jar file for plugin 'id' version 'version'</li>
 * </ul>
 */
public class HttpPluginRepository implements PluginRepository
{
    public static final String PATH_AVAILABLE = "available.xml";
    public static final String PATH_PLUGINS   = "plugins";
    
    public static final String EXTENSION_JAR  = ".jar";
    
    private String baseUrl;

    /**
     * Create a new repository that lives on an HTTP server at the given URL.
     * 
     * @param baseUrl location of the root of the repository file system
     */
    public HttpPluginRepository(String baseUrl)
    {
        if (!baseUrl.endsWith("/"))
        {
            baseUrl += "/";
        }
        
        this.baseUrl = baseUrl;
    }

    public Collection<PluginInfo> getAvailablePlugins(Scope scope)
    {
        HttpClient client = new HttpClient();
        GetMethod get = new GetMethod(baseUrl + PATH_AVAILABLE);
        try
        {
            int code = client.executeMethod(get);
            if (code != HttpStatus.SC_OK)
            {
                throw new PluginRepositoryException("Unable to list plugins: Server returned response code " + code + " (" + HttpStatus.getStatusText(code) + ")");
            }

            return Lists.newArrayList(Collections2.filter(PluginList.read(get.getResponseBodyAsStream()), new PluginScopePredicate(scope)));
        }
        catch (IOException e)
        {
            throw new PluginRepositoryException("Unable to list plugins: " + e.getMessage(), e);
        }
        finally
        {
            get.releaseConnection();
        }
    }

    public URI getPluginLocation(PluginInfo pluginInfo)
    {
        try
        {
            return new URI(baseUrl + getPluginPath(pluginInfo.getId(), pluginInfo.getVersion()));
        }
        catch (URISyntaxException e)
        {
            throw new RuntimeException(e);
        }
    }

    private String getPluginPath(String id, String version)
    {
        return PATH_PLUGINS + "/" + getPluginFileName(id, version);
    }

    private String getPluginFileName(String id, String version)
    {
        return id + "-" + version + EXTENSION_JAR;
    }
}
