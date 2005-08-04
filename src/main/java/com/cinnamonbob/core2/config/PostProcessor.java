package com.cinnamonbob.core2.config;

import com.cinnamonbob.model.StoredArtifact;


/**
 * 
 *
 */
public interface PostProcessor extends Reference
{
    void process(StoredArtifact a);
}
