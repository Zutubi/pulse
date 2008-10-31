package com.zutubi.pulse.master.project;

import com.zutubi.events.Event;
import com.zutubi.events.EventListener;
import com.zutubi.events.EventManager;
import com.zutubi.pulse.core.Stoppable;
import com.zutubi.pulse.master.bootstrap.MasterConfigurationManager;
import com.zutubi.pulse.master.project.events.ProjectEvent;
import com.zutubi.util.io.IOUtils;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Manages loggers for all projects.  Loggers are created on demand as project
 * events come in to log, or as they are requested via {@link #getLogger(long)}.
 */
public class ProjectLoggerManager implements EventListener, Stoppable
{
    // Default is 1MB
    private static final int DEFAULT_SIZE_LIMIT = 1024 * 1024;
    private static final int SIZE_LIMIT = Integer.getInteger("pulse.project.log.size.limit", DEFAULT_SIZE_LIMIT);

    private final Map<Long, ProjectLogger> idToLogger = new HashMap<Long, ProjectLogger>();

    private File projectRoot;
    private volatile boolean stopped = false;
    private EventManager eventManager;

    /**
     * Returns the logger for the given project.
     *
     * @param projectId identifier of the project to retrieve the logger for
     * @return the logger for the given project
     */
    public ProjectLogger getLogger(long projectId)
    {
        synchronized(this)
        {
            ProjectLogger logger = idToLogger.get(projectId);
            if (logger == null)
            {
                File dir = new File(projectRoot, Long.toString(projectId));
                if (!dir.exists())
                {
                    dir.mkdirs();
                }
                
                logger = new ProjectLogger(dir, SIZE_LIMIT);
                idToLogger.put(projectId, logger);
            }

            return logger;
        }
    }

    public void handleEvent(Event event)
    {
        synchronized(this)
        {
            if (stopped)
            {
                return;
            }
            
            ProjectEvent pe = (ProjectEvent) event;
            getLogger(pe.getProjectConfiguration().getProjectId()).log(pe);
        }
    }

    public Class[] getHandledEvents()
    {
        return new Class[] { ProjectEvent.class };
    }

    public void stop(boolean force)
    {
        synchronized(this)
        {
            stopped = true;
            eventManager.unregister(this);

            for (ProjectLogger logger: idToLogger.values())
            {
                IOUtils.close(logger);
            }
        }
    }

    public void setConfigurationManager(MasterConfigurationManager configurationManager)
    {
        projectRoot = configurationManager.getUserPaths().getProjectRoot();
    }

    public void setEventManager(EventManager eventManager)
    {
        this.eventManager = eventManager;
        eventManager.register(this);
    }
}
