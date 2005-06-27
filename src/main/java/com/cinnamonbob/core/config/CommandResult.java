package com.cinnamonbob.core.config;

import java.util.List;

/**
 * 
 *
 */
public interface CommandResult
{
    boolean succeeded();
    
    List<Artifact> getArtifacts();
    
    
}
