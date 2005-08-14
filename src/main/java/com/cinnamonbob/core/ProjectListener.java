package com.cinnamonbob.core;

/**
 * 
 *
 */
public interface ProjectListener
{
    public void buildTriggered(BobFile project);
    public void buildComplete(BobFile project);
}
 
