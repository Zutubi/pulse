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

package com.zutubi.pulse.master.tove.config;

import org.hibernate.EmptyInterceptor;
import org.hibernate.type.Type;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * A Hibernate interceptor that injects associated config instances 
 */
public class ConfigurationInjector extends EmptyInterceptor
{
    private Map<Class, ConfigurationSetter> setterMap = new HashMap<Class, ConfigurationSetter>();

    @SuppressWarnings({"unchecked"})
    public boolean onLoad(Object entity, Serializable id, Object[] state, String[] propertyNames, Type[] types)
    {
        ConfigurationSetter setter = setterMap.get(entity.getClass());
        if(setter != null)
        {
            setter.setConfiguration(entity);
        }
        return false;
    }

    public <T> void registerSetter(Class<? extends T> clazz, ConfigurationSetter<T> setter)
    {
        setterMap.put(clazz, setter);
    }

    /**
     * Returns true if a configuration setter has been registered for the specified class.  Another
     * way of looking at it is that if this method returns true, then the specified class is a state
     * class for a configuration type.
     *
     * @param clazz the class for which we are checking whether or not a configuration setter is registered.
     * 
     * @return true if a configuration setter has been registered, false otherwise.
     */
    public boolean hasRegisteredSetter(Class clazz)
    {
        return setterMap.containsKey(clazz);
    }

    public static interface ConfigurationSetter<T>
    {
        void setConfiguration(T state);
    }
}
