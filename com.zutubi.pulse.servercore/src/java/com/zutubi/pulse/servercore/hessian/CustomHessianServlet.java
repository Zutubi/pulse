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

package com.zutubi.pulse.servercore.hessian;

import com.caucho.hessian.io.HessianInput;
import com.caucho.hessian.io.HessianOutput;
import com.caucho.hessian.io.SerializerFactory;
import com.caucho.hessian.server.HessianSkeleton;
import com.zutubi.util.bean.ObjectFactory;
import com.zutubi.util.logging.Logger;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * A custom, cut down version of the HessainServlet.  Does the basics, and
 * (most importantly) uses the CustomSerialiserFactory (I could not *quite*
 * get this working with HessianServlet itself).
 */
public class CustomHessianServlet extends GenericServlet
{
    private static final Logger LOG = Logger.getLogger(CustomHessianServlet.class);

    HessianSkeleton skeleton;
    SerializerFactory factory;
    
    private ObjectFactory objectFactory;
    private CustomSerialiserFactory customSerialiserFactory;

    @Override
    public void init(ServletConfig config) throws ServletException
    {
        super.init(config);

        String serviceName = getInitParameter("home-api");
        String implName = getInitParameter("home-class");

        if (serviceName == null)
        {
            throw new ServletException("Missing required parameter 'home-api'");
        }

        if (implName == null)
        {
            throw new ServletException("Missing required parameter 'home-class'");
        }

        try
        {
            Class<?> serviceClass = objectFactory.getClassInstance(serviceName, Object.class);
            skeleton = new HessianSkeleton(objectFactory.buildBean(implName, Object.class), serviceClass);
        }
        catch (Exception e)
        {
            ServletException se = new ServletException(e.getMessage());
            se.initCause(e);
            throw se;
        }

        factory = new SerializerFactory();
        factory.addFactory(customSerialiserFactory);
    }

    public void setObjectFactory(ObjectFactory objectFactory)
    {
        this.objectFactory = objectFactory;
    }

    public void setCustomSerialiserFactory(CustomSerialiserFactory serialiserFactory)
    {
        this.customSerialiserFactory = serialiserFactory;
    }

    public void service(ServletRequest servletRequest, ServletResponse servletResponse) throws ServletException, IOException
    {
        HttpServletRequest req = (HttpServletRequest) servletRequest;
        HttpServletResponse res = (HttpServletResponse) servletResponse;

        if (!req.getMethod().equals("POST"))
        {
            res.sendError(500, "Hessian Requires POST");
            return;
        }

        ClassLoader originalClassLoader = Thread.currentThread().getContextClassLoader();
        try
        {
            CustomHessianClassLoader customClassLoader = objectFactory.buildBean(CustomHessianClassLoader.class, originalClassLoader);
            
            Thread.currentThread().setContextClassLoader(customClassLoader);

            HessianInput in = new HessianInput();
            HessianOutput out = new HessianOutput();
            in.setSerializerFactory(factory);
            in.init(req.getInputStream());
            out.setSerializerFactory(factory);
            out.init(res.getOutputStream());

            skeleton.invoke(in, out);
        }
        catch (RuntimeException e)
        {
            LOG.severe(e);
            throw e;
        }
        catch (ServletException e)
        {
            LOG.severe(e);
            throw e;
        }
        catch (Throwable e)
        {
            LOG.severe(e);
            ServletException se = new ServletException(e.getMessage());
            se.initCause(e);
            throw se;
        }
        finally
        {
            Thread.currentThread().setContextClassLoader(originalClassLoader);
        }
    }
}
