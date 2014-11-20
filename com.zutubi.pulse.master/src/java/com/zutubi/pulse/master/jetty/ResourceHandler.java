package com.zutubi.pulse.master.jetty;

import com.zutubi.util.logging.Logger;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.http.MimeTypes;
import org.eclipse.jetty.io.WriterOutputStream;
import org.eclipse.jetty.server.HttpOutput;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.util.BufferUtil;
import org.eclipse.jetty.util.Callback;
import org.eclipse.jetty.util.IO;
import org.eclipse.jetty.util.URIUtil;
import org.eclipse.jetty.util.resource.FileResource;
import org.eclipse.jetty.util.resource.Resource;

import javax.servlet.AsyncContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.util.logging.Level;

/**
 * An extension of Jetty's static file handler that supports PUT'ing files.  We use this for the
 * internal artifact repository, so it is only really tested with Ivy as a client.
 * <p/>
 * The HEAD and GET portions of this implementation are lifted from the superclass, with a little
 * factoring out to share the handling of conditional headers with PUT.  The PUT implementation
 * is based on Jetty's old implementation, updated and augmented by us.
 */
public class ResourceHandler extends org.eclipse.jetty.server.handler.ResourceHandler
{
    private static final Logger LOG = Logger.getLogger(ResourceHandler.class);

    @Override
    public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException
    {
        if (baseRequest.isHandled())
        {
            return;
        }

        Resource resource = getResource(request);
        if (resource == null)
        {
            if (LOG.isLoggable(Level.FINE))
            {
                LOG.fine("resource=null");
            }

            super.handle(target, baseRequest, request, response);
            return;
        }

        if (HttpMethod.GET.is(request.getMethod()) || HttpMethod.HEAD.is(request.getMethod()))
        {
            handleGet(target, baseRequest, request, response, resource);
        }
        else if (HttpMethod.PUT.is(request.getMethod()))
        {
            handlePut(target, baseRequest, request, response, resource);
        }
        else
        {
            super.handle(target, baseRequest, request, response);
        }
    }

    private void handleGet(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response, Resource resource) throws IOException, ServletException
    {
        boolean skipContentBody = HttpMethod.HEAD.is(request.getMethod());
        if (LOG.isLoggable(Level.FINE))
        {
            LOG.fine(String.format("resource=%s alias=%s exists=%s", resource.toString(), resource.getAlias().toString(), resource.exists()));
        }

        // If resource is not found
        if (!resource.exists())
        {
            // inject the jetty-dir.css file if it matches
            if (target.endsWith("/jetty-dir.css"))
            {
                resource = getStylesheet();
                if (resource == null)
                {
                    return;
                }
                response.setContentType("text/css");
            }
            else
            {
                //no resource - try other handlers
                super.handle(target, baseRequest, request, response);
                return;
            }
        }

        // We are going to serve something
        baseRequest.setHandled(true);

        // handle directories
        if (resource.isDirectory())
        {
            if (!request.getPathInfo().endsWith(URIUtil.SLASH))
            {
                response.sendRedirect(response.encodeRedirectURL(URIUtil.addPaths(request.getRequestURI(), URIUtil.SLASH)));
                return;
            }

            Resource welcome = getWelcome(resource);
            if (welcome != null && welcome.exists())
            {
                resource = welcome;
            }
            else
            {
                doDirectory(request, response, resource);
                baseRequest.setHandled(true);
                return;
            }
        }

        if (!passConditionalHeaders(baseRequest, request, response, resource))
        {
            return;
        }

        // set the headers
        MimeTypes mimeTypes = getMimeTypes();
        String mime = mimeTypes.getMimeByExtension(resource.toString());
        if (mime == null)
        {
            mime = mimeTypes.getMimeByExtension(request.getPathInfo());
        }

        doResponseHeaders(response, resource, mime);

        if (isEtags())
        {
            baseRequest.getResponse().getHttpFields().put(HttpHeader.ETAG, resource.getWeakETag());
        }

        if (!skipContentBody)
        {
            sendContent(request, response, resource);
        }
    }

