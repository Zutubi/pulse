package com.zutubi.pulse.master.webwork.dispatcher.mapper;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.opensymphony.webwork.dispatcher.mapper.ActionMapper;
import com.opensymphony.webwork.dispatcher.mapper.ActionMapping;
import com.opensymphony.webwork.dispatcher.mapper.DefaultActionMapper;
import com.opensymphony.xwork.ActionProxyFactory;
import com.zutubi.pulse.core.api.PulseRuntimeException;
import com.zutubi.pulse.master.bootstrap.WebManager;
import com.zutubi.pulse.master.webwork.dispatcher.mapper.agents.AgentsActionResolver;
import com.zutubi.pulse.master.webwork.dispatcher.mapper.browse.BrowseActionResolver;
import com.zutubi.pulse.master.webwork.dispatcher.mapper.dashboard.MyBuildsActionResolver;
import com.zutubi.pulse.master.webwork.dispatcher.mapper.server.ServerActionResolver;
import com.zutubi.tove.type.record.PathUtils;
import com.zutubi.util.StringUtils;
import com.zutubi.util.WebUtils;

import javax.servlet.http.HttpServletRequest;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 */
public class PulseActionMapper implements ActionMapper
{
    public static final String DASHBOARD_NAMESPACE = "/dashboard";
    public static final String BROWSE_NAMESPACE    = "/browse";
    public static final String SERVER_NAMESPACE    = "/server";
    public static final String AGENTS_NAMESPACE    = "/agents";
    public static final String ADMIN_NAMESPACE     = "/admin";

    private static final String PATH_MY_BUILDS = "my";
    private static final String PATH_MY_CHANGES = "changes";
    private static final String PATH_PREFERENCES = "preferences";
    private static final String PATH_MY_HOME = "home";

    private DefaultActionMapper delegate = new DefaultActionMapper();
    private ActionResolver browseActionResolver = new BrowseActionResolver();
    private ActionResolver serverActionResolver = new ServerActionResolver();
    private ActionResolver agentsActionResolver = new AgentsActionResolver();

    // Pure config namespaces only, no hybrids here!
    private static final Set<String> configNamespaces = new HashSet<>();
    static
    {
        configNamespaces.add("/setupconfig");
        configNamespaces.add("/ajax/config");
        configNamespaces.add("/ajax/help");
        configNamespaces.add("/ajax/template");
    }

    // Setup paths are only available during the setup.
    private static final Set<String> setupNamespaces = new HashSet<>();
    static
    {
        setupNamespaces.add("migrate");
        setupNamespaces.add("restore");
        setupNamespaces.add("setup");
        setupNamespaces.add("upgrade");
    }

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
        if (isSetupRequest(request) && webManager.isMainDeployed())
        {
            return new ActionMapping("starting", "/startup", null, new HashMap());
        }

        ActionMapping mapping = null;
        if (configNamespaces.contains(namespace))
        {
            mapping = getConfigMapping(namespace, path, request.getQueryString());
        }
        else if("/".equals(namespace))
        {
            mapping = new ActionMapping("default", namespace, null, new HashMap());
        }
        else if(DASHBOARD_NAMESPACE.equals(namespace))
        {
            // Urls in this space currently have no parameters, just the
            // action name.
            mapping = getDashboardMapping(getEncodedPath(request, namespace), request);
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
            Map<String, String> params = Maps.newHashMap();
            params.put("path", path);
            mapping = new ActionMapping("app", ADMIN_NAMESPACE, null, params);
        }

        if(mapping == null)
        {
            mapping = delegate.getMapping(request);
        }

        return mapping;
    }

    private boolean isSetupRequest(HttpServletRequest request)
    {
        String[] pathElements = PathUtils.getPathElements(request.getServletPath());
        return pathElements.length > 0 && setupNamespaces.contains(pathElements[0]);
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

    private ActionMapping getConfigMapping(String namespace, String path, String query)
    {
        return getConfigMapping(namespace, path, query, null);
    }
    
    private ActionMapping getConfigMapping(String namespace, String path, String query, Map<String, String> params)
    {
        ActionMapping mapping = null;
        if (path != null)
        {
            if(params == null)
            {
                params = new HashMap<>();
            }

            if (query != null)
            {
                // Strip parameter which is automatically added by Ext in some
                // cases.
                query = stripRandomParam(query);
                query = collectExtraParameters(query, params);
            }
            
            String[] actionSubmit = getActionSubmit(query, "display");
            String requestedAction = actionSubmit[0];
            params.put("submitField", actionSubmit[1]);
            params.put("actionName", requestedAction);
            params.put("path", path);
            try
            {
                ActionProxyFactory.getFactory().createActionProxy(namespace, requestedAction, params);
                mapping = new ActionMapping(requestedAction, namespace, null, params);
            }
            catch (Exception e)
            {
                // noop, action not mapped, go to generic action handling
                mapping = new ActionMapping("generic", namespace, null, params);
            }
        }

        return mapping;
    }

    private String stripRandomParam(String query)
    {
        return query.replaceAll("&?_dc=[0-9]+", "");
    }

    private String collectExtraParameters(String query, Map<String, String> params)
    {
        String[] queryElements = query.split("&");
        if (queryElements.length > 1)
        {
            query = queryElements[0];
            for (int i = 1; i < queryElements.length; i++)
            {
                String element = queryElements[i];
                int index = element.indexOf('=');
                if (index > 0)
                {
                    params.put(element.substring(0, index), element.substring(index + 1));
                }
            }
        }
        return query;
    }

    private String[] getActionSubmit(String query, String defaultAction)
    {
        String[] actionSubmit = new String[]{defaultAction, ""};
        if (StringUtils.stringSet(query))
        {
            int index = query.indexOf('=');
            if (index > 0)
            {
                actionSubmit[0] = query.substring(0, index);
                actionSubmit[1] = query.substring(index + 1);

                index = actionSubmit[1].indexOf('&');
                if(index >= 0)
                {
                    actionSubmit[1] = actionSubmit[1].substring(0, index);
                }
            }
            else
            {
                actionSubmit[0] = query;
            }
        }
        return actionSubmit;
    }

    private ActionMapping getDashboardMapping(String encodedPath, HttpServletRequest request)
    {
        encodedPath = normalise(encodedPath);
        if(encodedPath.startsWith(PATH_PREFERENCES))
        {
            // /dashboard/preferences/<path> is a config view rooted at
            // users/<user>/preferences
            Map<String, String> parameters = new HashMap<>();
            parameters.put("prefixPath", "users/${principle}");
            parameters.put("section", "dashboard");
            parameters.put("tab", PATH_PREFERENCES);
            return getConfigMapping(ADMIN_NAMESPACE, WebUtils.uriPathDecode(encodedPath), request.getQueryString(), parameters);
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

    private ActionMapping getResolverMapping(String encodedPath, String namespace, ActionResolver resolver)
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
                .put(DASHBOARD_NAMESPACE + "/" + PATH_MY_BUILDS, Optional.<ActionResolver>of(new MyBuildsActionResolver()))
                .put(DASHBOARD_NAMESPACE + "/" + PATH_PREFERENCES + "/", Optional.<ActionResolver>absent())
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
