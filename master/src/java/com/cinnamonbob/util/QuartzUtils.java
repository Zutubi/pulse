package com.cinnamonbob.util;

import org.quartz.JobExecutionContext;
import org.quartz.JobDetail;
import org.quartz.SchedulerException;

import java.util.Comparator;
import java.util.List;

import com.cinnamonbob.util.logging.Logger;

/**
 * <class-comment/>
 */
public class QuartzUtils
{
    private static final Logger LOG = Logger.getLogger(QuartzUtils.class);

    public static boolean isJobExecuting(JobExecutionContext context)
    {
        try
        {
            Comparator c = new JobDetailComparator();

            List executingJobs = context.getScheduler().getCurrentlyExecutingJobs();
            for (Object executingJob : executingJobs)
            {
                JobExecutionContext executingContext = (JobExecutionContext) executingJob;
                if (executingContext == context)
                {
                    // ignore the current executing context.
                    continue;
                }
                if (c.compare(context.getJobDetail(), executingContext.getJobDetail()) == 0)
                {
                    // concurrent execution detected.
                    return true;
                }
            }
            return false;
        }
        catch (SchedulerException e)
        {
            LOG.warning("Failed to determine if quartz job was already executing.", e);
            return false;
        }
    }

    private static class JobDetailComparator implements Comparator
    {
        public int compare(Object o1, Object o2)
        {
            JobDetail a = (JobDetail) o1;
            JobDetail b = (JobDetail) o2;

            int groupComparison = a.getGroup().compareTo(b.getGroup());
            if (groupComparison != 0)
            {
                return groupComparison;
            }

            return a.getName().compareTo(b.getName());
        }
    }
}