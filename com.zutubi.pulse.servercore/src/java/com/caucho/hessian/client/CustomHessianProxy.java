package com.caucho.hessian.client;

import com.caucho.hessian.io.AbstractHessianInput;
import com.caucho.hessian.io.HessianOutput;
import com.caucho.hessian.io.HessianProtocolException;
import com.zutubi.util.StringUtils;
import com.zutubi.util.io.IOUtils;
import com.zutubi.util.logging.Logger;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashSet;
import java.util.Set;

/**
 */
public class CustomHessianProxy extends HessianProxy
{
    private static final Logger LOG = Logger.getLogger(CustomHessianProxy.class);

    private static final String PROPERTY_HESSIAN_SUPPRESSED_ERRORS = "pulse.hessian.suppressed.errors";
    private static final String DEFAULT_HESSIAN_SUPPRESSED_ERRORS = "ping";

    private HessianProxyFactory factory;
    private Set<String> suppressedMethods = null;

    public CustomHessianProxy(HessianProxyFactory factory, URL url)
    {
        super(factory, url);
        this.factory = factory;
    }

    // This is directly copied from HessianProxy, purely so we can log
    // exceptions properly rather than swallowing them.
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
    {
        String methodName = method.getName();
        Class[] params = method.getParameterTypes();

        // equals and hashCode are special cased
        if (methodName.equals("equals") &&
                params.length == 1 && params[0].equals(Object.class))
        {
            Object value = args[0];
            if (value == null || !Proxy.isProxyClass(value.getClass()))
            {
                return false;
            }

            HessianProxy handler = (HessianProxy) Proxy.getInvocationHandler(value);

            return getURL().equals(handler.getURL());
        }
        else if (methodName.equals("hashCode") && params.length == 0)
        {
            return getURL().hashCode();
        }
        else if (methodName.equals("getHessianType"))
        {
            return proxy.getClass().getInterfaces()[0].getName();
        }
        else if (methodName.equals("getHessianURL"))
        {
            return getURL().toString();
        }
        else if (methodName.equals("toString") && params.length == 0)
        {
            return "[HessianProxy " + getURL() + "]";
        }

        InputStream is = null;
        URLConnection conn = null;

        try
        {
            conn = factory.openConnection(getURL());
            conn.setRequestProperty("Content-Type", "text/xml");

            OutputStream os;
            try
            {
                os = conn.getOutputStream();
            }
            catch (Exception e)
            {
                throw new HessianRuntimeException(e);
            }

            HessianOutput out = factory.getHessianOutput(os);

            if (!factory.isOverloadEnabled())
            {
            }
            else if (args != null)
            {
                methodName = methodName + "__" + args.length;
            }
            else
            {
                methodName = methodName + "__0";
            }

            out.call(methodName, args);
            os.flush();

            if (conn instanceof HttpURLConnection)
            {
                HttpURLConnection httpConn = (HttpURLConnection) conn;
                int code = 500;

                try
                {
                    code = httpConn.getResponseCode();
                }
                catch (Exception e)
                {
                    if (!methodErrorsSupressed(methodName))
                    {
                        LOG.severe(e);
                    }
                }

                if (code != 200)
                {
                    StringBuffer sb = new StringBuffer();
                    int ch;

                    try
                    {
                        is = httpConn.getInputStream();

                        if (is != null)
                        {
                            while ((ch = is.read()) >= 0)
                            {
                                sb.append((char) ch);
                            }

                            is.close();
                        }

                        is = httpConn.getErrorStream();
                        if (is != null)
                        {
                            while ((ch = is.read()) >= 0)
                            {
                                sb.append((char) ch);
                            }
                        }
                    }
                    catch (FileNotFoundException e)
                    {
                        throw new HessianRuntimeException(String.valueOf(e));
                    }
                    catch (IOException e)
                    {
                        if (!methodErrorsSupressed(methodName))
                        {
                            LOG.severe(e);
                        }
                    }

                    if (is != null)
                    {
                        is.close();
                    }

                    throw new HessianProtocolException(sb.toString());
                }
            }

            is = conn.getInputStream();

            AbstractHessianInput in = factory.getHessianInput(is);

            return in.readReply(method.getReturnType());
        }
        catch (HessianProtocolException e)
        {
            if (!methodErrorsSupressed(methodName))
            {
                LOG.severe(e);
            }
            throw new HessianRuntimeException(e);
        }
        finally
        {
            IOUtils.close(is);

            try
            {
                if (conn != null)
                {
                    ((HttpURLConnection) conn).disconnect();
                }
            }
            catch (Throwable e)
            {
                // Empty
            }
        }
    }

    private boolean methodErrorsSupressed(String methodName)
    {
        if(suppressedMethods == null)
        {
            suppressedMethods = new HashSet<String>(StringUtils.split(System.getProperty(PROPERTY_HESSIAN_SUPPRESSED_ERRORS, DEFAULT_HESSIAN_SUPPRESSED_ERRORS)));
        }

        return suppressedMethods.contains(methodName);
    }

}
