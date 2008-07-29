package com.zutubi.pulse.webwork.mapping;

import com.opensymphony.webwork.dispatcher.mapper.ActionMapper;
import com.opensymphony.webwork.dispatcher.mapper.ActionMapping;
import com.opensymphony.webwork.dispatcher.mapper.DefaultActionMapper;
import com.opensymphony.xwork.ActionProxyFactory;
import com.zutubi.pulse.webwork.mapping.agents.AgentsActionResolver;
import com.zutubi.pulse.webwork.mapping.browse.BrowseActionResolver;
import com.zutubi.pulse.webwork.mapping.dashboard.MyBuildsActionResolver;
import com.zutubi.pulse.webwork.mapping.server.ServerActionResolver;
import com.zutubi.tove.type.record.PathUtils;
import com.zutubi.util.TextUtils;

import javax.servlet.http.HttpServletRequest;
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

    private DefaultActionMapper delegate = new DefaultActionMapper();
    private ActionResolver browseActionResolver = new BrowseActionResolver();
    private ActionResolver serverActionResolver = new ServerActionResolver();
    private ActionResolver agentsActionResolver = new AgentsActionResolver();

    // Pure config namespaces only, no hybrids here!
    private static final Set<String> configNamespaces = new HashSet<String>();
    {
        configNamespaces.add("/setupconfig");
        configNamespaces.add("/aconfig");
        configNamespaces.add("/ahelp");
        configNamespaces.add("/atemplate");
    }

    public ActionMapping getMapping(HttpServletRequest request)
    {
        String namespace = request.getServletPath();
        String path = request.getPathInfo();
        if(path != null)
        {
            path = PathUtils.normalizePath(path);
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
            mapping = getDashboardMapping(path, request);
        }
        else if(BROWSE_NAMESPACE.equals(namespace))
        {
            mapping = getBrowseMapping(path);
        }
        else if(SERVER_NAMESPACE.equals(namespace))
        {
            mapping = getServerMapping(path);
        }
        else if(AGENTS_NAMESPACE.equals(namespace))
        {
            mapping = getAgentsMapping(path);
        }
        else if(ADMIN_NAMESPACE.equals(namespace))
        {
            mapping = getAdminMapping(path, request);
        }

        if(mapping == null)
        {
            mapping = delegate.getMapping(request);
        }

        return mapping;
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
            String[] actionSubmit = getActionSubmit(query, "display");
            if(params == null)
            {
                params = new HashMap<String, String>();
            }

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

    private String[] getActionSubmit(String query, String defaultAction)
    {
        String[] actionSubmit = new String[]{defaultAction, ""};
        if (query != null)
        {
            // Strip parameter which is automatically added by Ext in some
            // cases.
            query = query.replaceAll("_dc=[0-9]+", "");
        }
        
        if (TextUtils.stringSet(query))
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

    private ActionMapping getDashboardMapping(String path, HttpServletRequest request)
    {
        if(path.startsWith("preferences"))
        {
            // /dashboard/preferences/<path> is a config view rooted at
            // users/<user>/preferences
            Map<String, String> parameters = new HashMap<String, String>();
            parameters.put("prefixPath", "users/${principle}");
            parameters.put("section", "dashboard");
            parameters.put("tab", "preferences");
            return getConfigMapping(ADMIN_NAMESPACE, path, request.getQueryString(), parameters);
        }
        else if(path.startsWith("changes"))
        {
            String[] elements = path.split("/");
            if(elements.length != 2)
            {
                return null;
            }

            Map<String, String> parameters = new HashMap<String, String>(1);
            parameters.put("id", elements[1]);
            return new ActionMapping("viewChangelist", "default", null, parameters);
        }
        else if(path.startsWith("my"))
        {
            return getResolverMapping(path.substring(2), DASHBOARD_NAMESPACE, new MyBuildsActionResolver());
        }
        else
        {
            if (path.length() == 0)
            {
                path = "home";
            }
            
            // All other dashboard paths are trivial action names.
            return new ActionMapping(path, DASHBOARD_NAMESPACE, null, null);
        }
    }

    private ActionMapping getBrowseMapping(String path)
    {
        // browse/                    - projects page
        //   <project>/               - project tabs
        //     home/                  - (default)
        //     reports/
        //     builds/                - (history)
        //       <build id>/          - build tabs
        //         summary/           - (default)
        //         detailed/
        //           <stage>/         - select stage on detailed tab
        //             log/           - log for stage
        //         changes/
        //         tests/
        //         file/
        //         wc/
        return getResolverMapping(path, BROWSE_NAMESPACE, browseActionResolver);
    }

    private ActionMapping getServerMapping(String path)
    {
        return getResolverMapping(path, SERVER_NAMESPACE, serverActionResolver);
    }

    private ActionMapping getAgentsMapping(String path)
    {
        return getResolverMapping(path, AGENTS_NAMESPACE, agentsActionResolver);
    }

    private ActionMapping getResolverMapping(String path, String namespace, ActionResolver resolver)
    {
        path = path == null ? "" : PathUtils.normalizePath(path);
        return resolve(namespace, path, resolver);
    }

    private ActionMapping resolve(String namespace, String path, ActionResolver actionResolver)
    {
        Map<String, String> parameters = new HashMap<String, String>(actionResolver.getParameters());
        String[] elements = path.length() == 0 ? new String[0] : path.split("/");
        for(String element: elements)
        {
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

    private ActionMapping getAdminMapping(String path, HttpServletRequest request)
    {
        if(path.startsWith("actions"))
        {
            // /admin/actions?action=method takes your to:
            //   <action>!<method>
            if(TextUtils.stringSet(request.getQueryString()))
            {
                String[] pieces = request.getQueryString().split("=", 2);
                return new ActionMapping(pieces[0], ADMIN_NAMESPACE, pieces.length > 1 ? pieces[1] : null, null);
            }

            return null;
        }
        else if(path.startsWith("plugins"))
        {
            // /admin/plugins?<action>=<method> takes you to:
            //   <action>Plugin.action!<method>
            if(TextUtils.stringSet(request.getQueryString()))
            {
                String[] pieces = request.getQueryString().split("=", 2);
                return new ActionMapping(pieces[0] + "Plugin", ADMIN_NAMESPACE, pieces.length > 1 ? pieces[1] : null, null);
            }

            // /admin/plugins/<id> takes you to the plugins view, selecting a
            // plugin with the given id if any is specified.
            Map<String, String> params = new HashMap<String, String>();
            String[] pathElements = PathUtils.getPathElements(path);
            if(pathElements.length > 1)
            {
                params.put("path", PathUtils.getPath(1, pathElements));
            }

            return new ActionMapping(pathElements[0], ADMIN_NAMESPACE, null, params);
        }
        else
        {
            // All other admin paths are config views.
            return getConfigMapping(ADMIN_NAMESPACE, path, request.getQueryString());
        }
    }

    public String getUriFromActionMapping(ActionMapping mapping)
    {
        return delegate.getUriFromActionMapping(mapping);
    }
}
