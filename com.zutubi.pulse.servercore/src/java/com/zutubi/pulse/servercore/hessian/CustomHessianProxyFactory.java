/* Copyright 2017 Zutubi Pty Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zutubi.pulse.servercore.hessian;

import com.caucho.hessian.client.CustomHessianProxy;
import com.caucho.hessian.client.HessianProxy;
import com.caucho.hessian.client.HessianProxyFactory;
import com.caucho.hessian.io.HessianRemoteObject;

import java.lang.reflect.Proxy;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * This class provides a convenient single point to ensure use of the
 * CustomSerialiserFactory when creating hessian proxies.
 */
public class CustomHessianProxyFactory extends HessianProxyFactory
{
    private static final String PROPERTY_HESSIAN_READ_TIMEOUT = "pulse.hessian.read.timeout";

    public CustomHessianProxyFactory()
    {
        super();
        setReadTimeout(Integer.getInteger(PROPERTY_HESSIAN_READ_TIMEOUT, 60) * 1000);
    }

    // Copied from HessianProxyFactory, changed only to create our custom
    // proxy class.
    public Object create(Class api, String url) throws MalformedURLException
    {
        HessianProxy handler = new CustomHessianProxy(this, new URL(url));

        return Proxy.newProxyInstance(api.getClassLoader(),
                                      new Class[]{api,
                                                  HessianRemoteObject.class},
                                      handler);
    }

    public void setCustomSerialiserFactory(CustomSerialiserFactory factory)
    {
        getSerializerFactory().addFactory(factory);
    }
}
