package com.zutubi.prototype.webwork;

import com.opensymphony.webwork.dispatcher.mapper.ActionMapper;
import com.opensymphony.webwork.dispatcher.mapper.ActionMapping;
import com.opensymphony.webwork.dispatcher.mapper.DefaultActionMapper;
import com.opensymphony.util.TextUtils;
import com.zutubi.prototype.type.record.PathUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.HashMap;

/**
 */
public class ConfigurationActionMapper implements ActionMapper
{
    static final String NAMESPACE = "/config";
    
    private DefaultActionMapper delegate = new DefaultActionMapper();

    public ActionMapping getMapping(HttpServletRequest request)
    {
        if (NAMESPACE.equals(request.getServletPath()))
        {
            String path = request.getPathInfo();
            if (path != null)
            {
                String[] elements = PathUtils.getPathElements(path);
                if(elements.length > 0)
                {
                    String action = "display";
                    String submitField = "";
                    String query = request.getQueryString();
                    if(TextUtils.stringSet(query))
                    {
                        int index = query.indexOf('=');
                        if(index > 0)
                        {
                            action = query.substring(0, index);
                            submitField = query.substring(index + 1);
                        }
                        else
                        {
                            action = query;
                        }
                    }

                    Map<String, String> params = new HashMap<String, String>();
                    params.put("submitField", submitField);
                    path = PathUtils.normalizePath(path);
                    if(TextUtils.stringSet(path))
                    {
                        params.put("path", path);
                    }
                    return new ActionMapping(action, NAMESPACE, null, params);
                }
                else
                {
                    return new ActionMapping("index", NAMESPACE, null, null);
                }
            }
        }

        return delegate.getMapping(request);
    }

    public String getUriFromActionMapping(ActionMapping mapping)
    {
        return delegate.getUriFromActionMapping(mapping);
    }
}
