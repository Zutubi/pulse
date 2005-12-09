package com.cinnamonbob.scheduling;

/**
 * <class-comment/>
 */
public class NoopTask extends Task
{
    public NoopTask()
    {
        
    }

    public NoopTask(String name)
    {
        this(name, DEFAULT_GROUP);
    }

    public NoopTask(String name, String group)
    {
        super(name, group);
    }

    public void execute(TaskExecutionContext context)
    {
        // noop
    }
}
