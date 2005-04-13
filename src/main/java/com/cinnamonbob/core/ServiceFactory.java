package com.cinnamonbob.core;

import nu.xom.Element;

public class ServiceFactory extends GenericFactory<Service>
{
    public ServiceFactory()
    {
        super(Service.class);
    }
    
    public Service createService(String name, ConfigContext context, Element element) throws ConfigException
    {
        return (Service)super.create(name, context, element);
    }
}
