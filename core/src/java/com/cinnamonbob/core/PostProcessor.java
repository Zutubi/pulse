package com.cinnamonbob.core;

import com.cinnamonbob.core.model.CommandResult;
import com.cinnamonbob.core.model.StoredFileArtifact;

import java.io.File;


/**
 * Post processors are used to detect interesting features in artifacts
 * (including command output) produced by commands.  For example, a post
 * processor can be used to search for compiler errors in output from a
 * build command.
 */
public interface PostProcessor extends Reference
{
    void process(File outputDir, StoredFileArtifact artifact, CommandResult result);
}
