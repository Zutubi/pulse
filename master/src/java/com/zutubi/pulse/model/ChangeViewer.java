package com.zutubi.pulse.model;

import com.zutubi.pulse.core.model.Revision;
import com.zutubi.pulse.core.model.Entity;
import com.zutubi.pulse.core.model.FileRevision;

/**
 * Represents an external SCM change viewing application, such as Fisheye or
 * ViewVC.
 */
public abstract class ChangeViewer extends Entity
{
    public enum Capability
    {
        DOWNLOAD_FILE,
        VIEW_CHANGESET,
        VIEW_FILE,
        VIEW_FILE_DIFF,
    }

    public abstract boolean hasCapability(Scm scm, Capability capability);

    public abstract String getDetails();

    public abstract String getChangesetURL(Revision revision);
    public abstract String getFileViewURL(String path, FileRevision revision);
    public abstract String getFileDownloadURL(String path, FileRevision revision);
    public abstract String getFileDiffURL(String path, FileRevision revision);

    public abstract ChangeViewer copy();
}
