package com.zutubi.pulse.master.webwork.dispatcher;

import com.sun.syndication.feed.WireFeed;

import java.util.Date;

/**
 * An interface representing a 'Just In Time Feed'.  Any actions that
 * intend to use the {@link com.zutubi.pulse.master.webwork.dispatcher.JITFeedResult} need to
 * ensure that the feed data implements this interface.
 */
public interface JITFeed
{
    /**
     * Returns true if this feed contains any entries.
     *
     * @return true if the feed has entries, false otherwise.
     */
    boolean hasEntries();

    /**
     * Returns the date at which the most recent entry was added to this
     * feed.
     *
     * @return  the publish date of the most recent entry.
     */
    Date getPublishedDate();

    /**
     * Returns the date at which the last change was made to an entry in
     * this feed.
     *
     * @return the update date of the most recently updated entry.
     */
    Date getUpdatedDate();

    /**
     * Create the wire feed in the given format.
     *
     * @param format
     *
     * @return instance of the feed
     *
     * @see com.sun.syndication.feed.WireFeed
     */
    WireFeed createWireFeed(String format);
}
