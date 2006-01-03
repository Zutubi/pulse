package com.cinnamonbob.scheduling;

/**
 * <class-comment/>
 */
public class SimpleTrigger extends Trigger
{
    protected static final String TYPE = "simple";

    private static final String INTERVAL = "interval";
    private static final String REPEAT = "repeat";

    public static final int REPEAT_INDEFINITELY = org.quartz.SimpleTrigger.REPEAT_INDEFINITELY;

    public SimpleTrigger()
    {

    }

    public SimpleTrigger(String name, String group, long interval)
    {
        this(name, group, interval, REPEAT_INDEFINITELY);
    }

    /**
     *
     * @param name
     * @param group
     * @param interval in milliseconds between successive triggers.
     * @param repeatCount
     */
    public SimpleTrigger(String name, String group, long interval, int repeatCount)
    {
        super(name, group);
        setInterval(interval);
        setRepeatCount(repeatCount);
    }

    public String getType()
    {
        return TYPE;
    }

    private void setInterval(long interval)
    {
        getDataMap().put(INTERVAL, interval);
    }

    public long getInterval()
    {
        return (Long)getDataMap().get(INTERVAL);
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
