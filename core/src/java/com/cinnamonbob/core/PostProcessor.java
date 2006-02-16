package com.cinnamonbob.core;

import com.cinnamonbob.core.model.CommandResult;
import com.cinnamonbob.core.model.StoredArtifact;

import java.io.File;


/**
 * 
 *
 */
public interface PostProcessor extends Reference
{
    void process(File outputDir, StoredArtifact artifact, CommandResult result);
}
