package com.zutubi.pulse.master.xmlrpc;

import com.zutubi.pulse.master.api.RemoteApi;
import com.zutubi.pulse.servercore.xmlrpc.XmlRpcServlet;
import com.zutubi.pulse.core.spring.SpringComponentContext;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;

/**
 */
public class MasterXmlRpcServlet extends XmlRpcServlet
{
    public void init(ServletConfig config) throws ServletException
    {
        super.init(config);
        
        // can remove this call when we sort out autowiring from the XmlRpcServlet.
        RemoteApi remoteApi = new RemoteApi();
        SpringComponentContext.autowire(remoteApi);

        xmlrpc.addHandler("RemoteApi", remoteApi);        
    }
}
