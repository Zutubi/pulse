package com.zutubi.prototype.webwork;

import com.opensymphony.util.TextUtils;
import com.opensymphony.webwork.dispatcher.mapper.ActionMapper;
import com.opensymphony.webwork.dispatcher.mapper.ActionMapping;
import com.opensymphony.webwork.dispatcher.mapper.DefaultActionMapper;
import com.zutubi.prototype.type.record.PathUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;

/**
 */
public class ConfigurationActionMapper implements ActionMapper
{
    static final String CONFIG_NAMESPACE = "/config";
    static final String AJAX_CONFIG_NAMESPACE = "/aconfig";

    static final Set<String> builtinActions = new HashSet<String>();
    static
    {
        builtinActions.add("display");
        builtinActions.add("index");
        builtinActions.add("save");
        builtinActions.add("delete");
        builtinActions.add("wizard");
    }

    private DefaultActionMapper delegate = new DefaultActionMapper();

    public ActionMapping getMapping(HttpServletRequest request)
    {
        String servletPath = request.getServletPath();
        if (CONFIG_NAMESPACE.equals(servletPath) || AJAX_CONFIG_NAMESPACE.equals(servletPath))
        {
            String path = request.getPathInfo();
            if (path != null)
            {
                String[] elements = PathUtils.getPathElements(path);
                String[] actionSubmit = getActionSubmit(request, elements.length > 0 ? "display" : "index");

                Map<String, String> params = new HashMap<String, String>();
                params.put("submitField", actionSubmit[1]);
                path = PathUtils.normalizePath(path);
                if (TextUtils.stringSet(path))
                {
                    params.put("path", path);
                }
                String requestedAction = actionSubmit[0];
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
}
