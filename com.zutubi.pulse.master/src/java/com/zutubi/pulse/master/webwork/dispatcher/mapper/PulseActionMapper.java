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

package com.zutubi.pulse.master.webwork.dispatcher.mapper;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.opensymphony.webwork.dispatcher.mapper.ActionMapper;
import com.opensymphony.webwork.dispatcher.mapper.ActionMapping;
import com.opensymphony.webwork.dispatcher.mapper.DefaultActionMapper;
import com.zutubi.pulse.core.api.PulseRuntimeException;
import com.zutubi.pulse.master.bootstrap.WebManager;
import com.zutubi.pulse.master.webwork.dispatcher.mapper.agents.AgentsActionResolver;
import com.zutubi.pulse.master.webwork.dispatcher.mapper.browse.BrowseActionResolver;
import com.zutubi.pulse.master.webwork.dispatcher.mapper.dashboard.MyBuildsActionResolver;
import com.zutubi.pulse.master.webwork.dispatcher.mapper.server.ServerActionResolver;
import com.zutubi.tove.type.record.PathUtils;
import com.zutubi.util.WebUtils;

import javax.servlet.http.HttpServletRequest;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 */
public class PulseActionMapper implements ActionMapper
{
    public static final String DASHBOARD_NAMESPACE = "/dashboard";
    public static final String MY_NAMESPACE = "/my";
    public static final String PREFERENCES_NAMESPACE = "/preferences";
    public static final String BROWSE_NAMESPACE    = "/browse";
    public static final String SERVER_NAMESPACE    = "/server";
    public static final String AGENTS_NAMESPACE    = "/agents";
    public static final String ADMIN_NAMESPACE     = "/admin";
    public static final String SETUP_NAMESPACE     = "/setup";

    private static final String PATH_MY_BUILDS = "my";
    private static final String PATH_MY_CHANGES = "changes";
    private static final String PATH_PREFERENCES = "preferences";
    private static final String PATH_MY_HOME = "home";

    private DefaultActionMapper delegate = new DefaultActionMapper();
    private ActionResolver browseActionResolver = new BrowseActionResolver();
    private ActionResolver serverActionResolver = new ServerActionResolver();
    private ActionResolver agentsActionResolver = new AgentsActionResolver();

    private WebManager webManager = null;

    public ActionMapping getMapping(HttpServletRequest request)
    {
        String namespace;
        String path;
        String fullPath = request.getServletPath();
        String pathInfo = request.getPathInfo();

        if (pathInfo != null)
        {
            fullPath = PathUtils.getPath(fullPath, pathInfo);
        }

        fullPath = PathUtils.normalisePath(fullPath);

        namespace = "/" + PathUtils.getElement(fullPath, 0);
        path = PathUtils.getSuffix(fullPath, 1);

        // Is this a request for one of the setup paths?
        if (namespace.equals(SETUP_NAMESPACE) && webManager.isMainDeployed())
        {
            return new ActionMapping("app", namespace, null, new HashMap());
        }

        ActionMapping mapping = null;
        if("/".equals(namespace))
        {
            mapping = new ActionMapping("default", namespace, null, new HashMap());
        }
        else if(DASHBOARD_NAMESPACE.equals(namespace))
        {
            // Urls in this space currently have no parameters, just the
            // action name.
            mapping = getDashboardMapping(getEncodedPath(request, namespace));
        }
        else if(MY_NAMESPACE.equals(namespace))
        {
            mapping = getResolverMapping(getEncodedPath(request, namespace), DASHBOARD_NAMESPACE, new MyBuildsActionResolver());
        }
        else if(PREFERENCES_NAMESPACE.equals(namespace))
        {
            mapping = new ActionMapping(PATH_PREFERENCES, DASHBOARD_NAMESPACE, null, Collections.emptyMap());
        }
        else if(BROWSE_NAMESPACE.equals(namespace))
        {
            mapping = getBrowseMapping(getEncodedPath(request, namespace));
        }
        else if(SERVER_NAMESPACE.equals(namespace))
        {
            mapping = getServerMapping(getEncodedPath(request, namespace));
        }
        else if(AGENTS_NAMESPACE.equals(namespace))
        {
            mapping = getAgentsMapping(getEncodedPath(request, namespace));
        }
        else if(ADMIN_NAMESPACE.equals(namespace))
        {
            mapping = getAdminMapping(path);
        }

        if(mapping == null)
        {
            mapping = delegate.getMapping(request);
        }

        return mapping;
    }

    private String getEncodedPath(HttpServletRequest request, String namespace)
    {
        String encoded = request.getRequestURI();
        try
        {
            // The URI may or may not have a scheme://host part.  Let the URI
            // class parse this for us.
            URI uri = new URI(encoded);
            encoded = uri.getRawPath();

            String context = request.getContextPath();
            if (context.length() > 0 && encoded.startsWith(context))
            {
                encoded = encoded.substring(context.length());
            }
            
            if (encoded.startsWith(namespace))
            {
                encoded = encoded.substring(namespace.length());
            }

            if (encoded.startsWith("/"))
            {
                encoded = encoded.substring(1);
            }

            return encoded;
        }
        catch (URISyntaxException e)
        {
            throw new PulseRuntimeException(e);
        }
    }

