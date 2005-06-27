package com.cinnamonbob.core2.config;

/**
 * 
 *
 */
public interface PostProcessor extends Reference
{
    void process(Artifact a);
}
