package com.cinnamonbob.core;

import com.cinnamonbob.core.model.StoredArtifact;


/**
 * 
 *
 */
public interface PostProcessor extends Reference
{
    void process(StoredArtifact a);
}
