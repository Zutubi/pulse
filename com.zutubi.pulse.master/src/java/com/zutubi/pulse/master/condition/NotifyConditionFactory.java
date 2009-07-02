package com.zutubi.pulse.master.condition;

import com.zutubi.util.bean.DefaultObjectFactory;
import com.zutubi.util.bean.ObjectFactory;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 */
public class NotifyConditionFactory
{
    // Primitive conditions
    public static final String CHANGED = "changed";
    public static final String CHANGED_BY_ME = "changed.by.me";
    public static final String CHANGED_BY_ME_SINCE_SUCCESS = "changed.by.me.since.success";
    public static final String ERROR = "error";
    public static final String FAILURE = "failure";
    public static final String FALSE = "false";
    public static final String SUCCESS = "success";
    public static final String STATE_CHANGE = "state.change";
    public static final String TRUE = "true";

    // Integer values
    public static final String UNSUCCESSFUL_COUNT_BUILDS = "unsuccessful.count.builds";
    public static final String UNSUCCESSFUL_COUNT_DAYS = "unsuccessful.count.days";

    // String values
    public static final String BUILD_SPECIFICATION_NAME = "build.specification.name";

    private final static Map<String, Class> typeMap = new HashMap<String, Class>();

    private ObjectFactory objectFactory = new DefaultObjectFactory();

    static
    {
        // initialise the default notification types.
        typeMap.put(CHANGED, ChangedNotifyCondition.class);
        typeMap.put(CHANGED_BY_ME, ChangedByMeNotifyCondition.class);
        typeMap.put(CHANGED_BY_ME_SINCE_SUCCESS, ChangedByMeSinceSuccessNotifyCondition.class);
        typeMap.put(ERROR, ErrorNotifyCondition.class);
        typeMap.put(FAILURE, FailureNotifyCondition.class);
        typeMap.put(FALSE, FalseNotifyCondition.class);
        typeMap.put(SUCCESS, SuccessNotifyCondition.class);
        typeMap.put(STATE_CHANGE, StateChangeNotifyCondition.class);
        typeMap.put(TRUE, TrueNotifyCondition.class);

        typeMap.put(UNSUCCESSFUL_COUNT_BUILDS, UnsuccessfulCountBuildsValue.class);
        typeMap.put(UNSUCCESSFUL_COUNT_DAYS, UnsuccessfulCountDaysValue.class);
    }

    public List<String> getAvailableConditions()
    {
        return new LinkedList<String>(typeMap.keySet());
    }

    public boolean isValid(String key, Class clazz)
    {
        Class foundClass = typeMap.get(key);
        return foundClass != null && clazz.isAssignableFrom(foundClass);
    }

    public void setObjectFactory(ObjectFactory objectFactory)
    {
        this.objectFactory = objectFactory;
    }

    public NotifyCondition createCondition(String condition)
    {
        return create(condition, NotifyCondition.class);
    }

    public NotifyIntegerValue createIntegerValue(String value)
    {
        return create(value, NotifyIntegerValue.class);
    }

    private <T> T create(String token, Class<T> clazz)
    {
        if (!isValid(token, clazz))
        {
            throw new IllegalArgumentException("Invalid token '" + token + "' specified.");
        }
        Class definition = typeMap.get(token);
        return (T) objectFactory.buildBean(definition);
    }

    public <T> T build(Class<T> clazz, Class[] argTypes, Object[] args)
    {
        return objectFactory.buildBean(clazz, argTypes, args);
    }
}
