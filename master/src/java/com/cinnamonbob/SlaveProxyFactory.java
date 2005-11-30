package com.cinnamonbob;

import com.cinnamonbob.hessian.CustomHessianProxyFactory;
import com.cinnamonbob.model.Slave;
import com.cinnamonbob.services.SlaveService;

import java.net.MalformedURLException;

/**
 */
public class SlaveProxyFactory
{
    private CustomHessianProxyFactory hessianProxyFactory;

    public SlaveService createProxy(Slave slave) throws MalformedURLException
    {
        String url = "http://" + slave.getHost() + "/hessian";
        return (SlaveService) hessianProxyFactory.create(SlaveService.class, url);
    }

    public void setHessianProxyFactory(CustomHessianProxyFactory hessianProxyFactory)
    {
        this.hessianProxyFactory = hessianProxyFactory;
    }
}
