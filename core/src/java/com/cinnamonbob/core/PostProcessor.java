package com.cinnamonbob.core;

import com.cinnamonbob.core.model.CommandResult;
import com.cinnamonbob.core.model.StoredFileArtifact;
import com.cinnamonbob.core.model.StoredArtifact;

import java.io.File;


/**
 * 
 *
 */
public interface PostProcessor extends Reference
{
    void process(File outputDir, StoredFileArtifact artifact, CommandResult result);
}
