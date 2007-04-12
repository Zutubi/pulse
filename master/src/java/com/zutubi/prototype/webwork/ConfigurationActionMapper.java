package com.zutubi.prototype.webwork;

import com.opensymphony.webwork.dispatcher.mapper.ActionMapper;
import com.opensymphony.webwork.dispatcher.mapper.ActionMapping;
import com.opensymphony.webwork.dispatcher.mapper.DefaultActionMapper;
import com.zutubi.prototype.type.record.PathUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.HashMap;

/**
 */
public class ConfigurationActionMapper implements ActionMapper
{
    static final String NAMESPACE = "/prototype";
    
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
                    Map<String, String> params = new HashMap<String, String>(1);
                    path = elements.length > 1 ? PathUtils.getPath(1, elements) : "";
                    params.put("path", path);
                    return new ActionMapping(elements[0], NAMESPACE, null, params);
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
