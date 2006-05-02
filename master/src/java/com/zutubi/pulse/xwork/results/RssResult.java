/********************************************************************************
 @COPYRIGHT@
 ********************************************************************************/
package com.zutubi.pulse.xwork.results;

import com.opensymphony.util.TextUtils;
import com.opensymphony.webwork.ServletActionContext;
import com.opensymphony.webwork.dispatcher.WebWorkResultSupport;
import com.opensymphony.xwork.ActionInvocation;
import com.opensymphony.xwork.util.OgnlValueStack;
import com.sun.syndication.feed.WireFeed;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.io.WireFeedOutput;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Render an RSS feed.
 *
 * @author Daniel Ostermeier
 */
public class RssResult extends WebWorkResultSupport
{
    private static final Date DAY_0 = new Date(0);

    protected void doExecute(String format, ActionInvocation actionInvocation) throws Exception
    {
        // No need to create a session for RSS
        ServletActionContext.getRequest().getSession(false);
        HttpServletResponse response = ServletActionContext.getResponse();
        HttpServletRequest request = ServletActionContext.getRequest();

        OgnlValueStack stack = actionInvocation.getStack();

        SyndFeed feed = (SyndFeed) stack.findValue("feed");
        if (feed == null)
        {
            // this means that the feed requested does not exist. Thats not to say it
            // never existed, just that it no longer exists. So, we deal with it appropriately
            response.sendError(HttpServletResponse.SC_GONE);
            return;
        }

        // rss content type.
        response.setContentType("application/rss+xml; charset=UTF-8");
        response.setHeader("Content-Disposition", "filename=rss.xml");

        Date feedLastModified = DAY_0;
        List entries = feed.getEntries();
        if (entries.size() > 0)
        {
            // get the latest feed entry - assuming the latest is at the top.
            SyndEntry entry = (SyndEntry) entries.get(0);
            feedLastModified = entry.getPublishedDate();
            Date updatedDate = entry.getUpdatedDate();
            if (updatedDate != null && feedLastModified.compareTo(updatedDate) < 0)
            {
                feedLastModified = updatedDate;
            }
        }

        // drop things below the second, that level of detail is not included in the http headers.
        Calendar cal = Calendar.getInstance();
        cal.setTime(feedLastModified);
        cal.set(Calendar.MILLISECOND, 0);
        feedLastModified = cal.getTime();

        // always set
        response.setDateHeader("Last-Modified", feedLastModified.getTime());

        // ETag should be based on the pre-concatenated lastmodified date.
        String etag = Long.toString(feedLastModified.getTime());
        response.setHeader("ETag", etag);

        // check the headers to determine whether or not a response is required.
        if (TextUtils.stringSet(request.getHeader("If-None-Match")) ||
                TextUtils.stringSet(request.getHeader("If-Modified-Since")))
        {
            if (etag.equals(request.getHeader("If-None-Match")) &&
                    feedLastModified.getTime() == request.getDateHeader("If-Modified-Since"))
            {
                // if response is not required, send 304 Not modified.
                response.sendError(HttpServletResponse.SC_NOT_MODIFIED);
                return;

            }
        }

        // render the feed in the requested format.
        WireFeed outFeed = feed.createWireFeed(format);
        outFeed.setEncoding("UTF-8");
        new WireFeedOutput().output(outFeed, response.getWriter());
        response.flushBuffer();
    }
}
