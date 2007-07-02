package com.zutubi.prototype.webwork;

import com.opensymphony.util.TextUtils;
import com.opensymphony.webwork.dispatcher.mapper.ActionMapper;
import com.opensymphony.webwork.dispatcher.mapper.ActionMapping;
import com.opensymphony.webwork.dispatcher.mapper.DefaultActionMapper;
import com.opensymphony.xwork.ActionProxyFactory;
import com.zutubi.prototype.config.ConfigurationTemplateManager;
import com.zutubi.prototype.type.record.PathUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.HashSet;
import java.util.Set;

/**
 */
public class ConfigurationActionMapper implements ActionMapper
{
    public static final String CONFIG_NAMESPACE = "/admin";
    static final String AJAX_CONFIG_NAMESPACE = "/aconfig";
    static final String AJAX_TEMPLATE_NAMESPACE = "/atemplate";

    static final Set<String> configActions = new HashSet<String>();
    static final Set<String> templateActions = new HashSet<String>();

    static
    {
        configActions.add("display");
        configActions.add("index");
        configActions.add("save");
        configActions.add("delete");
        configActions.add("wizard");
        configActions.add("ls");
        configActions.add("check");

        templateActions.add("display");
        templateActions.add("ls");
    }

    private ConfigurationTemplateManager configurationTemplateManager;

    private DefaultActionMapper delegate = new DefaultActionMapper();

    private static final List<String> configNamespaces = new LinkedList<String>();
    {
        configNamespaces.add("/setup");
        configNamespaces.add("/admin");
        configNamespaces.add("/aconfig");
        configNamespaces.add("/atemplate");
    }

    public ActionMapping getMapping(HttpServletRequest request)
    {
        String servletPath = request.getServletPath();

        String namespace = getConfigNamespace(servletPath);
        if (namespace != null)
        {
            // apply custom mapping.
            // a) extract path and check that it is valid. If not valid, use default.
            String path = extractPath(servletPath, namespace);
            path = PathUtils.normalizePath(path);

            boolean pathExists = isValidPath(path);
            if (pathExists)
            {
                // are we dealing with a templated path (projects / agents) or a standard config path (global).

                String[] actionSubmit = getActionSubmit(request, "template");
                String requestedAction = actionSubmit[0];
                String submitField = actionSubmit[1];

                Map<String, String> params = new HashMap<String, String>();
                params.put("submitField", submitField);
                params.put("action", requestedAction);
                params.put("path", path);

                try
                {
                    ActionProxyFactory.getFactory().createActionProxy(namespace, requestedAction, params);
                    return new ActionMapping(requestedAction, namespace, null, params);
                }
                catch (Exception e)
                {
                    // noop, action not mapped, go to generic action handling
                    return new ActionMapping("generic", namespace, null, params);
                }
            }
        }

        // get mapping using previous implementation.
        return getOldMapping(request);
    }

    private boolean isValidPath(String path)
    {
        try
        {
            return (configurationTemplateManager.getType(path) != null); // is this a valid assumption/check to make?
        }
        catch (IllegalArgumentException e)
        {
            return false;
        }
    }

    private String getConfigNamespace(String servletPath)
    {
        for (String namespace : configNamespaces)
        {
            if (servletPath.startsWith(namespace))
            {
                return namespace;
            }
        }
        return null;
    }

    private String extractPath(String servletPath, String namespace)
    {
        String path = servletPath.substring(namespace.length());
        int index = path.indexOf(".action");
        if (index != -1)
        {
            path = path.substring(0, index);
        }
        return path;
    }

    private ActionMapping getOldMapping(HttpServletRequest request)
    {
        String servletPath = request.getServletPath();
        if (CONFIG_NAMESPACE.equals(servletPath) || AJAX_CONFIG_NAMESPACE.equals(servletPath) || AJAX_TEMPLATE_NAMESPACE.equals(servletPath))
        {
            boolean isTemplate = AJAX_TEMPLATE_NAMESPACE.equals(servletPath);
            String path = request.getPathInfo();
            if (path != null)
            {
                String[] elements = PathUtils.getPathElements(path);
                String[] actionSubmit = getActionSubmit(request, (isTemplate || elements.length > 0) ? "display" : "index");

                Map<String, String> params = new HashMap<String, String>();
                params.put("submitField", actionSubmit[1]);
                path = PathUtils.normalizePath(path);
                if (TextUtils.stringSet(path))
                {
                    params.put("path", path);
                }
                String requestedAction = actionSubmit[0];
                Set<String> builtinActions = isTemplate ? templateActions : configActions;
                if (builtinActions.contains(requestedAction))
                {
                    return new ActionMapping(requestedAction, servletPath, null, params);
                }
                params.put("action", requestedAction);
                return new ActionMapping("generic", servletPath, null, params);
            }
        }

        return delegate.getMapping(request);
    }

    private String[] getActionSubmit(HttpServletRequest request, String defaultAction)
    {
        String[] actionSubmit = new String[]{defaultAction, ""};
        String query = request.getQueryString();
        if (TextUtils.stringSet(query))
        {
            int index = query.indexOf('=');
            if (index > 0)
            {
                actionSubmit[0] = query.substring(0, index);
                actionSubmit[1] = query.substring(index + 1);
            }
            else
            {
                actionSubmit[0] = query;
            }
        }
        return actionSubmit;
    }

    public String getUriFromActionMapping(ActionMapping mapping)
    {
        return delegate.getUriFromActionMapping(mapping);
    }

    public void setConfigurationTemplateManager(ConfigurationTemplateManager configurationTemplateManager)
    {
        this.configurationTemplateManager = configurationTemplateManager;
    }
}
