package com.cinnamonbob.hessian;

import com.caucho.hessian.io.HessianInput;
import com.caucho.hessian.io.HessianOutput;
import com.caucho.hessian.io.SerializerFactory;
import com.caucho.hessian.server.HessianSkeleton;

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
    HessianSkeleton skeleton;
    SerializerFactory factory;

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
            Class serviceClass = Class.forName(serviceName);
            Class implClass = Class.forName(implName);

            skeleton = new HessianSkeleton(implClass.newInstance(), serviceClass);
        }
        catch (Exception e)
        {
            ServletException se = new ServletException(e.getMessage());
            se.initCause(e);
            throw se;
        }

        factory = new SerializerFactory();
        factory.addFactory(new CustomSerialiserFactory());
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

        try
        {
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
            throw e;
        }
        catch (ServletException e)
        {
            throw e;
        }
        catch (Throwable e)
        {
            ServletException se = new ServletException(e.getMessage());
            se.initCause(e);
            throw se;
        }
    }
}
