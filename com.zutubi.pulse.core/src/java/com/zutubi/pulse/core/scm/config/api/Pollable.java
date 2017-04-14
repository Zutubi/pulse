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

package com.zutubi.pulse.core.scm.config.api;

import java.util.List;

/**
 * Interface that must be implemented by SCM configurations when the SCM
 * claims capability {@link com.zutubi.pulse.core.scm.api.ScmCapability#POLL}.
 */
public interface Pollable
{
    /**
     * Indicates if the SCM should be monitored for new changes.  Note that
     * this alone is not sufficient to trigger builds when new changes arrive
     * (it means changes will be detected, but a trigger is needed to launch a
     * build based on this detection).
     *
     * @return true if the SCM should be monitored for changes, false to
     *         disable monitoring
     */
    boolean isMonitor();

    /**
     * Indicates if the SCM should use the server-wide polling interval setting
     * or its own local setting.  This is irrelevant when monitoring is
     * disabled.
     *
     * @return true if the SCM should use its own local interval setting, false
     *         if it should use the server-wide setting
     *
     * @see #getPollingInterval()
     */
    boolean isCustomPollingInterval();

    /**
     * The minimum amount of time, in minutes, between successive polls of the
     * SCM for new changes.  Only used when monitoring is enabled, with a
     * custom polling interval.
     *
     * @return the minimum number of minutes between polls of the SCM
     *
     * @see #isCustomPollingInterval()
     */
    int getPollingInterval();

    /**
     * Indicates if a quiet period should be observed before turning detection
     * of a new change into a published event.
     *
     * @return true if a quiet period should be required before raising events
     *
     * @see #getQuietPeriod()
     */
    boolean isQuietPeriodEnabled();

    /**
     * The minimum period of inactivity, in minutes, required after detecting a
     * change before raising an event for that change.  This is primarily
     * useful for SCMs that do not support atomic commits, where a change may
     * be detected mid-commit.  Requiring a period of inactivity in practice
     * ensures that the commit will be complete before an event is raised to
     * indicate that there is a new change.
     *
     * @return the number of minutes of inactivity to require before raising an
     *         event about a new change
     *
     * @see #isQuietPeriodEnabled()
     */
    int getQuietPeriod();

    /**
     * A collection of file patterns used as inclusion filters for files
     * reported in changelists.  If this list is empty all files pass the
     * inclusion test.  Otherwise, only files that match at least one of the
     * patterns pass the filter.  Changelists that have all files filtered out
     * are ignored.
     *
     * The patterns support Ant-style globbing.
     *
     * @return a list of inclusion filters for files in changelists, may be
     *         empty to pass all files
     *
     * @see #getExcludedPaths()
     */
    List<String> getIncludedPaths();

    /**
     * A collection of file patterns used as exclusion filters for files
     * reported in changelists.  Only files that match none of the patterns
     * pass the filter.  Changelists that have all files filtered out are
     * ignored.
     *
     * The patterns support Ant-style globbing.
     *
     * @return a list of exclusion filters for files in changelists
     *
     * @see #getIncludedPaths()
     */
    List<String> getExcludedPaths();
}