    private ActionMapping getDashboardMapping(String encodedPath)
    {
        encodedPath = normalise(encodedPath);
        if(encodedPath.startsWith(PATH_PREFERENCES))
        {
            // Legacy: /dashboard/preferences/<path> is a config view rooted at
            // users/<user>/preferences
            return new ActionMapping(encodedPath, DASHBOARD_NAMESPACE, null, Collections.emptyMap());
        }
        else if(encodedPath.startsWith(PATH_MY_CHANGES))
        {
            String[] elements = encodedPath.split("/");
            if(elements.length < 2)
            {
                return null;
            }

            Map<String, String> parameters = new HashMap<>(2);
            parameters.put("id", WebUtils.uriComponentDecode(elements[1]));
            if (elements.length > 2)
            {
                parameters.put("startPage", elements[2]);
            }
            return new ActionMapping("viewChangelist", "default", null, parameters);
        }
        else if(encodedPath.startsWith(PATH_MY_BUILDS))
        {
            return getResolverMapping(encodedPath.substring(2), DASHBOARD_NAMESPACE, new MyBuildsActionResolver());
        }
        else
        {
            if (encodedPath.length() == 0)
            {
                encodedPath = PATH_MY_HOME;
            }
            
            // All other dashboard paths are trivial action names.
            return new ActionMapping(encodedPath, DASHBOARD_NAMESPACE, null, null);
        }
    }

    private ActionMapping getBrowseMapping(String encodedPath)
    {
        // browse/                    - projects page
        //   <project>/               - project tabs
        //     home/                  - (default)
        //     reports/
        //     log/
        //     builds/                - (history)
        //       <build id>/          - build tabs
        //         summary/           - (default)
        //         log/               - build log.
        //         detailed/
        //           <stage>/         - select stage on detailed tab
        //             log/           - log for stage
        //         changes/
        //         tests/
        //         file/
        //         wc/
        return getResolverMapping(encodedPath, BROWSE_NAMESPACE, browseActionResolver);
    }

    private ActionMapping getServerMapping(String encodedPath)
    {
        return getResolverMapping(encodedPath, SERVER_NAMESPACE, serverActionResolver);
    }

    private ActionMapping getAgentsMapping(String encodedPath)
    {
        return getResolverMapping(encodedPath, AGENTS_NAMESPACE, agentsActionResolver);
    }

    private ActionMapping getAdminMapping(String path)
    {
        Map<String, String> params = Maps.newHashMap();
        params.put("path", path);
        return new ActionMapping("app", ADMIN_NAMESPACE, null, params);
    }

    private ActionMapping   getResolverMapping(String encodedPath, String namespace, ActionResolver resolver)
    {
        return resolve(namespace, normalise(encodedPath), resolver);
    }

    private String normalise(String path)
    {
        return path == null ? "" : PathUtils.normalisePath(path);
    }

    private ActionMapping resolve(String namespace, String encodedPath, ActionResolver actionResolver)
    {
        Map<String, String> parameters = new HashMap<>(actionResolver.getParameters());
        String[] elements = encodedPath.length() == 0 ? new String[0] : encodedPath.split("/");
        for(String element: elements)
        {
            element = WebUtils.uriComponentDecode(element);
            actionResolver = actionResolver.getChild(element);
            if(actionResolver == null)
            {
                break;
            }

            parameters.putAll(actionResolver.getParameters());
        }

        if(actionResolver == null || actionResolver.getAction() == null)
        {
            // No action found.
            return null;
        }

        return new ActionMapping(actionResolver.getAction(), namespace, null, parameters);
    }

    public String getUriFromActionMapping(ActionMapping mapping)
    {
        return delegate.getUriFromActionMapping(mapping);
    }

    public void setWebManager(WebManager webManager)
    {
        this.webManager = webManager;
    }

    public static void main(String argv[])
    {
        // Displays valid URLs for all namespaces except /admin.

        Map<String, Optional<ActionResolver>> baseResolvers = ImmutableMap.<String, Optional<ActionResolver>>builder()
                .put(DASHBOARD_NAMESPACE + "/" + PATH_MY_HOME + "/", Optional.<ActionResolver>absent())
                .put(MY_NAMESPACE, Optional.<ActionResolver>of(new MyBuildsActionResolver()))
                .put(PREFERENCES_NAMESPACE, Optional.<ActionResolver>absent())
                .put(BROWSE_NAMESPACE, Optional.<ActionResolver>of(new BrowseActionResolver()))
                .put(SERVER_NAMESPACE, Optional.<ActionResolver>of(new ServerActionResolver()))
                .put(AGENTS_NAMESPACE, Optional.<ActionResolver>of(new AgentsActionResolver()))
                .build();

        for (Map.Entry<String, Optional<ActionResolver>> entry: baseResolvers.entrySet())
        {
            if (entry.getValue().isPresent())
            {
                for (String url: UrlEnumerator.enumerate(entry.getValue().get()))
                {
                    System.out.println(entry.getKey() + "/" + url);
                }
            }
            else
            {
                System.out.println(entry.getKey());
            }
        }
    }
}
