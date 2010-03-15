package com.zutubi.pulse.core.dependency.ivy;

import static com.zutubi.util.reflection.ReflectionUtils.invoke;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.ivy.util.CopyProgressListener;
import org.apache.ivy.util.url.HttpClientHandler;

import java.io.*;
import java.net.URL;

/**
 * An extension of the HttpClientHandler that works around CIB-2380.  The fix is to
 * use a custom implementation of the RequestEntity instance that is able to handle
 * large files.  By default, the InputStreamRequestEntity attempts to 'buffer' the
 * file to calculate its length, triggering OOM exceptions on large files.
 */
public class CustomHttpClientHandler extends HttpClientHandler
{
    private static final String METHOD_USE_PROXY_AUTHENTICATION = "useProxyAuthentication";
    private static final String METHOD_USE_AUTHENTICATION = "useAuthentication";
    private static final String METHOD_GET_CLIENT = "getClient";

    @Override
    public void upload(File src, URL dest, CopyProgressListener l) throws IOException
    {
        HttpClient client = callGetClient(dest);

        PutMethod put = new PutMethod(normalizeToString(dest));
        put.setDoAuthentication(callUseAuthentication(dest) || callUseProxyAuthentication());
        
        try
        {
            put.setRequestEntity(new FileRequestEntity(src));
            int statusCode = client.executeMethod(put);
            validatePutStatusCode(dest, statusCode, null);
        }
        finally
        {
            put.releaseConnection();
        }
    }

    // The following methods are private in the HttpClientHandler, so
    // we use reflection to access them.

    private boolean callUseProxyAuthentication()
    {
        return (Boolean) invoke(this, METHOD_USE_PROXY_AUTHENTICATION);
    }

    private boolean callUseAuthentication(URL dest)
    {
        return (Boolean) invoke(this, METHOD_USE_AUTHENTICATION, dest);
    }

    private HttpClient callGetClient(URL dest)
    {
        return (HttpClient) invoke(this, METHOD_GET_CLIENT, dest);
    }

    // The key parts of the implemenation of this FileRequestEntity over the InputStreamRequestEntity is
    // that it does not buffer the content to determine the content length, and is repeatable without
    // buffering.
    private class FileRequestEntity implements RequestEntity
    {
        private File file = null;

        public FileRequestEntity(File file)
        {
            super();
            this.file = file;
        }

        public boolean isRepeatable()
        {
            return true;
        }

        public String getContentType()
        {
            return null;
        }

        public void writeRequest(OutputStream out) throws IOException
        {
            InputStream in = new FileInputStream(this.file);
            try
            {
                int l;
                byte[] buffer = new byte[4096];
                while ((l = in.read(buffer)) != -1)
                {
                    out.write(buffer, 0, l);
                }
            }
            finally
            {
                in.close();
            }
        }

        public long getContentLength()
        {
            return file.length();
        }
    }

}
