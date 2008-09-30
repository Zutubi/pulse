package com.zutubi.pulse.slave.xmlrpc;

import com.zutubi.pulse.xmlrpc.XmlRpcServlet;
import com.zutubi.pulse.slave.api.RemoteApi;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;

/**
 */
public class SlaveXmlRpcServlet extends XmlRpcServlet
{
    public void init(ServletConfig config) throws ServletException
    {
        super.init(config);
        xmlrpc.addHandler("RemoteApi", new RemoteApi());
    }
}
