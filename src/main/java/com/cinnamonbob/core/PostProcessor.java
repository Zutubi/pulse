package com.cinnamonbob.core;

import com.cinnamonbob.model.StoredArtifact;


/**
 * 
 *
 */
public interface PostProcessor extends Reference
{
    void process(StoredArtifact a);
}
