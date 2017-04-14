/* Copyright 2017 Zutubi Pty Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zutubi.pulse.master.notifications.condition;

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
    public static final String BROKEN = "broken";
    public static final String ERROR = "error";
    public static final String FAILURE = "failure";
    public static final String FALSE = "false";
    public static final String HEALTHY = "healthy";
    public static final String RESPONSIBILITY_TAKEN = "responsibility.taken";
    public static final String SKIPPED = "skipped";
    public static final String SUCCESS = "success";
    public static final String STATE_CHANGE = "state.change";
    public static final String TERMINATED = "terminated";
    public static final String TRUE = "true";
    public static final String WARNINGS = "warnings";

    // Integer values
    public static final String BROKEN_COUNT_BUILDS = "broken.count.builds";
    public static final String BROKEN_COUNT_DAYS = "broken.count.days";

    private final static Map<String, Class> typeMap = new HashMap<String, Class>();

    private ObjectFactory objectFactory = new DefaultObjectFactory();

    static
    {
        // initialise the default notification types.
        typeMap.put(BROKEN, BrokenNotifyCondition.class);
        typeMap.put(ERROR, ErrorNotifyCondition.class);
        typeMap.put(FAILURE, FailureNotifyCondition.class);
        typeMap.put(FALSE, FalseNotifyCondition.class);
        typeMap.put(HEALTHY, HealthyNotifyCondition.class);
        typeMap.put(RESPONSIBILITY_TAKEN, ResponsibilityTakenNotifyCondition.class);
        typeMap.put(SKIPPED, SkippedNotifyCondition.class);
        typeMap.put(SUCCESS, SuccessNotifyCondition.class);
        typeMap.put(STATE_CHANGE, StateChangeNotifyCondition.class);
        typeMap.put(TERMINATED, TerminatedNotifyCondition.class);
        typeMap.put(TRUE, TrueNotifyCondition.class);
        typeMap.put(WARNINGS, WarningsNotifyCondition.class);

        typeMap.put(BROKEN_COUNT_BUILDS, BrokenCountBuildsValue.class);
        typeMap.put(BROKEN_COUNT_DAYS, BrokenCountDaysValue.class);
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

    public <T> T build(Class<T> clazz)
    {
        return objectFactory.buildBean(clazz);
    }

    public <T> T build(Class<T> clazz, Class[] argTypes, Object[] args)
    {
        return objectFactory.buildBean(clazz, argTypes, args);
    }
}
