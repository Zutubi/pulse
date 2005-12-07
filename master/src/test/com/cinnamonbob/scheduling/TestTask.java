package com.cinnamonbob.scheduling;

/**
 * <class-comment/>
 */
public class TestTask extends Task
{
    private boolean executed = false;

    public TestTask(String name, String group)
    {
        super(name, group);
    }

    public void execute(TaskExecutionContext context)
    {
        if (context == null)
        {
            throw new NullPointerException("TaskExecutionContext is null.");
        }
        if (context.getTrigger() == null)
        {
            throw new NullPointerException("Trigger is null.");
        }
        this.executed = true;
    }

    public boolean isExecuted()
    {
        return executed;
    }

    public void reset()
    {
        executed = false;
    }
}
