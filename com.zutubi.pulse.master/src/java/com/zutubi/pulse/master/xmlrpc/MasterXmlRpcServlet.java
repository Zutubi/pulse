package com.zutubi.pulse.master.xmlrpc;

import com.zutubi.pulse.master.api.RemoteApi;
import com.zutubi.pulse.servercore.xmlrpc.XmlRpcServlet;
import com.zutubi.util.bean.ObjectFactory;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;

/**
 */
public class MasterXmlRpcServlet extends XmlRpcServlet
{
    private ObjectFactory objectFactory;

    public void init(ServletConfig config) throws ServletException
    {
        super.init(config);

        try
        {
            RemoteApi remoteApi = objectFactory.buildBean(RemoteApi.class);

            xmlrpc.addHandler("RemoteApi", remoteApi);
        }
        catch (Exception e)
        {
            throw new ServletException(e);
        }
    }

    public void setObjectFactory(ObjectFactory objectFactory)
    {
        this.objectFactory = objectFactory;
    }
}
