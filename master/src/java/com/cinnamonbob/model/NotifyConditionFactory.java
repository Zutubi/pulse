package com.zutubi.pulse.model;

import com.zutubi.pulse.core.BobRuntimeException;
import com.zutubi.pulse.core.ObjectFactory;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * <class-comment/>
 */
public class NotifyConditionFactory
{
    public static final String ALL_BUILDS = "all builds";
    public static final String ALL_CHANGED = "all changed";
    public static final String ALL_FAILED = "all failed";
    public static final String ALL_CHANGED_OR_FAILED = "all changed or failed";
    public static final String ALL_FAILED_AND_FIRST_SUCCESS = "all failed and first success";

    private final static Map<String, Class> typeMap = new HashMap<String, Class>();

    private ObjectFactory objectFactory;

    static
    {
        // initialise the default notification types.
        typeMap.put(ALL_BUILDS, TrueNotifyCondition.class);
        typeMap.put(ALL_CHANGED, ChangedNotifyCondition.class);
        typeMap.put(ALL_FAILED, FailedNotifyCondition.class);
        typeMap.put(ALL_CHANGED_OR_FAILED, ChangedOrFailedNotifyCondition.class);
        typeMap.put(ALL_FAILED_AND_FIRST_SUCCESS, FailedAndFirstSuccessNotifyCondition.class);
    }

    public List<String> getAvailableConditions()
    {
        return new LinkedList<String>(typeMap.keySet());
    }

    public boolean isValid(String key)
    {
        return typeMap.containsKey(key);
    }

    public void setObjectFactory(ObjectFactory objectFactory)
    {
        this.objectFactory = objectFactory;
    }

    public NotifyCondition createCondition(String condition)
    {
        if (!isValid(condition))
        {
            throw new IllegalArgumentException("invalid condition '" + condition + "' specified.");
        }
        Class definition = typeMap.get(condition);

        try
        {
            return objectFactory.buildBean(definition);
        }
        catch (Exception e)
        {
            throw new BobRuntimeException(e);
        }
    }
}
