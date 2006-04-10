package com.cinnamonbob;

import com.cinnamonbob.hessian.CustomHessianProxyFactory;
import com.cinnamonbob.model.Slave;
import com.cinnamonbob.services.SlaveService;

import java.net.MalformedURLException;
import java.net.URL;

/**
 */
public class SlaveProxyFactory
{
    private CustomHessianProxyFactory hessianProxyFactory;

    public SlaveService createProxy(Slave slave) throws MalformedURLException
    {
        URL url = new URL("http", slave.getHost(), slave.getPort(), "/hessian");
        return (SlaveService) hessianProxyFactory.create(SlaveService.class, url.toString());
    }

    public void setHessianProxyFactory(CustomHessianProxyFactory hessianProxyFactory)
    {
        this.hessianProxyFactory = hessianProxyFactory;
    }
}
