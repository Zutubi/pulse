package com.zutubi.pulse.slave.xmlrpc;

import com.zutubi.pulse.servercore.xmlrpc.XmlRpcServlet;
import com.zutubi.pulse.slave.api.RemoteApi;
import com.zutubi.pulse.core.spring.SpringComponentContext;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;

/**
 */
public class SlaveXmlRpcServlet extends XmlRpcServlet
{
    public void init(ServletConfig config) throws ServletException
    {
        super.init(config);

        RemoteApi remoteApi = new RemoteApi();
        SpringComponentContext.autowire(remoteApi);

        xmlrpc.addHandler("RemoteApi", remoteApi);
    }
}
