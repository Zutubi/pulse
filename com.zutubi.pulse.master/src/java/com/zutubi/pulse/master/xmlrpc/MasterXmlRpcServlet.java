package com.zutubi.pulse.master.xmlrpc;

import com.zutubi.pulse.master.api.MonitorApi;
import com.zutubi.pulse.master.api.RemoteApi;
import com.zutubi.pulse.master.api.TestApi;
import com.zutubi.pulse.servercore.xmlrpc.XmlRpcServlet;
import com.zutubi.util.bean.ObjectFactory;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;

/**
 * Servlet for XML-RPC APIs.
 */
public class MasterXmlRpcServlet extends XmlRpcServlet
{
    private ObjectFactory objectFactory;

    public void init(ServletConfig config) throws ServletException
    {
        super.init(config);

        RemoteApi remoteApi = objectFactory.buildBean(RemoteApi.class);
        xmlrpc.addHandler("RemoteApi", remoteApi);
        TestApi testApi = objectFactory.buildBean(TestApi.class);
        xmlrpc.addHandler("TestApi", testApi);
        MonitorApi monitorApi = objectFactory.buildBean(MonitorApi.class);
        xmlrpc.addHandler("MonitorApi", monitorApi);
    }

    public void setObjectFactory(ObjectFactory objectFactory)
    {
        this.objectFactory = objectFactory;
    }
}
