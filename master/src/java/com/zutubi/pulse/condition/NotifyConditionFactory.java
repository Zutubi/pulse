/********************************************************************************
 @COPYRIGHT@
 ********************************************************************************/
package com.zutubi.pulse.condition;

import com.zutubi.pulse.core.PulseRuntimeException;
import com.zutubi.pulse.core.ObjectFactory;
import com.zutubi.pulse.condition.NotifyCondition;
import com.zutubi.pulse.condition.TrueNotifyCondition;
import com.zutubi.pulse.condition.SuccessNotifyCondition;
import com.zutubi.pulse.condition.StateChangeNotifyCondition;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * <class-comment/>
 */
public class NotifyConditionFactory
{
    // Primitive conditions
    public static final String CHANGED = "changed";
    public static final String CHANGED_BY_ME = "changed.by.me";
    public static final String ERROR = "error";
    public static final String FAILURE = "failure";
    public static final String FALSE = "false";
    public static final String SUCCESS = "success";
    public static final String STATE_CHANGE = "state.change";
    public static final String TRUE = "true";

    private final static Map<String, Class> typeMap = new HashMap<String, Class>();

    private ObjectFactory objectFactory;

    static
    {
        // initialise the default notification types.
        typeMap.put(CHANGED, ChangedNotifyCondition.class);
        typeMap.put(CHANGED_BY_ME, ChangedByMeNotifyCondition.class);
        typeMap.put(ERROR, ErrorNotifyCondition.class);
        typeMap.put(FAILURE, FailureNotifyCondition.class);
        typeMap.put(FALSE, FalseNotifyCondition.class);
        typeMap.put(SUCCESS, SuccessNotifyCondition.class);
        typeMap.put(STATE_CHANGE, StateChangeNotifyCondition.class);
        typeMap.put(TRUE, TrueNotifyCondition.class);
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
            throw new PulseRuntimeException(e);
        }
    }
}
