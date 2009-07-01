package com.zutubi.pulse.master.project;

import com.zutubi.events.EventManager;
import com.zutubi.i18n.Messages;
import com.zutubi.pulse.core.scm.api.*;
import com.zutubi.pulse.core.scm.config.api.ScmConfiguration;
import com.zutubi.pulse.master.project.events.*;
import com.zutubi.pulse.master.scm.ScmClientUtils;
import com.zutubi.pulse.master.scm.ScmManager;
import com.zutubi.pulse.master.tove.config.project.ProjectConfiguration;
import com.zutubi.pulse.servercore.util.background.BackgroundServiceSupport;
import com.zutubi.util.FileSystemUtils;
import com.zutubi.util.io.IOUtils;
import com.zutubi.util.logging.Logger;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * A background service to run project initialisation, as this may take some
 * time.
 */
public class ProjectInitialisationService extends BackgroundServiceSupport
{
    private static final Logger LOG = Logger.getLogger(ProjectInitialisationService.class);
    private static final Messages I18N = Messages.getInstance(ProjectInitialisationService.class);

    /**
     * A mapping from project handle to configuration for projects that have
     * been initialised.  This is used to guarantee that we destroy with the
     * same config that we used to initialise.
     */
    private final Map<Long, ProjectConfiguration> initialisedConfigurations = new HashMap<Long, ProjectConfiguration>();

    private EventManager eventManager;
    private ScmManager scmManager;

    public ProjectInitialisationService()
    {
        super("Project Initialisation");
    }

    /**
     * Registers that a project is already initialised on startup.
     *
     * @param projectConfig the project to register
     */
    public void registerInitialised(ProjectConfiguration projectConfig)
    {
        synchronized (initialisedConfigurations)
        {
            initialisedConfigurations.put(projectConfig.getHandle(), projectConfig);
        }
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
     */
    public void requestInitialisation(final ProjectConfiguration projectConfiguration)
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
                    checkExistingInitialisation(projectConfiguration);

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
                        cleanupScmDirectoryIfRequired(scmContext.getPersistentWorkingDir());
                        scmClient.init(scmContext, handler);
                    }
                    finally
                    {
                        scmContext.unlock();
                    }

                    synchronized (initialisedConfigurations)
                    {
                        initialisedConfigurations.put(projectConfiguration.getHandle(), projectConfiguration);
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

    private void checkExistingInitialisation(ProjectConfiguration projectConfiguration)
    {
        synchronized (initialisedConfigurations)
        {
            ProjectConfiguration initialised = initialisedConfigurations.remove(projectConfiguration.getHandle());
            if (initialised != null)
            {
                eventManager.publish(new ProjectStatusEvent(this, projectConfiguration, I18N.format("reinitialise.message")));
                doDestroy(initialised);
                eventManager.publish(new ProjectStatusEvent(this, projectConfiguration, I18N.format("reinitialise.complete.message")));
            }
        }
    }

    /**
     * Requests an asynchronous call to destroy on an {@link ScmClient} for the
     * given project.  This is should be called when the project is being
     * deleted.
     * <p/>
     * <strong>Note</strong>: destruction may also occur when re-initialising a
     * project, i.e. via a call to {@link #requestInitialisation(com.zutubi.pulse.master.tove.config.project.ProjectConfiguration)}
     * for an already-initialised project.
     *
     * @param projectConfiguration configuration of the project to call destroy
     *                             for
     * @param deleted              should be true if the entire project has
     *                             been deleted
     */
    public void requestDestruction(final ProjectConfiguration projectConfiguration, final boolean deleted)
    {
        getExecutorService().submit(new Runnable()
        {
            public void run()
            {
                if (!deleted)
                {
                    eventManager.publish(new ProjectDestructionCommencedEvent(ProjectInitialisationService.this, projectConfiguration));
                }

                ProjectConfiguration initialisedConfiguration;
                synchronized (initialisedConfigurations)
                {
                    initialisedConfiguration = initialisedConfigurations.remove(projectConfiguration.getHandle());
                }

                // If we have no initialised configuration, destroy is a no-op.
                if (initialisedConfiguration != null)
                {
                    doDestroy(initialisedConfiguration);
                }

                if (!deleted)
                {
                    eventManager.publish(new ProjectDestructionCompletedEvent(ProjectInitialisationService.this, projectConfiguration));
                }
            }
        });
    }

    private void doDestroy(final ProjectConfiguration projectConfiguration)
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
                        scmManager.clearCache(projectConfiguration.getProjectId());
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

    public void setEventManager(EventManager eventManager)
    {
        this.eventManager = eventManager;
    }

    public void setScmManager(ScmManager scmManager)
    {
        this.scmManager = scmManager;
    }
}
