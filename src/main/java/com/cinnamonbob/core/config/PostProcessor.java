package com.cinnamonbob.core.config;

/**
 * 
 *
 */
public interface PostProcessor extends Reference
{
    void process(Artifact a);
}
