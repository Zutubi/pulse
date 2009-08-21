package com.zutubi.pulse.master.scheduling;

import com.zutubi.pulse.core.model.Entity;
import com.zutubi.pulse.core.model.NamedEntity;
import com.zutubi.pulse.master.tove.config.project.triggers.TriggerConfiguration;
import com.zutubi.tove.annotations.Transient;

import java.util.Calendar;
import java.util.Date;

/**
 * <class-comment/>
 */
public abstract class Trigger extends Entity implements NamedEntity
{
    /**
     * The default group to be used when no group is specified.
     */
    public static final String DEFAULT_GROUP = "default";

    /**
     * The name used to identify the trigger instance. Along with the group, the
     * name uniquely identifies a trigger.
     */
    private String name;

    /**
     * The group to which this trigger instance belongs. Along with the name, the
     * group uniquely identifies a trigger.
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

    private Class<? extends Task> taskClass;

    private long projectId;

    /**
     * Contains extra configuration.  Only project build triggers have this
     * config so far.  Further refactoring may expand this usage.
     */
    private TriggerConfiguration config;

    /**
     * The no argument constructor should not be used directly. It is made available so
     * that triggers can be created via the use of newInstance(). A trigger should have
     * at least a name.
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
     * @param name  of this trigger instance. This value should not be null.
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
     */
    @Transient
    public abstract String getType();

    public TriggerState getState()
    {
        return state;
    }

    void setState(TriggerState state)
    {
        this.state = state;
    }

    public long getTriggerCount()
    {
        return triggerCount;
    }

    /**
     * Used by hibernate.
     */
    private void setTriggerCount(long count)
    {
        this.triggerCount = count;
    }

    public void fire()
    {
        triggerCount++;
        previousTriggerTime = Calendar.getInstance().getTime();
    }

    public Date getPreviousTriggerTime()
    {
        return previousTriggerTime;
    }

    /**
     * Used by hibernate.
     */
    private void setPreviousTriggerTime(Date time)
    {
        this.previousTriggerTime = time;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getGroup()
    {
        return group;
    }

    /**
     * Used by hibernate.
     */
    public void setGroup(String group)
    {
        this.group = group;
    }

    /**
     * Used by hibernate.
     */
    private void setTriggerState(String str)
    {
        state = TriggerState.valueOf(str);
    }

    /**
     * Used by hibernate.
     */
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
        return TriggerState.SCHEDULED == getState();
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

    public long getProjectId()
    {
        return projectId;
    }

    public void setProjectId(long projectId)
    {
        this.projectId = projectId;
    }

    public TriggerConfiguration getConfig()
    {
        return config;
    }

    public void setConfig(TriggerConfiguration config)
    {
        this.config = config;
    }
}
