package com.cinnamonbob.scheduling;

/**
 * <class-comment/>
 */
public class NoopTrigger extends Trigger
{
    protected static final String TYPE = "noop";

    public NoopTrigger()
    {
    }

    public NoopTrigger(String name)
    {
        this(name, DEFAULT_GROUP);
    }

    public NoopTrigger(String name, String group)
    {
        super(name, group);
    }

    public String getType()
    {
        return TYPE;
    }
}
