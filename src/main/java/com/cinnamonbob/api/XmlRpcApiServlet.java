package com.cinnamonbob.api;

import org.apache.xmlrpc.XmlRpcServer;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;
import java.io.IOException;
import java.io.OutputStream;

/**
 *
 *
 */
public class XmlRpcApiServlet extends HttpServlet
{
    XmlRpcServer server = null;

    public void init() throws ServletException
    {
        server = new XmlRpcServer();
        server.addHandler("$default", new XmlRpcApiHandler());
    }

    /**
     * Handle the incoming request.
     *
     * @param request
     * @param response
     */
    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException
    {
        byte[] result = server.execute(request.getInputStream());

        response.setContentType("text/xml");
        response.setContentLength(result.length);

        OutputStream out = response.getOutputStream();
        out.write(result);
        out.flush();
    }


}
