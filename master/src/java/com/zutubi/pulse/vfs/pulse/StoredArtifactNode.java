package com.zutubi.pulse.vfs.pulse;

import com.zutubi.pulse.core.model.StoredArtifact;

/**
 * <class comment/>
 */
public interface StoredArtifactNode
{
    StoredArtifact getArtifact();

    long getArtifactId();
}
