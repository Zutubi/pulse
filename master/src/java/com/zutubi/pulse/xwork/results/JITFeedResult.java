package com.zutubi.pulse.xwork.results;

import com.opensymphony.webwork.dispatcher.WebWorkResultSupport;
import com.opensymphony.webwork.ServletActionContext;
import com.opensymphony.xwork.ActionInvocation;
import com.opensymphony.xwork.util.OgnlValueStack;
import com.opensymphony.util.TextUtils;
import com.sun.syndication.feed.WireFeed;
import com.sun.syndication.io.WireFeedOutput;
import com.zutubi.pulse.util.Constants;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.Calendar;

/**
 * Render an Rome synd feed instance.
 *
 *
 * @author Daniel Ostermeier
 */
public class JITFeedResult extends WebWorkResultSupport
{
    /**
     * The name used to retrieve the feed instance from the OGNL stack.
     */
    private String feedName = "feed";

    /**
     * Last modified header.
     */
    private static final String LAST_MODIFIED = "Last-Modified";

    /**
     * If none match header.
     */
    private static final String IF_NONE_MATCH = "If-None-Match";

    /**
     * If modified since header.
     */
    private static final String IF_MODIFIED_SINCE = "If-Modified-Since";

    /**
     * ETag header.
     */
    private static final String ETAG = "ETag";

    /**
     * Specify the name of the feed as it appears in the OGNL stack. This value defaults to "feed"
     *
     * @param feedName
     */
    public void setFeedName(String feedName)
    {
        this.feedName = feedName;
    }

    protected void doExecute(String format, ActionInvocation actionInvocation) throws Exception
    {
        HttpServletResponse response = ServletActionContext.getResponse();
        HttpServletRequest request = ServletActionContext.getRequest();

        OgnlValueStack stack = actionInvocation.getStack();

        JITFeed renderer = (JITFeed) stack.findValue(feedName);
        if (renderer == null)
        {
            // this means that the feed requested does not exist. Thats not to say it
            // never existed, just that it no longer exists. So, we deal with it appropriately
            response.sendError(HttpServletResponse.SC_GONE);
            return;
        }

        // rss content type.
        response.setContentType("application/rss+xml; charset=UTF-8");
        response.setHeader("Content-Disposition", "filename=rss.xml");

        // The last modified date from the request header may be invalid. If that is the case, then
        // we need to default to something workable.
        Date ifModifiedSince;
        try
        {
            ifModifiedSince = new Date(request.getDateHeader(IF_MODIFIED_SINCE));
        }
        catch (Throwable t)
        {
            ifModifiedSince = Constants.DAY_0;
        }

        Date lastModified = ifModifiedSince;

        if (renderer.hasEntries())
        {
            // get the latest feed entry - assuming the latest is at the top.
            lastModified = renderer.getPublishedDate();
            Date updatedDate = renderer.getUpdatedDate();
            if (updatedDate != null && lastModified.compareTo(updatedDate) < 0)
            {
                lastModified = updatedDate;
            }
        }

        // ETag should be based on the pre-concatenated lastmodified date.
        String etag = Long.toString(lastModified.getTime());
        response.setHeader(ETAG, etag);

        // drop things below the second, that level of detail is not included in the http headers.
        Calendar cal = Calendar.getInstance();
        cal.setTime(lastModified);
        cal.set(Calendar.MILLISECOND, 0);
        lastModified = cal.getTime();

        // always set
        response.setDateHeader(LAST_MODIFIED, lastModified.getTime());

        // check the headers to determine whether or not a response is required.
        if (TextUtils.stringSet(request.getHeader(IF_NONE_MATCH)) ||
                TextUtils.stringSet(request.getHeader(IF_MODIFIED_SINCE)))
        {
            if (etag.equals(request.getHeader(IF_NONE_MATCH)) &&
                    lastModified.getTime() == ifModifiedSince.getTime())
            {
                // if response is not required, send 304 Not modified.
                response.sendError(HttpServletResponse.SC_NOT_MODIFIED);
                return;
            }
        }

        // render the feed in the requested format.
        WireFeed outFeed = renderer.createFeed().createWireFeed(format);
        outFeed.setEncoding(response.getCharacterEncoding());
        new WireFeedOutput().output(outFeed, response.getWriter());
        response.flushBuffer();
    }
}
