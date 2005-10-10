package com.cinnamonbob.web.console;

import com.cinnamonbob.web.ActionSupport;
import com.cinnamonbob.bootstrap.quartz.QuartzManager;
import org.quartz.Scheduler;

/**
 * <class-comment/>
 */
public class QuartzAdminAction extends ActionSupport
{
    public Scheduler getScheduler()
    {
        return QuartzManager.getScheduler();
    }
}
