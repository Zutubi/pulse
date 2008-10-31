package com.zutubi.pulse.master.scheduling;

/**
 * <class-comment/>
 */
public class NoopTask implements Task
{
    public void execute(TaskExecutionContext context)
    {
        // noop
    }
}
