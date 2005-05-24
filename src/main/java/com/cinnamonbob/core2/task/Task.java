package com.cinnamonbob.core2.task;

import com.cinnamonbob.core2.Project;

/**
 * 
 *
 */
public interface Task
{
    void setProject(Project project);
    void execute();
}
