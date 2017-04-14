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

package com.zutubi.pulse.core.scm.api;

/**
 * SCM capabilities are used to indicate which of the optional operations an SCM
 * implementation supports.  The presence or absence of these capabilities will enable / disable
 * various features within pulse.
 */
public enum ScmCapability
{
    /**
     * This defines whether or not the contents of the SCM repository can be browsed / navigated.
     * This involves being able obtain a listing of the repository directories.
     */
    BROWSE,

    /**
     * The SCM supports the concept of revisions including the ability to get
     * the latest revision and checkout at a specified revision.
     *
     * Required for the 'changelist isolation' build option to take effect.
     */
    REVISIONS,

    /**
     * Defines whether the SCM supports defining the changes that have occurred
     * at each revision.  This is used by pulse to provide extra information
     * about what changes have occurred in a build.
     *
     * SCMs with this capability must also support {@link #REVISIONS}.
     */
    CHANGESETS,

    /**
     * Indicates that the SCM supports polling for new changes.
     *
     * SCMs with this capability must also support {@link #REVISIONS}.
     *
     * SCMs supporting this capability must have a configuration class which
     * implements {@link com.zutubi.pulse.core.scm.config.api.Pollable}.
     */
    POLL,

    /**
     * This scm capability defines whether or not tagging of the scm server is supported.  Tagging of
     * the scm server is analogous to adding a persistent label to a revision on the scm server so that
     * later you can retrieve that revision using the label.  This is typically done as a post build
     * step.
     */
    TAG,

    /**
     * Indicates that the SCM can map from SCM use names in changes to email addresses.  A few SCMs
     * have user accounts which contain this information, which may be reused by Pulse on occasion.
     */
    EMAIL
}
