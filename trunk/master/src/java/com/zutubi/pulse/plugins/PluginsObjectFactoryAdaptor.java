package com.zutubi.pulse.plugins;

import com.zutubi.plugins.ObjectFactory;

/**
 * <class-comment/>
 */
public class PluginsObjectFactoryAdaptor implements ObjectFactory
{
    private com.zutubi.pulse.core.ObjectFactory delegate;

    public PluginsObjectFactoryAdaptor(com.zutubi.pulse.core.ObjectFactory of)
    {
        this.delegate = of;
    }

    public <V> V buildBean(Class clazz) throws Exception
    {
        return (V)delegate.buildBean(clazz);
    }

    public <U> U buildBean(String className) throws Exception
    {
        return (U)delegate.buildBean(className);
    }
}