    private void sendContent(HttpServletRequest request, HttpServletResponse response, Resource resource) throws IOException
    {
        OutputStream out;
        try
        {
            out = response.getOutputStream();
        }
        catch (IllegalStateException e)
        {
            out = new WriterOutputStream(response.getWriter());
        }

        // Has the output been wrapped
        if (!(out instanceof HttpOutput))
        // Write content via wrapped output
        {
            resource.writeTo(out, 0, resource.length());
        }
        else
        {
            // select async by size
            int minAsyncContentLength = getMinAsyncContentLength();
            int minMemoryMappedContentLength = getMinMemoryMappedContentLength();
            int min_async_size = minAsyncContentLength == 0 ? response.getBufferSize() : minAsyncContentLength;

            if (request.isAsyncSupported() &&
                    min_async_size > 0 &&
                    resource.length() >= min_async_size)
            {
                final AsyncContext async = request.startAsync();
                Callback callback = new Callback()
                {
                    public void succeeded()
                    {
                        async.complete();
                    }

                    public void failed(Throwable x)
                    {
                        LOG.warning(x.toString());
                        LOG.debug(x);
                        async.complete();
                    }
                };

                // Can we use a memory mapped file?
                if (minMemoryMappedContentLength > 0 &&
                        resource.length() > minMemoryMappedContentLength &&
                        resource instanceof FileResource)
                {
                    ByteBuffer buffer = BufferUtil.toMappedBuffer(resource.getFile());
                    ((HttpOutput) out).sendContent(buffer, callback);
                }
                else  // Do a blocking write of a channel (if available) or input stream
                {
                    // Close of the channel/inputstream is done by the async sendContent
                    ReadableByteChannel channel = resource.getReadableByteChannel();
                    if (channel != null)
                    {
                        ((HttpOutput) out).sendContent(channel, callback);
                    }
                    else
                    {
                        ((HttpOutput) out).sendContent(resource.getInputStream(), callback);
                    }
                }
            }
            else
            {
                // Can we use a memory mapped file?
                if (minMemoryMappedContentLength > 0 &&
                        resource.length() > minMemoryMappedContentLength &&
                        resource instanceof FileResource)
                {
                    ByteBuffer buffer = BufferUtil.toMappedBuffer(resource.getFile());
                    ((HttpOutput) out).sendContent(buffer);
                }
                else  // Do a blocking write of a channel (if available) or input stream
                {
                    ReadableByteChannel channel = resource.getReadableByteChannel();
                    if (channel != null)
                    {
                        ((HttpOutput) out).sendContent(channel);
                    }
                    else
                    {
                        ((HttpOutput) out).sendContent(resource.getInputStream());
                    }
                }
            }
        }
    }

    private void handlePut(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response, Resource resource) throws IOException, ServletException
    {
        boolean exists = resource.exists();
        if (exists && !passConditionalHeaders(baseRequest, request, response, resource))
        {
            return;
        }

        if (target.endsWith("/"))
        {
            if (!exists)
            {
                if (!resource.getFile().mkdirs())
                {
                    baseRequest.setHandled(true);
                    response.sendError(HttpServletResponse.SC_FORBIDDEN, "Directories could not be created");
                }
                else
                {
                    baseRequest.setHandled(true);
                    response.setStatus(HttpServletResponse.SC_CREATED);
                }
            }
            else
            {
                baseRequest.setHandled(true);
                response.setStatus(HttpServletResponse.SC_OK);
            }
        }
        else
        {
            File dir = resource.getFile().getParentFile();
            if (!dir.isDirectory() && !dir.mkdirs())
            {
                baseRequest.setHandled(true);
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Directories could not be created");
            }
            else
            {
                try
                {
                    int toRead = request.getContentLength();
                    InputStream in = request.getInputStream();
                    OutputStream out = null;
                    try
                    {
                        File file = resource.getFile();
                        out = new FileOutputStream(file);
                        if (toRead >= 0)
                        {
                            IO.copy(in, out, toRead);
                        }
                        else
                        {
                            IO.copy(in, out);
                        }
                    }
                    finally
                    {
                        if (out != null)
                        {
                            out.close();
                        }
                    }
                    baseRequest.setHandled(true);
                    response.setStatus(exists ? HttpServletResponse.SC_OK : HttpServletResponse.SC_CREATED);
                }
                catch (Exception ex)
                {
                    LOG.warning(ex);
                    response.sendError(HttpServletResponse.SC_FORBIDDEN, ex.getMessage());
                }
            }
        }
    }

    private boolean passConditionalHeaders(Request baseRequest, HttpServletRequest request, HttpServletResponse response, Resource resource) throws IOException
    {
        if (!HttpMethod.HEAD.is(request.getMethod()))
        {
            long last_modified = resource.lastModified();
            if (isEtags())
            {
                String ifnm = request.getHeader(HttpHeader.IF_NONE_MATCH.asString());
                String etag = resource.getWeakETag();
                if (ifnm != null && ifnm.equals(etag))
                {
                    response.setStatus(HttpStatus.NOT_MODIFIED_304);
                    baseRequest.getResponse().getHttpFields().put(HttpHeader.ETAG, etag);
                    return false;
                }
            }

            if (last_modified > 0)
            {
                long if_modified = request.getDateHeader(HttpHeader.IF_MODIFIED_SINCE.asString());
                if (if_modified > 0 && last_modified / 1000 <= if_modified / 1000)
                {
                    response.setStatus(HttpStatus.NOT_MODIFIED_304);
                    return false;
                }
            }
        }

        return true;
    }

}
