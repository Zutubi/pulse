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

package com.zutubi.util.reflection;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * An invocation handler that can be used to create proxies that simply wrap delegates.
 */
public class DelegatingInvocationHandler<T> implements InvocationHandler
{
    private T delegate;

    /**
     * Convenience method for creating a delegating proxy for the given interface wrapping the given
     * delegate.  The system class loader is used for the proxy.
     *
     * @param iface    the interface to implement in the proxy
     * @param delegate the delegate to forward all proxy invocations to
     * @param <T>      type of the interface
     * @return a new proxy that implements the given interface by forwarding to the delegate
     */
    @SuppressWarnings("unchecked")
    public static <T> T newProxy(Class<T> iface, final T delegate)
    {
        return (T) Proxy.newProxyInstance(ClassLoader.getSystemClassLoader(), new Class<?>[]{iface}, new DelegatingInvocationHandler(delegate));
    }

    /**
     * Creates a new handler that forwards all method calls to the given delegate.
     * 
     * @param delegate the delegate to forward calls to
     */
    public DelegatingInvocationHandler(T delegate)
    {
        this.delegate = delegate;
    }

    /**
     * Retrieves the delegate we are wrapping.
     * 
     * @return the wrapped delegate
     */
    public T getDelegate()
    {
        return delegate;
    }

    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
    {
        try
        {
            return method.invoke(delegate, args);
        }
        catch (IllegalAccessException e)
        {
            throw new RuntimeException(e);
        }
        catch (IllegalArgumentException e)
        {
            throw new RuntimeException(e);
        }
        catch (InvocationTargetException e)
        {
            throw e.getTargetException();
        }
    }
}
