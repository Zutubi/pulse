package com.zutubi.pulse.xmlrpc;

import com.zutubi.pulse.api.RemoteApi;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;

/**
 */
public class MasterXmlRpcServlet extends XmlRpcServlet
{
    public void init(ServletConfig config) throws ServletException
    {
        super.init(config);
        xmlrpc.addHandler("RemoteApi", new RemoteApi());        
    }
}
