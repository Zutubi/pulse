/* Copyright 2017 Zutubi Pty Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
