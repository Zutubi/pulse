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

package com.zutubi.pulse.master.tove.config.project.changeviewer;

import com.zutubi.pulse.core.scm.api.*;
import com.zutubi.pulse.master.tove.config.project.ProjectConfiguration;

/**
 * Context of a changelist to retrieve information about.  Convenience methods
 * are also supplied for the common operation of getting previous revisions.
 */
public interface ChangeContext
{
    /**
     * @return the revision of the changelist that this operation relates to
     */
    Revision getRevision();

    /**
     * @return the configuration of the project that created the changelist
     */
    ProjectConfiguration getProjectConfiguration();

    /**
     * @return a client for the SCM implementation that created the changelist
     */
    ScmClient getScmClient();

    /**
     * @return an SCM context that may be passed to the SCM client if required
     */
    ScmContext getScmContext();

    /**
     * Returns the revision previous to the changelist's revision, if any.
     *
     * @return the previous changelist revision, or null if there is none
     * @throws ScmException if the SCM implementation encounters an error
     */
    Revision getPreviousChangelistRevision() throws ScmException;

    /**
     * Returns the file revision previous to the given file's revision, if any.
     *
     * @param fileChange the file change to retrieve the revision from
     * @return the previous file revision, or null if there is none
     * @throws ScmException if the SCM implementation encounters an error
     */
    Revision getPreviousFileRevision(FileChange fileChange) throws ScmException;
}
