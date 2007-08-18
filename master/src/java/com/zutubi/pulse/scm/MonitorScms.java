package com.zutubi.pulse.scm;

import com.zutubi.pulse.model.ScmManager;
import com.zutubi.pulse.scheduling.Task;
import com.zutubi.pulse.scheduling.TaskExecutionContext;
import com.zutubi.util.logging.Logger;
import com.zutubi.util.Constants;

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
        long start = System.currentTimeMillis();
        try
        {
            scmManager.pollActiveScms();
        }
        finally
        {
            long end = System.currentTimeMillis();
            long time = (end - start) / Constants.SECOND;
            if (time > 100)
            {
                LOG.info(String.format("Polling active Scms took %s seconds.", time));
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
