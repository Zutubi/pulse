package com.zutubi.pulse.servercore.xmlrpc;

import org.apache.xmlrpc.XmlRpcServer;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;

public class XmlRpcServlet extends HttpServlet
{
    protected XmlRpcServer xmlrpc = null;

    public void init(ServletConfig config) throws ServletException
    {
        super.init(config);
        xmlrpc = new PulseXmlRpcServer();
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
