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

package com.zutubi.pulse.master.tove.mapper;

import com.opensymphony.module.sitemesh.Config;
import com.opensymphony.module.sitemesh.Decorator;
import com.opensymphony.module.sitemesh.DecoratorMapper;
import com.opensymphony.module.sitemesh.Page;
import com.opensymphony.module.sitemesh.mapper.AbstractDecoratorMapper;
import com.opensymphony.module.sitemesh.mapper.ConfigLoader;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import java.util.Properties;

/**
 *
 *
 */
public class ConfigDecoratorMapper extends AbstractDecoratorMapper
{
    private ConfigLoader configLoader = null;

    private String servletPath = null;

    /**
     * Create new ConfigLoader using '/WEB-INF/decorators.xml' file.
     */
    public void init(Config config, Properties properties, DecoratorMapper parent) throws InstantiationException
    {
        super.init(config, properties, parent);
        try
        {
            String fileName = properties.getProperty("config", "/WEB-INF/decorators.xml");
            configLoader = new ConfigLoader(fileName, config);
            servletPath = properties.getProperty("servlet-path", "/config");
        }
        catch (Exception e)
        {
            throw new InstantiationException(e.toString());
        }
    }

    /**
     * Retrieve {@link com.opensymphony.module.sitemesh.Decorator} based on 'pattern' tag.
     */
    public Decorator getDecorator(HttpServletRequest request, Page page)
    {
        String thisPath = request.getServletPath();

        // getServletPath() returns null unless the mapping corresponds to a servlet
        if (thisPath == null)
        {
            String requestURI = request.getRequestURI();
            if (request.getPathInfo() != null)
            {
                // strip the pathInfo from the requestURI
                thisPath = requestURI.substring(0, requestURI.indexOf(request.getPathInfo()));
            }
            else
            {
                thisPath = requestURI;
            }
        }

        if (thisPath.equals(servletPath))
        {
            thisPath = request.getRequestURI();
        }

        String name;
        try
        {
            name = configLoader.getMappedName(thisPath);
        }
        catch (ServletException e)
        {
            throw new RuntimeException(e);
        }
        Decorator result = getNamedDecorator(request, name);
        return result == null ? super.getDecorator(request, page) : result;
    }

    /**
     * Retrieve Decorator named in 'name' attribute. Checks the role if specified.
     */
    public Decorator getNamedDecorator(HttpServletRequest request, String name)
    {
        Decorator result;
        try
        {
            result = configLoader.getDecoratorByName(name);
        }
        catch (ServletException e)
        {
            throw new RuntimeException(e);
        }

        if (result == null || (result.getRole() != null && !request.isUserInRole(result.getRole())))
        {
            // if the result is null or the user is not in the role
            return super.getNamedDecorator(request, name);
        }
        else
        {
            return result;
        }
    }
}
