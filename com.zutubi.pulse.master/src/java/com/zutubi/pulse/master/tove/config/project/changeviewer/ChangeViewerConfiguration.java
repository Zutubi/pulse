package com.zutubi.pulse.master.tove.config.project.changeviewer;

import com.zutubi.config.annotations.SymbolicName;
import com.zutubi.config.annotations.Transient;
import com.zutubi.pulse.core.config.AbstractConfiguration;
import com.zutubi.pulse.core.scm.api.Revision;

/**
 * Represents an external SCM change viewing application, such as Fisheye or
 * ViewVC.
 */
@SymbolicName("zutubi.changeViewerConfig")
public abstract class ChangeViewerConfiguration extends AbstractConfiguration
{
    public enum Capability
    {
        DOWNLOAD_FILE,
        VIEW_CHANGESET,
        VIEW_FILE,
        VIEW_FILE_DIFF,
    }

    public abstract boolean hasCapability(Capability capability);

    @Transient
    public abstract String getDetails();

    @Transient
    public abstract String getChangesetURL(Revision revision);

    @Transient
    public abstract String getFileViewURL(String path, String revision);

    @Transient
    public abstract String getFileDownloadURL(String path, String revision);

    @Transient
    public abstract String getFileDiffURL(String path, String revision);
}
