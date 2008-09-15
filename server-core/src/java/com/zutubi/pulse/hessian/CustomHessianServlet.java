package com.zutubi.pulse.hessian;

import com.caucho.hessian.io.HessianInput;
import com.caucho.hessian.io.HessianOutput;
import com.caucho.hessian.io.SerializerFactory;
import com.caucho.hessian.server.HessianSkeleton;
import com.zutubi.pulse.spring.SpringComponentContext;
import com.zutubi.util.bean.ObjectFactory;
import com.zutubi.util.logging.Logger;
import com.zutubi.pulse.spring.SpringObjectFactory;
import com.zutubi.pulse.plugins.PluginManager;

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
    private ObjectFactory objectFactory = new SpringObjectFactory();
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
            Class serviceClass = objectFactory.getClassInstance(serviceName);
            skeleton = new HessianSkeleton(objectFactory.buildBean(implName), serviceClass);
        }
        catch (Exception e)
        {
            ServletException se = new ServletException(e.getMessage());
            se.initCause(e);
            throw se;
        }

        factory = new SerializerFactory();
        factory.addFactory(getSerialiserFactory());
    }

    public void setCustomSerialiserFactory(CustomSerialiserFactory serialiserFactory)
    {
        this.customSerialiserFactory = serialiserFactory;
    }

    public CustomSerialiserFactory getSerialiserFactory()
    {
        if (customSerialiserFactory == null)
        {
            // TODO: when we get autowiring of servlets sorted out, we can remove this call to the ComponentContext.
            customSerialiserFactory = (CustomSerialiserFactory) SpringComponentContext.getBean("customSerialiserFactory");
        }
        return customSerialiserFactory;
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
            CustomHessianClassLoader customClassLoader = new CustomHessianClassLoader(originalClassLoader);
            customClassLoader.setRegistry((HessianConfigurationExtensionManager) SpringComponentContext.getBean("hessianExtensionManager"));
            customClassLoader.setPluginManager((PluginManager) SpringComponentContext.getBean("pluginManager"));
            
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
