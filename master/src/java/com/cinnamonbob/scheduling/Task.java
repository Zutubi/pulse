package com.cinnamonbob.scheduling;

import com.cinnamonbob.core.model.Entity;

import java.util.Map;
import java.util.HashMap;

/**
 * <class-comment/>
 */
public abstract class Task extends Entity
{
    public static final String DEFAULT_GROUP = "default";

    /**
     * The name used to identify this task instance.
     */
    private String name;

    /**
     * The group to which this task instance belongs.
     */
    private String group;

    /**
     * The datamap is used to store arbitrary serializable data.
     */
    private Map dataMap;

    /**
     * The no argument constructor should not be used directly. It is made available so
     * that tasks can be created via the use of newInstance(). A task should have
     * at least a name.
     *
     */
    public Task()
    {

    }

    /**
     * Use the specified name and the default group as the identifiers for this task
     * instance.
     *
     * @param name of this task instance. This value should not be null.
     */
    public Task(String name)
    {
        this(name, null);
    }

    /**
     * Use the specified name and the group as the identifiers for this task
     * instance.
     *
     * @param name of this task instance. This value should not be null.
     * @param group of this task instance. This value should not be null.
     */
    public Task(String name, String group)
    {
        this.name = name;
        this.group = group;
    }

    /**
     * The execute method should be implemented to handle the logic for handling a particular
     * task. It is the execute method that is called when a trigger is triggered.
     *
     * @param context is a task execution context configured with the context of this execution
     * such as the trigger that triggered resulting in the execution of this action.
     */
    public abstract void execute(TaskExecutionContext context);

    /**
     * Getter for this tasks dataMap. The dataMap is used to provide arbitrary key:value pairs
     * to this task during its execution.
     *
     * @return the data map.
     */
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
        this.dataMap = map;
    }

    /**
     * Getter for the group property.
     *
     * @return the group identifier for this task.
     */
    public String getGroup()
    {
        return group;
    }

    private void setGroup(String group)
    {
        this.group = group;
    }

    /**
     * Getter for the name property.
     *
     * @return the name identifier for this task.
     */
    public String getName()
    {
        return name;
    }

    private void setName(String name)
    {
        this.name = name;
    }
}
