package com.zutubi.pulse.core.dependency.ivy;

import org.apache.ivy.util.url.BasicURLHandler;
import org.apache.ivy.util.url.HttpClientHandler;
import org.apache.ivy.util.CopyProgressListener;

import java.net.URL;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * An annoying hack to get security working in a sensible manner with ivy.
 *
 * The basic problem is that HttpClientHandler does not handle the file protocol, but the BasicURLHandler
 * does.  At the same time, the HttpClientHandler handles authentication, but the BasicURLHandler does not
 * appear to.  So, here is a handler that does both based on the implementation of the other two components.
 *
 * When we are using the file protocol, we are dealing with the file system and hence do not need authentication.
 * When we are not using the file protocol, we need authentication.
 */
public class CustomURLHandler extends HttpClientHandler
{
    private BasicURLHandler delegate = new BasicURLHandler();

    public URLInfo getURLInfo(URL url, int timeout)
    {
        if (isFileProtocol(url))
        {
            return delegate.getURLInfo(url, timeout);
        }
        return super.getURLInfo(url, timeout);
    }

    public void download(URL src, File dest, CopyProgressListener l) throws IOException
    {
        if (isFileProtocol(src))
        {
            delegate.download(src, dest, l);
        }
        super.download(src, dest, l); 
    }

    public void upload(File src, URL dest, CopyProgressListener l) throws IOException
    {
        if (isFileProtocol(dest))
        {
            delegate.upload(src, dest, l);
        }
        super.upload(src, dest, l);
    }

    public InputStream openStream(URL url) throws IOException
    {
        if (isFileProtocol(url))
        {
            return delegate.openStream(url);
        }
        return super.openStream(url);
    }
    
    private boolean isFileProtocol(URL url)
    {
        return url.getProtocol().equals("file");
    }
}
