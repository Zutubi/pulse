package com.cinnamonbob.schedule.triggers;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.util.logging.Logger;
import com.cinnamonbob.schedule.DefaultScheduleManager;
import com.cinnamonbob.schedule.ScheduleManager;

/**
 * <class-comment/>
 */
public class QuartzTaskWrapper implements Job
{
    private static final Logger LOG = Logger.getLogger(QuartzTaskWrapper.class.getName());

    private DefaultScheduleManager scheduleManager;

    public void setScheduleManager(ScheduleManager scheduleManager)
    {
        this.scheduleManager = (DefaultScheduleManager) scheduleManager;
    }

    public void execute(JobExecutionContext context) throws JobExecutionException
    {
        // notify schedule manager that this trigger has been activated.
        long id = (Long) context.getTrigger().getJobDataMap().get("id");
        scheduleManager.activate(id);
    }


//        try
//        {
//            // create task from context and execute.
//            String taskClassName = (String) context.getMergedJobDataMap().get(TASK);
//            Task task = ObjectFactory.getObjectFactory().buildBean(taskClassName);
//
//            // autowrite
//            ComponentContext.autowire(task);
//
//            // auto wire it with the available context data.
//            BeanWrapper bw = new BeanWrapperImpl(task);
//            MutablePropertyValues pvs = new MutablePropertyValues();
//            pvs.addPropertyValues(context.getScheduler().getContext());
//            pvs.addPropertyValues(context.getTrigger().getJobDataMap());
//            pvs.addPropertyValues(context.getJobDetail().getJobDataMap());
//            bw.setPropertyValues(pvs, true);
//
//            // execute the task.
//            task.execute();
//        }
//        catch (Exception e)
//        {
//            LOG.log(Level.SEVERE, e.getMessage(), e);
//        }

}
