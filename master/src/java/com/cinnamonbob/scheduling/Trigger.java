package com.cinnamonbob.scheduling;

import com.cinnamonbob.core.model.Entity;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * <class-comment/>
 */
public abstract class Trigger extends Entity
{
    /**
     * The default group to be used when no group is specified.
     */
    public static final String DEFAULT_GROUP = "default";

    /**
     * The name used to identify the trigger instance.
     */
    private String name;

    /**
     * The group to which this trigger instance belongs.
     */
    private String group;

    /**
     * The state of this trigger.
     */
    private TriggerState state = TriggerState.NONE;

    /**
     * A count of the number of times this trigger has been 'triggered'.
     */
    private long triggerCount;

    /**
     * The last time this trigger was triggered, or null if this trigger has not
     * been triggered.
     */
    private Date previousTriggerTime;

    /**
     * The datamap, contains arbitrary pieces of data. This data is later made available
     * to the task being executed.
     */
    private Map dataMap;

    private Class<? extends Task> taskClass;

    private long projectId;

    /**
     * The no argument constructor should not be used directly. It is made available so
     * that triggers can be created via the use of newInstance(). A trigger should have
     * at least a name.
     *
     */
    public Trigger()
    {

    }

    /**
     * Use the specified name and the default group as the identifiers for this trigger
     * instance.
     *
     * @param name of this trigger instance. This value should not be null.
     */
    public Trigger(String name)
    {
        this(name, DEFAULT_GROUP);
    }

    /**
     * Use the specified name and group as the identifiers for this trigger instance.
     * 
     * @param name of this trigger instance. This value should not be null.
     * @param group of this trigger instance. This value should not be null.
     */
    public Trigger(String name, String group)
    {
        this.name = name;
        this.group = group;
    }

    /**
     * Return the type identifier for this type of trigger. This identifier is used to
     * locate the Scheduler Strategies available to handle this trigger.
     *
     */
    public abstract String getType();

    public Map getDataMap()
    {
        if (dataMap == null)
        {
            dataMap = new HashMap();
        }
        return dataMap;
    }

    private void setDataMap(Map map)
    {
        dataMap = map;
    }

    public TriggerState getState()
    {
        return state;
    }

    public void setState(TriggerState state)
    {
        this.state = state;
    }

    public long getTriggerCount()
    {
        return triggerCount;
    }

    private void setTriggerCount(long count)
    {
        this.triggerCount = count;
    }

    public void trigger()
    {
        triggerCount++;
        previousTriggerTime = Calendar.getInstance().getTime();
    }

    public Date getPreviousTriggerTime()
    {
        return previousTriggerTime;
    }

    private void setPreviousTriggerTime(Date time)
    {
        this.previousTriggerTime = time;
    }

    public String getName()
    {
        return name;
    }

    private void setName(String name)
    {
        this.name = name;
    }

    public String getGroup()
    {
        return group;
    }

    private void setGroup(String group)
    {
        this.group = group;
    }

    private void setTriggerState(String str)
    {
        state = TriggerState.valueOf(str);
    }

    private String getTriggerState()
    {
        return state.toString();
    }

    public void setTaskClass(Class<? extends Task> task)
    {
        this.taskClass = task;
    }

    public Class<? extends Task> getTaskClass()
    {
        return this.taskClass;
    }

    public boolean isPaused()
    {
        return TriggerState.PAUSED == getState();
    }

    public boolean isActive()
    {
        return TriggerState.ACTIVE == getState();
    }

    public boolean isScheduled()
    {
        return TriggerState.NONE != getState();
    }

    public long getProject()
    {
        return projectId;
    }

    public void setProject(long projectId)
    {
        this.projectId = projectId;
    }
}
