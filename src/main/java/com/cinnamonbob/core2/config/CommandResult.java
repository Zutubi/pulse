package com.cinnamonbob.core2.config;

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
