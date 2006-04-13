/********************************************************************************
 @COPYRIGHT@
 ********************************************************************************/
package com.zutubi.pulse.slave;

import com.zutubi.pulse.hessian.CustomHessianProxyFactory;
import com.zutubi.pulse.services.MasterService;

import java.net.MalformedURLException;

/**
 */
public class MasterProxyFactory
{
    private CustomHessianProxyFactory hessianProxyFactory;

    public MasterService createProxy(String master) throws MalformedURLException
    {
        String url = "http://" + master + "/hessian";
        return (MasterService) hessianProxyFactory.create(MasterService.class, url);
    }

    public void setHessianProxyFactory(CustomHessianProxyFactory hessianProxyFactory)
    {
        this.hessianProxyFactory = hessianProxyFactory;
    }
}
