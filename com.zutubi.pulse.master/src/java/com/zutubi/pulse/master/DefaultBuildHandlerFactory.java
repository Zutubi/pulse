package com.zutubi.pulse.master;

import com.zutubi.pulse.master.events.build.AbstractBuildRequestEvent;
import com.zutubi.pulse.master.bootstrap.MasterConfigurationManager;
import com.zutubi.util.bean.ObjectFactory;

/**
 * The default build handler factory creates and configures a BuildController instance
 * to handle the build request.
 */
public class DefaultBuildHandlerFactory implements BuildHandlerFactory
{
    private ObjectFactory objectFactory;
    private MasterConfigurationManager configurationManager;

    public BuildHandler createHandler(AbstractBuildRequestEvent request)
    {
        BuildController controller = objectFactory.buildBean(BuildController.class,
                                                             new Class[] { AbstractBuildRequestEvent.class },
                                                             new Object[] { request });
        DefaultRecipeResultCollector collector = new DefaultRecipeResultCollector(configurationManager);
        collector.setProjectConfig(request.getProjectConfig());
        controller.setCollector(collector);
        
        return controller;
    }

    public void setObjectFactory(ObjectFactory objectFactory)
    {
        this.objectFactory = objectFactory;
    }

    public void setConfigurationManager(MasterConfigurationManager configurationManager)
    {
        this.configurationManager = configurationManager;
    }
}
