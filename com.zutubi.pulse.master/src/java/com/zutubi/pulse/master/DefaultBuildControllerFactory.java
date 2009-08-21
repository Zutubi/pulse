package com.zutubi.pulse.master;

import com.zutubi.pulse.master.events.build.BuildRequestEvent;
import com.zutubi.pulse.master.bootstrap.MasterConfigurationManager;
import com.zutubi.pulse.master.scheduling.quartz.TimeoutRecipeJob;
import static com.zutubi.pulse.master.DefaultBuildController.TIMEOUT_JOB_GROUP;
import static com.zutubi.pulse.master.DefaultBuildController.TIMEOUT_JOB_NAME;
import com.zutubi.util.bean.ObjectFactory;
import com.zutubi.util.logging.Logger;
import org.quartz.JobDetail;
import org.quartz.SchedulerException;
import org.quartz.Scheduler;

/**
 * The default build controller factory creates and configures a BuildController instance
 * to handle the build request.
 */
public class DefaultBuildControllerFactory implements BuildControllerFactory
{
    private static final Logger LOG = Logger.getLogger(DefaultBuildControllerFactory.class);

    private ObjectFactory objectFactory;
    private MasterConfigurationManager configurationManager;
    private Scheduler quartzScheduler;

    public void init()
    {
        // initialise the timeout triggers that will be used by the created BuildControllers.
        try
        {
            JobDetail detail = quartzScheduler.getJobDetail(TIMEOUT_JOB_NAME, TIMEOUT_JOB_GROUP);
            if (detail ==  null)
            {
                detail = new JobDetail(TIMEOUT_JOB_NAME, TIMEOUT_JOB_GROUP, TimeoutRecipeJob.class);
                detail.setDurability(true); // will stay around after the trigger has gone.
                quartzScheduler.addJob(detail, true);
            }
        }
        catch (SchedulerException e)
        {
            LOG.severe("Unable to setup build timeout job: " + e.getMessage(), e);
        }
    }

    public BuildController createHandler(BuildRequestEvent request)
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

    public void setQuartzScheduler(Scheduler quartzScheduler)
    {
        this.quartzScheduler = quartzScheduler;
    }
}
