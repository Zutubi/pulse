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
