package com.cinnamonbob.scheduling;

/**
 * <class-comment/>
 */
public class SimpleTrigger extends Trigger
{
    protected static final String TYPE = "simple";

    private static final String INTERVAL = "interval";
    private static final String REPEAT = "repeat";

    public SimpleTrigger()
    {

    }

    public SimpleTrigger(String name, String group, int interval, int repeatCount)
    {
        super(name, group);
        setInterval(interval);
        setRepeatCount(repeatCount);
    }

    public String getType()
    {
        return TYPE;
    }

    private void setInterval(int interval)
    {
        getDataMap().put(INTERVAL, interval);
    }

    public int getInterval()
    {
        return (Integer)getDataMap().get(INTERVAL);
    }

    private void setRepeatCount(int repeat)
    {
        getDataMap().put(REPEAT, repeat);
    }

    public int getRepeatCount()
    {
        return (Integer)getDataMap().get(REPEAT);
    }
}
