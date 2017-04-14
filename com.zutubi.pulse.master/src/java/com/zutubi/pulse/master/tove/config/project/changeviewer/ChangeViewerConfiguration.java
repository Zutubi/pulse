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

import com.zutubi.pulse.core.scm.api.FileChange;
import com.zutubi.pulse.core.scm.api.Revision;
import com.zutubi.pulse.core.scm.api.ScmException;
import com.zutubi.pulse.master.tove.config.project.ProjectConfiguration;
import com.zutubi.tove.annotations.SymbolicName;
import com.zutubi.tove.annotations.Transient;
import com.zutubi.tove.config.api.AbstractConfiguration;

/**
 * Represents an external SCM change viewing application, such as Fisheye or
 * ViewVC.
 */
@SymbolicName("zutubi.changeViewerConfig")
public abstract class ChangeViewerConfiguration extends AbstractConfiguration
{
    /**
     * Capabilities that a change viewer may have.  See method documentation to
     * determine which capabilities correspond to which methods.
     */
    public enum Capability
    {
        /**
         * The viewer supports downloading raw file contents, possibly at a
         * given revision.
         */
        DOWNLOAD_FILE,
        /**
         * The viewer can display a page with information about a revision.
         */
        VIEW_REVISION,
        /**
         * The viewer can display a file, possily at a given revision.
         */
        VIEW_FILE,
        /**
         * The viewer can display the diff between two revisions of a file.
         */
        VIEW_FILE_DIFF,
    }

    /**
     * Indicates if the change viewer supports a given capability.  Not all
     * viewers will support all methods on this interface -- indicating this
     * via capabilities allows Pulse to avoid asking the viewer for details it
     * does not support.
     *
     * @param capability the capability to check for
     * @return true if this viewer supports the given capability
     */
    public abstract boolean hasCapability(Capability capability);

    /**
     * Returns the URL for a view of the given revision.  Note that no context
     * is provided in this case as we are not necessarily operating under the
     * context of one change.
     * <p/>
     * Required for capability {@link Capability#VIEW_REVISION}
     *
     *
     * @param projectConfiguration
     * @param revision revision of the changelist
     * @return URL for the changelist view
     */
    @Transient
    public abstract String getRevisionURL(ProjectConfiguration projectConfiguration, Revision revision);

    /**
     * Returns the URL for viewing a file at a given changelist and/or file
     * revision.
     * <p/>
     * Required for capability {@link Capability#VIEW_FILE}
     *
     * @param context    context of the change in which the file participated,
     *                   including the changelist of which it was part
     * @param fileChange information about the file to retrieve the URL for,
     *                   most notably the path and file revision
     * @return URL for viewing the file at the given revision
     * @throws com.zutubi.pulse.core.scm.api.ScmException if there is an error
     *         retrieving further information from the SCM implementation
     */
    @Transient
    public abstract String getFileViewURL(ChangeContext context, FileChange fileChange) throws ScmException;

    /**
     * Returns the URL for downloading a raw file at a given changelist and/or
     * file revision.
     * <p/>
     * Required for capability {@link Capability#DOWNLOAD_FILE}
     *
     * @param context    context of the change in which the file participated,
     *                   including the changelist of which it was part
     * @param fileChange information about the file to retrieve the URL for,
     *                   most notably the path and file revision
     * @return URL for downloading the file at the given revision
     * @throws com.zutubi.pulse.core.scm.api.ScmException if there is an error
     *         retrieving further information from the SCM implementation
     */
    @Transient
    public abstract String getFileDownloadURL(ChangeContext context, FileChange fileChange) throws ScmException;

    /**
     * Returns the URL for viewing the differences applied to a file in a given
     * changelist and/or file revision.
     * <p/>
     * Required for capability {@link Capability#VIEW_FILE_DIFF}
     *
     * @param context    context of the change in which the file participated,
     *                   including the changelist of which it was part
     * @param fileChange information about the file to retrieve the URL for,
     *                   most notably the path and file revision
     * @return URL for viewing the file changes that occurred at the given
     *         revision
     * @throws com.zutubi.pulse.core.scm.api.ScmException if there is an error
     *         retrieving further information from the SCM implementation
     */
    @Transient
    public abstract String getFileDiffURL(ChangeContext context, FileChange fileChange) throws ScmException;
}
