package com.zutubi.pulse.master.project;

import com.zutubi.events.EventManager;
import com.zutubi.pulse.core.scm.api.*;
import com.zutubi.pulse.core.scm.config.api.ScmConfiguration;
import com.zutubi.pulse.master.project.events.ProjectInitialisationCommencedEvent;
import com.zutubi.pulse.master.project.events.ProjectInitialisationCompletedEvent;
import com.zutubi.pulse.master.project.events.ProjectStatusEvent;
import com.zutubi.pulse.master.scm.ScmClientUtils;
import com.zutubi.pulse.master.scm.ScmManager;
import com.zutubi.pulse.master.tove.config.project.ProjectConfiguration;
import com.zutubi.pulse.servercore.util.background.BackgroundServiceSupport;
import com.zutubi.util.FileSystemUtils;
import com.zutubi.util.io.IOUtils;
import com.zutubi.util.logging.Logger;

import java.io.File;

/**
 * A background service to run project initialisation, as this may take some
 * time.
 */
public class ProjectInitialisationService extends BackgroundServiceSupport
{
    private static final Logger LOG = Logger.getLogger(ProjectInitialisationService.class);

    private EventManager eventManager;
    private ScmManager scmManager;

    public ProjectInitialisationService()
    {
        super("Project Initialisation");
    }

    /**
     * Submits a request to initialise a project.  The initialisation itself
     * will be run in the background and the result returned via {@link com.zutubi.pulse.master.project.events.ProjectLifecycleEvent}s.
     * Feedback may also be returned using {@link com.zutubi.pulse.master.project.events.ProjectStatusEvent}s
     * while the initialisation is running.
     * <p/>
     * If the project has already been initialised, the artifacts of the
     * previous initialisation will be cleaned up before running the new
     * initialisation.
     *
     * @param projectConfiguration project to be initialised
     * @param reinitialise         true if the project is being reinitialised, false if this is the first initialise
     */
    public void requestInitialisation(final ProjectConfiguration projectConfiguration, final boolean reinitialise)
    {
        getExecutorService().submit(new Runnable()
        {
            public void run()
            {
                eventManager.publish(new ProjectInitialisationCommencedEvent(ProjectInitialisationService.this, projectConfiguration));

                ProjectInitialisationCompletedEvent completedEvent;
                ScmClient scmClient = null;
                try
                {
                    ScmConfiguration scmConfiguration = projectConfiguration.getScm();
                    ScmContext scmContext = scmManager.createContext(projectConfiguration);
                    scmContext.lock();
                    try
                    {
                        ScmFeedbackHandler handler = new ScmFeedbackHandler()
                        {
                            public void status(String message)
                            {
                                eventManager.publish(new ProjectStatusEvent(ProjectInitialisationService.this, projectConfiguration, message));
                            }

                            public void checkCancelled() throws ScmCancelledException
                            {
                                // not cancellable
                            }
                        };

                        scmClient = scmManager.createClient(scmConfiguration);
                        if (reinitialise)
                        {
                            scmClient.destroy(scmContext, handler);
                        }

                        cleanupScmDirectoryIfRequired(scmContext.getPersistentWorkingDir());
                        scmClient.init(scmContext, handler);
                    }
                    finally
                    {
                        scmContext.unlock();
                    }

                    completedEvent = new ProjectInitialisationCompletedEvent(ProjectInitialisationService.this, projectConfiguration, true, null);
                }
                catch (Exception e)
                {
                    completedEvent = new ProjectInitialisationCompletedEvent(this, projectConfiguration, false, e.getMessage());
                }
                finally
                {
                    IOUtils.close(scmClient);
                }

                eventManager.publish(completedEvent);
            }

            private void cleanupScmDirectoryIfRequired(File dir)
            {
                if (dir.exists())
                {
                    FileSystemUtils.rmdir(dir);
                }
            }
        });
    }

    /**
     * Requests an asynchronous call to destroy on an {@link ScmClient} for the
     * given project.  This is should be called when the project is being
     * deleted.
     * <p/>
     * <strong>Note</strong>: destroy may also be called when re-initialising a
     * project, to do so call {@link #requestInitialisation(com.zutubi.pulse.master.tove.config.project.ProjectConfiguration, boolean)}
     * with reinitialise set to true.
     *
     * @param projectConfiguration configuration of the project to call destory
     *                             for
     */
    public void requestDestruction(final ProjectConfiguration projectConfiguration)
    {
        getExecutorService().submit(new Runnable()
        {
            public void run()
            {
                try
                {
                    ScmClientUtils.withScmClient(projectConfiguration, scmManager, new ScmClientUtils.ScmContextualAction<Object>()
                    {
                        public Object process(ScmClient scmClient, ScmContext scmContext) throws ScmException
                        {
                            scmContext.lock();
                            try
                            {
                                scmClient.destroy(scmContext, new ScmFeedbackAdapter());
                            }
                            finally
                            {
                                scmContext.unlock();
                            }
                            return null;
                        }
                    });
                }
                catch (Exception e)
                {
                    // The project log may be gone by now, so we don't write
                    // to it.
                    LOG.severe(e);
                }
            }
        });
    }

    public void setEventManager(EventManager eventManager)
    {
        this.eventManager = eventManager;
    }

    public void setScmManager(ScmManager scmManager)
    {
        this.scmManager = scmManager;
    }
}
