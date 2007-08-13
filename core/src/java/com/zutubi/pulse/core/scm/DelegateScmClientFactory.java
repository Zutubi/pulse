package com.zutubi.pulse.core.scm;

import com.zutubi.util.bean.ObjectFactory;

import java.util.HashMap;
import java.util.Map;

/**
 *
 *
 */
public class DelegateScmClientFactory implements ScmClientFactory
{
    private ObjectFactory objectFactory;

    private Map<Class, ScmClientFactory<Object>> factories = new HashMap<Class, ScmClientFactory<Object>>();

    public ScmClient createClient(Object config) throws ScmException
    {
        ScmClientFactory<Object> factory = getFactory(config);
        return factory.createClient(config);
    }

    private ScmClientFactory<Object> getFactory(Object config)
    {
        return factories.get(config.getClass());
    }

    public void register(Class configType, Class<ScmClientFactory<Object>> factoryType) throws ScmException
    {
        try
        {
            ScmClientFactory<Object> factory = objectFactory.buildBean(factoryType);
            factories.put(configType, factory);
        }
        catch (Exception e)
        {
            throw new ScmException(e);
        }
    }

    public void unregister(Class configType)
    {
        factories.remove(configType);
    }

    public void setObjectFactory(ObjectFactory objectFactory)
    {
        this.objectFactory = objectFactory;
    }
}
