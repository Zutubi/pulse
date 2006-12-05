package com.zutubi.pulse.vfs.pulse;

import com.zutubi.pulse.core.model.StoredArtifact;

/**
 * A provider interface that indicates the current node represents a StoredArtifact instance.
 * 
 * @see com.zutubi.pulse.core.model.StoredArtifact
 */
public interface ArtifactProvider
{
    StoredArtifact getArtifact();

    long getArtifactId();
}
