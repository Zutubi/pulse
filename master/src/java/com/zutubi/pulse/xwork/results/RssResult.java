/********************************************************************************
 @COPYRIGHT@
 ********************************************************************************/
package com.zutubi.pulse.xwork.results;

import com.opensymphony.webwork.dispatcher.WebWorkResultSupport;
import com.opensymphony.webwork.ServletActionContext;
import com.opensymphony.xwork.ActionInvocation;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.feed.WireFeed;
import com.sun.syndication.io.WireFeedOutput;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

/**
 * <class-comment/>
 */
public class RssResult extends WebWorkResultSupport
{
    protected void doExecute(String format, ActionInvocation actionInvocation) throws Exception
    {
        SyndFeed feed = (SyndFeed) actionInvocation.getStack().findValue("feed");
        if (feed == null)
        {
            // we have a problem - the action did not generate a synd feed for
            // us to render
            throw new ServletException("Unable to find feed for this action");
        }

        // No need to create a session for RSS
        ServletActionContext.getRequest().getSession(false);
        HttpServletResponse response = ServletActionContext.getResponse();

        // rss content type.
        response.setContentType("application/rss+xml; charset=UTF-8");
        response.setHeader("Content-Disposition", "filename=rss.xml");

        // render the feed in the requested format.
        WireFeed outFeed = feed.createWireFeed(format);
        outFeed.setEncoding("UTF-8");
        new WireFeedOutput().output(outFeed, response.getWriter());
        response.flushBuffer();
    }
}
