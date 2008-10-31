package com.zutubi.pulse.master.webwork.dispatcher;

import com.sun.syndication.feed.WireFeed;

import java.util.Date;

/**
 * <class comment/>
 */
public interface JITFeed
{
    boolean hasEntries();

    Date getPublishedDate();

    Date getUpdatedDate();

    WireFeed createWireFeed(String format);
}
