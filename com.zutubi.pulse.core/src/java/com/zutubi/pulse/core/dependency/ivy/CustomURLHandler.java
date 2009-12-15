package com.zutubi.pulse.core.dependency.ivy;

import org.apache.ivy.util.CopyProgressListener;
import org.apache.ivy.util.url.AbstractURLHandler;
import org.apache.ivy.util.url.BasicURLHandler;
import org.apache.ivy.util.url.HttpClientHandler;
import org.apache.ivy.util.url.URLHandler;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * An annoying hack to get security working in a sensible manner with ivy and the need to encode urls before
 * using them.
 *
 * The basic problem is that HttpClientHandler does not handle the file protocol, but the BasicURLHandler
 * does.  At the same time, the HttpClientHandler handles authentication, but the BasicURLHandler does not
 * appear to.  So, here is a handler that does both based on the implementation of the other two components.
 *
 * When we are using the file protocol, we are dealing with the file system and hence do not need authentication.
 * When we are not using the file protocol, we need authentication.
 *
 * Regarding the encoding, ivy does not encode its urls.  So, to support characters that can not be used directly on
 * a url (specifically '%') we encode all urls that go to jetty.  On the other end, jetty has had its alias checking
 * disabled to allow for the encoded paths.  All in all this means we can %encode artifact details and have them
 * work.
 */
public class CustomURLHandler extends AbstractURLHandler
{
    private URLHandler basicUrlHandler = new BasicURLHandler();
    private URLHandler httpClientHandler = new HttpClientHandler();

    public CustomURLHandler()
    {
    }

    public URLInfo getURLInfo(URL url)
    {
        if (isFileProtocol(url) || isJarProtocol(url))
        {
            return basicUrlHandler.getURLInfo(url);
        }
        return httpClientHandler.getURLInfo(url);
    }

    public URLInfo getURLInfo(URL url, int timeout)
    {
        if (isFileProtocol(url) || isJarProtocol(url))
        {
            return basicUrlHandler.getURLInfo(url, timeout);
        }
        return httpClientHandler.getURLInfo(url, timeout);
    }

    public void download(URL src, File dest, CopyProgressListener l) throws IOException
    {
        if (isFileProtocol(src) || isJarProtocol(src))
        {
            basicUrlHandler.download(src, dest, l);
        }
        else
        {
            httpClientHandler.download(src, dest, l);
        }
    }

    public void upload(File src, URL dest, CopyProgressListener l) throws IOException
    {
        if (isFileProtocol(dest) || isJarProtocol(dest))
        {
            basicUrlHandler.upload(src, dest, l);
        }
        else
        {
            httpClientHandler.upload(src, dest, l);
        }
    }

    public InputStream openStream(URL url) throws IOException
    {
        if (isFileProtocol(url) || isJarProtocol(url))
        {
            return basicUrlHandler.openStream(url);
        }
        return httpClientHandler.openStream(url);
    }
    
    private boolean isFileProtocol(URL url)
    {
        return url.getProtocol().equals("file");
    }

    private boolean isJarProtocol(URL url)
    {
        return url.getProtocol().equals("jar");
    }
}
