package com.zutubi.pulse.master.vfs.provider.pulse;

import com.zutubi.pulse.core.model.StoredArtifact;
import org.apache.commons.vfs.FileSystemException;

/**
 * A provider interface that indicates the current node represents a StoredArtifact instance.
 * 
 * @see com.zutubi.pulse.core.model.StoredArtifact
 */
public interface ArtifactProvider
{
    StoredArtifact getArtifact() throws FileSystemException;

    long getArtifactId() throws FileSystemException;
}
