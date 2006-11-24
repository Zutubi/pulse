package com.zutubi.pulse.vfs.pulse;

import com.zutubi.pulse.core.model.StoredFileArtifact;
import org.apache.commons.vfs.FileSystemException;

import java.io.File;

/**
 * <class comment/>
 */
public interface StoredFileArtifactNode
{
    StoredFileArtifact getFileArtifact() throws FileSystemException;

    File getFile();
}
