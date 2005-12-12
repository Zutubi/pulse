package com.cinnamonbob.scheduling.tasks;

import com.cinnamonbob.scheduling.Task;
import com.cinnamonbob.scheduling.TaskExecutionContext;

/**
 * <class-comment/>
 */
public class BuildProjectTask implements Task
{
    public static final String PARAM_RECIPE = "recipe";
    public static final String PARAM_PROJECT = "project";

    public void execute(TaskExecutionContext context)
    {
        String recipe = (String) context.get(PARAM_RECIPE);
        long project = (Long)context.get(PARAM_PROJECT);

        // generate build request.
        throw new RuntimeException("NYI: generate build project request.");
    }
}
