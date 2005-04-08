package com.cinnamonbob.core;



/**
 * A ProjectListener is able to receive notification of project events.
 */
public interface ProjectListener
{
    public void buildTriggered(Project project);
    public void buildComplete(Project project);
    
}
