package com.zutubi.pulse.scm;

import com.zutubi.pulse.model.ScmManager;
import com.zutubi.pulse.scheduling.Task;
import com.zutubi.pulse.scheduling.TaskExecutionContext;
import com.zutubi.pulse.util.logging.Logger;
import com.zutubi.pulse.util.Constants;

/**
 * <class-comment/>
 */
public class MonitorScms implements Task
{
    private static final Logger LOG = Logger.getLogger(MonitorScms.class);

    private ScmManager scmManager;

    public void execute(TaskExecutionContext context)
    {
        LOG.entering(MonitorScms.class.getName(), "execute");
//        System.out.println("MONITOR SCM STARTING.");
        long start = System.currentTimeMillis();
        try
        {
            scmManager.pollActiveScms();
        }
        finally
        {
//            System.out.println("MONITOR SCM FINISHED.");
            long end = System.currentTimeMillis();
            long time = (end - start) / Constants.SECOND;
            if (time > 100)
            {
                LOG.warning("polling active Scms took %s seconds.", time);
            }
        }
        LOG.exiting();
    }

    /**
     * Required resource.
     *
     * @param scmManager instance
     */
    public void setScmManager(ScmManager scmManager)
    {
        this.scmManager = scmManager;
    }
}
