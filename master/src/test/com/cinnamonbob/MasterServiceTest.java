package com.cinnamonbob;

import com.cinnamonbob.test.BobTestCase;
import com.cinnamonbob.hessian.CustomSerialiserFactory;
import com.cinnamonbob.core.model.BuildResult;
import com.caucho.hessian.io.SerializerFactory;
import com.caucho.hessian.client.HessianProxyFactory;

import java.net.MalformedURLException;

/**
 */
public class MasterServiceTest extends BobTestCase
{
    public void testBuildComplete() throws MalformedURLException
    {
        HessianProxyFactory factory = new HessianProxyFactory();
        factory.getSerializerFactory().addFactory(new CustomSerialiserFactory());
        String url = "http://localhost:8080/hessian";
        MasterService proxy = (MasterService) factory.create(MasterService.class, url);
        proxy.buildComplete(new BuildResult());
    }
}
