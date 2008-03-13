package com.zutubi.pulse.xmlrpc;

import org.apache.xmlrpc.XmlRpcServer;
import org.apache.xmlrpc.XmlRpcWorker;
import org.apache.xmlrpc.XmlRpcHandlerMapping;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.OutputStream;

import com.zutubi.util.ObjectUtils;

public class XmlRpcServlet extends HttpServlet
{
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
                    XmlRpcHandlerMapping handlerMapping = (XmlRpcHandlerMapping) ObjectUtils.getField("handlerMapping", worker);
                    return new LoggingXmlRpcWorker(handlerMapping);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
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
