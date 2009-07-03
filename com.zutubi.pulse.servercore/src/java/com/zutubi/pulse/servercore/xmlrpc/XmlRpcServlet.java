package com.zutubi.pulse.servercore.xmlrpc;

import com.zutubi.util.logging.Logger;
import com.zutubi.util.reflection.ReflectionUtils;
import org.apache.xmlrpc.XmlRpcHandlerMapping;
import org.apache.xmlrpc.XmlRpcServer;
import org.apache.xmlrpc.XmlRpcWorker;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;

public class XmlRpcServlet extends HttpServlet
{
    private static final Logger LOG = Logger.getLogger(XmlRpcServlet.class);

    protected XmlRpcServer xmlrpc = null;

    public void init(ServletConfig config) throws ServletException
    {
        super.init(config);
        xmlrpc = new XmlRpcServer()
        {
            protected XmlRpcWorker createWorker()
            {
                XmlRpcWorker worker = super.createWorker();
                try
                {
                    // extract the handler mapping - stupid i know, but there are limited options.
                    XmlRpcHandlerMapping handlerMapping = (XmlRpcHandlerMapping) ReflectionUtils.getFieldValue(worker, "handlerMapping");
                    return new PulseXmlRpcWorker(handlerMapping);
                }
                catch (Exception e)
                {
                    LOG.warning(e);
                    return worker;
                }
            }
        };
    }

    public void destroy()
    {
        super.destroy();
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        byte[] result = xmlrpc.execute(request.getInputStream());
        response.setContentType("text/xml");
        response.setContentLength(result.length);
        OutputStream os = response.getOutputStream();
        os.write(result);
        os.flush();
        os.close();
    }
}
