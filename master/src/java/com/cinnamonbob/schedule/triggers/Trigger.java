package com.cinnamonbob.schedule.triggers;

import com.cinnamonbob.core.model.Entity;
import com.cinnamonbob.schedule.ScheduleManager;
import com.cinnamonbob.schedule.SchedulingException;
import com.cinnamonbob.schedule.Schedule;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * The base trigger type.
 */
public abstract class Trigger extends Entity
{
    private Schedule schedule;

    /**
     * The state of this trigger.
     */
    private TriggerState state = TriggerState.NONE;

    /**
     * The count of the number of times this trigger has been activated.
     */
    private long triggerCount = 0;

    /**
     * The record of the last time this trigger was activated.
     */
    private Date lastTriggerTime = null;

    private String name;
    private String group;

    /**
     * The properties map supports arbitrary serializable objects to be stored in the persistent state of the object.
     */
    private Map<String, Serializable> properties;

    public Map<String, Serializable> getProperties()
    {
        if (properties == null)
        {
            properties = new HashMap<String, Serializable>();
        }
        return properties;
    }

    // required by hibernate
    private void setProperties(Map map)
    {
        this.properties = map;
    }

    public long getTriggerCount()
    {
        return triggerCount;
    }

    // required by hibernate
    private void setTriggerCount(long triggerCount)
    {
        this.triggerCount = triggerCount;
    }

    public void setSchedule(Schedule schedule)
    {
        this.schedule = schedule;
    }

    public Schedule getSchedule()
    {
        return this.schedule;
    }

    public Date getLastTriggerTime()
    {
        return lastTriggerTime;
    }

    // required by hibernate
    private void setLastTriggerTime(Date lastTriggerTime)
    {
        this.lastTriggerTime = lastTriggerTime;
    }

    // required by hibernate
    private TriggerState getState()
    {
        return state;
    }

    private void setState(TriggerState state)
    {
        this.state = state;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getName()
    {
        return this.name;
    }

    public void setGroup(String group)
    {
        this.group = group;
    }

    public String getGroup()
    {
        return this.group;
    }

    public void trigger()
    {
        triggerCount++;
        lastTriggerTime = Calendar.getInstance().getTime();

        // execute associated task.
        schedule.getTask().execute();
    }

    public abstract void rehydrate() throws SchedulingException;

    protected abstract void internalPause() throws SchedulingException;

    protected  abstract void internalResume() throws SchedulingException;

    protected  abstract void internalComplete() throws SchedulingException;

    protected  abstract void internalActivate() throws SchedulingException;

    public boolean isPaused()
    {
        return state == TriggerState.PAUSED;
    }

    public void pause() throws SchedulingException
    {
        if (isPaused())
        {
            return;
        }
        internalPause();
        setState(TriggerState.PAUSED);
    }

    public boolean isComplete()
    {
        return state == TriggerState.COMPLETE;
    }

    public void complete() throws SchedulingException
    {
        if (isComplete())
        {
            return;
        }
        internalComplete();
        setState(TriggerState.COMPLETE);
    }

    public boolean isActive()
    {
        return state == TriggerState.ACTIVE;
    }

    public void activate() throws SchedulingException
    {
        if (!isNone())
        {
            return;
        }
        internalActivate();
        setState(TriggerState.ACTIVE);
    }

    public boolean isNone()
    {
        return state == TriggerState.NONE;
    }

}
