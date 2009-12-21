package com.zutubi.pulse.master.build.control;

import com.zutubi.pulse.master.bootstrap.MasterConfigurationManager;
import com.zutubi.pulse.master.events.build.BuildRequestEvent;
import com.zutubi.util.bean.ObjectFactory;

/**
 * The default build controller factory creates and configures a BuildController instance
 * to handle the build request.
 */
public class DefaultBuildControllerFactory implements BuildControllerFactory
{
    private ObjectFactory objectFactory;
    private MasterConfigurationManager configurationManager;

    public void init()
    {
    }

    public BuildController create(BuildRequestEvent request)
    {
        DefaultBuildController controller = objectFactory.buildBean(DefaultBuildController.class,
                                                             new Class[] { BuildRequestEvent.class },
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
