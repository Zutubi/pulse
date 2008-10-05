package com.zutubi.pulse.master.vfs.provider.pulse;

import com.zutubi.pulse.core.model.StoredFileArtifact;
import org.apache.commons.vfs.FileSystemException;

import java.io.File;

/**
 * A provider interface that indicates the current node represents a stored file artifact instance.
 *
 * @see com.zutubi.pulse.core.model.StoredFileArtifact
 */
public interface FileArtifactProvider
{
    StoredFileArtifact getFileArtifact() throws FileSystemException;

    long getFileArtifactId() throws FileSystemException;

    File getFile();
}
