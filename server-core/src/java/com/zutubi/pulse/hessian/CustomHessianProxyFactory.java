/********************************************************************************
 @COPYRIGHT@
 ********************************************************************************/
package com.zutubi.pulse.hessian;

import com.caucho.hessian.client.HessianProxyFactory;

/**
 * This class provides a convenient single point to ensure use of the
 * CustomSerialiserFactory when creating hessian proxies.
 */
public class CustomHessianProxyFactory extends HessianProxyFactory
{
    public void setCustomSerialiserFactory(CustomSerialiserFactory factory)
    {
        getSerializerFactory().addFactory(factory);
    }
}
