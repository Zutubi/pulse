package com.cinnamonbob.scheduling;

import com.cinnamonbob.core.model.Entity;

import java.util.Map;
import java.util.HashMap;

/**
 * <class-comment/>
 */
public interface Task
{
    /**
     * The execute method should be implemented to handle the logic for handling a particular
     * task. It is the execute method that is called when a trigger is triggered.
     *
     * @param context is a task execution context configured with the context of this execution
     * such as the trigger that triggered resulting in the execution of this action.
     */
    public void execute(TaskExecutionContext context);

}
