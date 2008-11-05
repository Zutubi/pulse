package com.zutubi.pulse.master.tove.config.project.changeviewer;

import com.zutubi.pulse.core.scm.api.Revision;
import com.zutubi.tove.annotations.SymbolicName;
import com.zutubi.tove.annotations.Transient;
import com.zutubi.tove.config.AbstractConfiguration;

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
         * The viewer can display a page with information about a changelist.
         */
        VIEW_CHANGELIST,
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
     * Returns the URL for a view of the given changelist.
     * <p/>
     * Required for capability {@link Capability#VIEW_CHANGELIST}
     *
     * @param revision revision of the changelist
     * @return URL for the changelist view
     */
    @Transient
    public abstract String getChangelistURL(Revision revision);

    /**
     * Returns the URL for viewing a file at a given changelist and/or file
     * revision.  Note that typically only one of the changelist or file
     * revisions will be used, but different viewers require different
     * information.
     * <p/>
     * Required for capability {@link Capability#VIEW_FILE}
     *
     * @param path               path of the file to generate a URL for, in the
     *                           format returned by the SCM plugin implementation
     *                           for file changes
     * @param changelistRevision revision of the changelist at which to view the
     *                           file
     * @param fileRevision       revision of the file to view
     * @return URL for viewing the file at the given revision
     */
    @Transient
    public abstract String getFileViewURL(String path, Revision changelistRevision, String fileRevision);

    /**
     * Returns the URL for downloading a raw file at a given changelist and/or
     * file revision.  Note that typically only one of the changelist or file
     * revisions will be used, but different viewers require different
     * information.
     * <p/>
     * Required for capability {@link Capability#DOWNLOAD_FILE}
     *
     * @param path               path of the file to generate a URL for, in the
     *                           format returned by the SCM plugin implementation
     *                           for file changes
     * @param changelistRevision revision of the changelist at which to download
     *                           the file
     * @param fileRevision       revision of the file to download
     * @return URL for downloading the file at the given revision
     */
    @Transient
    public abstract String getFileDownloadURL(String path, Revision changelistRevision, String fileRevision);

    /**
     * Returns the URL for viewing the differences applied to a file in a given
     * changelist and/or file revision.  Note that typically only one of the
     * changelist or file revisions will be used, but different viewers require
     * different information.
     * <p/>
     * Required for capability {@link Capability#VIEW_FILE_DIFF}
     *
     * @param path               path of the file to generate a URL for, in the
     *                           format returned by the SCM plugin implementation
     *                           for file changes
     * @param changelistRevision revision of the changelist in which the file
     *                           change occurred
     * @param fileRevision       revision of the file after the change
     * @return URL for viewing the file changes that occurred at the given
     *         revision
     */
    @Transient
    public abstract String getFileDiffURL(String path, Revision changelistRevision, String fileRevision);
}
