package com.zutubi.pulse.servercore.jetty;

import org.mortbay.http.HttpFields;
import org.mortbay.http.HttpRequest;
import org.mortbay.http.HttpResponse;
import org.mortbay.http.ResourceCache;
import org.mortbay.util.CachedResource;
import org.mortbay.util.Resource;
import org.mortbay.util.StringUtil;
import org.mortbay.util.URI;

import java.io.IOException;
import java.text.DateFormat;
import java.util.Arrays;
import java.util.Date;

import com.zutubi.util.Constants;
import static com.zutubi.util.Constants.UTF8;

/**
 * This resource handler implementation is an extension of the original implementation
 * that fixes a bug in the original implementation of the html directory listing code.
 * The bug was that the generated directory listing anchors were not closed, causing problems
 * with the automated processing of the output.
 *
 * Much of the code (except for the bug fix) is a duplication of the original, necessary
 * to maintain the existing behaviour whilst being able to get at the code that needs to be
 * fixed.
 */
public class ResourceHandler extends org.mortbay.http.handler.ResourceHandler
{
    public void handleGet(HttpRequest request, HttpResponse response, String pathInContext, String pathParams, Resource resource) throws IOException
    {
        if (resource != null && resource.exists())
        {
            // check if directory
            if (resource.isDirectory())
            {
                if (!pathInContext.endsWith("/") && !pathInContext.equals("/"))
                {
                    String q = request.getQuery();
                    StringBuffer buf = request.getRequestURL();
                    if (q != null && q.length() != 0)
                    {
                        buf.append('?');
                        buf.append(q);
                    }
                    response.setField(HttpFields.__Location, URI.addPaths(buf.toString(), "/"));
                    response.setStatus(302);
                    request.setHandled(true);
                    return;
                }

                // See if index file exists
                String welcome = getHttpContext().getWelcomeFile(resource);
                if (welcome != null)
                {
                    // Forward to the index
                    String ipath = URI.addPaths(pathInContext, welcome);
                    if (getRedirectWelcome())
                    {
                        // Redirect to the index
                        ipath = URI.addPaths(getHttpContext().getContextPath(), ipath);
                        response.setContentLength(0);
                        response.sendRedirect(ipath);
                    }
                    else
                    {
                        URI uri = request.getURI();
                        uri.setPath(URI.addPaths(uri.getPath(), welcome));
                        getHttpContext().handle(ipath, pathParams, request, response);
                    }
                    return;
                }

                // Check modified dates
                if (!passConditionalHeaders(request, response, resource))
                    return;
                // If we got here, no forward to index took place
                sendDirectory(request, response, resource, pathInContext.length() > 1);
            }
            // check if it is a file
            else if (resource.exists())
            {
                // Check modified dates
                if (!passConditionalHeaders(request, response, resource))
                    return;
                sendData(request, response, pathInContext, resource, true);
            }
            else
            {
                // don't know what it is
            }
        }
    }

    /* ------------------------------------------------------------ */
    /* Check modification date headers.
     */
    private boolean passConditionalHeaders(HttpRequest request,
                                           HttpResponse response,
                                           Resource resource)
            throws IOException
    {
        if (!request.getMethod().equals(HttpRequest.__HEAD))
        {
            // If we have meta data for the file
            // Try a direct match for most common requests. Avoids
            // parsing the date.
            ResourceCache.ResourceMetaData metaData =
                    (ResourceCache.ResourceMetaData) resource.getAssociate();
            if (metaData != null)
            {
                String ifms = request.getField(HttpFields.__IfModifiedSince);
                String mdlm = metaData.getLastModified();
                if (ifms != null && mdlm != null && ifms.equals(mdlm))
                {
                    response.setStatus(HttpResponse.__304_Not_Modified);
                    request.setHandled(true);
                    return false;
                }
            }


            long date = 0;
            // Parse the if[un]modified dates and compare to resource

            if ((date = request.getDateField(HttpFields.__IfUnmodifiedSince)) > 0)
            {
                if (resource.lastModified() / 1000 > date / 1000)
                {
                    response.sendError(HttpResponse.__412_Precondition_Failed);
                    return false;
                }
            }

            if ((date = request.getDateField(HttpFields.__IfModifiedSince)) > 0)
            {

                if (resource.lastModified() / 1000 <= date / 1000)
                {
                    response.setStatus(HttpResponse.__304_Not_Modified);
                    request.setHandled(true);
                    return false;
                }
            }

        }
        return true;
    }

    /* ------------------------------------------------------------------- */
    void sendDirectory(HttpRequest request,
                       HttpResponse response,
                       Resource resource,
                       boolean parent)
            throws IOException
    {
        if (!isDirAllowed())
        {
            response.sendError(HttpResponse.__403_Forbidden);
            return;
        }

        request.setHandled(true);

        byte[] data = null;
        if (resource instanceof CachedResource)
            data = ((CachedResource) resource).getCachedData();

        if (data == null)
        {
            String base = URI.addPaths(request.getPath(), "/");
            String dir = getListHTML(resource, URI.encodePath(base), parent);
            if (dir == null)
            {
                response.sendError(HttpResponse.__403_Forbidden,
                        "No directory");
                return;
            }
            data = dir.getBytes(UTF8);
            if (resource instanceof CachedResource)
                ((CachedResource) resource).setCachedData(data);
        }

        response.setContentType("text/html; charset=UTF8");
        response.setContentLength(data.length);

        if (request.getMethod().equals(HttpRequest.__HEAD))
        {
            response.commit();
            return;
        }

        response.getOutputStream().write(data, 0, data.length);
        response.commit();
    }

    public String getListHTML(Resource resource, String base, boolean parent) throws IOException
    {
        if (!resource.isDirectory())
        {
            return null;
        }


        String[] ls = resource.list();
        if (ls == null)
            return null;
        Arrays.sort(ls);

        String title = "Directory: " + URI.decodePath(base);
        title = StringUtil.replace(StringUtil.replace(title, "<", "&lt;"), ">", "&gt;");
        StringBuffer buf = new StringBuffer(4096);
        buf.append("<HTML><HEAD><TITLE>");
        buf.append(title);
        buf.append("</TITLE></HEAD><BODY>\n<H1>");
        buf.append(title);
        buf.append("</H1><TABLE BORDER=0>");

        if (parent)
        {
            buf.append("<TR><TD><A HREF=");
            buf.append(URI.encodePath(URI.addPaths(base, "../")));
            buf.append(">Parent Directory</A></TD><TD></TD><TD></TD></TR>\n");
        }

        DateFormat dfmt = DateFormat.getDateTimeInstance(DateFormat.MEDIUM,
                DateFormat.MEDIUM);
        for (String l : ls)
        {
            String encoded = URI.encodePath(l);
            Resource item = resource.addPath(encoded);

            buf.append("<TR><TD><A HREF=\"");

            String path = URI.addPaths(base, encoded);

            if (item.isDirectory() && !path.endsWith("/"))
            {
                path = URI.addPaths(path, "/");
            }
            buf.append(path);
            buf.append("\">");
            buf.append(StringUtil.replace(StringUtil.replace(l, "<", "&lt;"), ">", "&gt;"));
            buf.append("&nbsp;");
            // fix the bug by adding the </A> to the appended string to close the anchor.
            buf.append("</A></TD><TD ALIGN=right>");
            buf.append(item.length());
            buf.append(" bytes&nbsp;</TD><TD>");
            buf.append(dfmt.format(new Date(item.lastModified())));
            buf.append("</TD></TR>\n");
        }
        buf.append("</TABLE>\n");
        buf.append("</BODY></HTML>\n");

        return buf.toString();
    }
}
