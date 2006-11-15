package com.zutubi.pulse.xwork.results;

import com.sun.syndication.feed.synd.SyndFeed;

import java.util.Date;

/**
 * <class comment/>
 */
public interface JITFeed
{
    boolean hasEntries();

    Date getPublishedDate();

    Date getUpdatedDate();

    SyndFeed createFeed();
}
