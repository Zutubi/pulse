package com.zutubi.tove.config;

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
    
    public static interface ConfigurationSetter<T>
    {
        void setConfiguration(T state);
    }
}
