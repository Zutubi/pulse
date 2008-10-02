package com.zutubi.pulse.master.scheduling;

import java.util.Date;

/**
 * <class-comment/>
 */
public class SimpleTrigger extends Trigger
{
    protected static final String TYPE = "simple";

    private static final String INTERVAL = "interval";
    private static final String REPEAT = "repeat";
    private static final String START_TIME = "start";

    public static final int REPEAT_INDEFINITELY = org.quartz.SimpleTrigger.REPEAT_INDEFINITELY;

    public SimpleTrigger()
    {

    }

    public SimpleTrigger(String name, String group, long interval)
    {
        this(name, group, new Date(), interval, REPEAT_INDEFINITELY);
    }

    /**
     * Creates a trigger that will fire once at the given time.
     *
     * @param name  trigger name (must be unique)
     * @param group trigger group name
     * @param time  the time at which the trigger should fire
     */
    public SimpleTrigger(String name, String group, Date time)
    {
        this(name, group, time, 0, 0);
    }

    /**
     * @param name
     * @param group
     * @param startTime   time at which to start firing
     * @param interval    in milliseconds between successive triggers.
     * @param repeatCount
     */
    public SimpleTrigger(String name, String group, Date startTime, long interval, int repeatCount)
    {
        super(name, group);
        setInterval(interval);
        setRepeatCount(repeatCount);
        setStartTime(startTime);
    }

    public String getType()
    {
        return TYPE;
    }

    public Date getStartTime()
    {
        return (Date) getDataMap().get(START_TIME);
    }

    private void setStartTime(Date time)
    {
        getDataMap().put(START_TIME, time);
    }

    private void setInterval(long interval)
    {
        getDataMap().put(INTERVAL, interval);
    }

    public long getInterval()
    {
        return (Long) getDataMap().get(INTERVAL);
    }

    private void setRepeatCount(int repeat)
    {
        getDataMap().put(REPEAT, repeat);
    }

    public int getRepeatCount()
    {
        return (Integer) getDataMap().get(REPEAT);
    }
}
