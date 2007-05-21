package com.zutubi.pulse.prototype.config.changeviewer;

import com.zutubi.config.annotations.SymbolicName;
import com.zutubi.pulse.core.config.AbstractNamedConfiguration;
import com.zutubi.pulse.core.model.FileRevision;
import com.zutubi.pulse.core.model.Revision;
import com.zutubi.pulse.servercore.config.ScmConfiguration;

/**
 * Represents an external SCM change viewing application, such as Fisheye or
 * ViewVC.
 */
@SymbolicName("internal.changeViewerConfig")
public abstract class ChangeViewerConfiguration extends AbstractNamedConfiguration
{
    public enum Capability
    {
        DOWNLOAD_FILE,
        VIEW_CHANGESET,
        VIEW_FILE,
        VIEW_FILE_DIFF,
    }

    public abstract boolean hasCapability(ScmConfiguration scm, Capability capability);

    public abstract String getDetails();

    public abstract String getChangesetURL(Revision revision);
    public abstract String getFileViewURL(String path, FileRevision revision);
    public abstract String getFileDownloadURL(String path, FileRevision revision);
    public abstract String getFileDiffURL(String path, FileRevision revision);
}
