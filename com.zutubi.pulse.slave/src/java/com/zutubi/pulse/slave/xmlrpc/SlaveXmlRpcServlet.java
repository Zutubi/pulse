package com.zutubi.pulse.slave.xmlrpc;

import com.zutubi.pulse.servercore.xmlrpc.XmlRpcServlet;
import com.zutubi.pulse.slave.api.RemoteApi;
import com.zutubi.util.bean.ObjectFactory;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;

public class SlaveXmlRpcServlet extends XmlRpcServlet
{
    private ObjectFactory objectFactory;

    public void init(ServletConfig config) throws ServletException
    {
        super.init(config);

        RemoteApi remoteApi = objectFactory.buildBean(RemoteApi.class);
        xmlrpc.addHandler("RemoteApi", remoteApi);
    }

    public void setObjectFactory(ObjectFactory objectFactory)
    {
        this.objectFactory = objectFactory;
    }
}
