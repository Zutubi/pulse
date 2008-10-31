package com.zutubi.pulse.master.project;

import com.zutubi.events.EventManager;
import com.zutubi.pulse.core.scm.api.ScmCancelledException;
import com.zutubi.pulse.core.scm.api.ScmClient;
import com.zutubi.pulse.core.scm.api.ScmContext;
import com.zutubi.pulse.core.scm.api.ScmFeedbackHandler;
import com.zutubi.pulse.core.scm.config.api.ScmConfiguration;
import com.zutubi.pulse.master.project.events.ProjectInitialisationCommencedEvent;
import com.zutubi.pulse.master.project.events.ProjectInitialisationCompletedEvent;
import com.zutubi.pulse.master.project.events.ProjectStatusEvent;
import com.zutubi.pulse.master.scm.ScmManager;
import com.zutubi.pulse.master.tove.config.project.ProjectConfiguration;
import com.zutubi.pulse.servercore.util.background.BackgroundServiceSupport;
import com.zutubi.util.FileSystemUtils;
import com.zutubi.util.io.IOUtils;

import java.io.File;

/**
 * A background service to run project initialisation, as this may take some
 * time.
 */
public class ProjectInitialisationService extends BackgroundServiceSupport
{
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
                    ScmConfiguration scmConfiguration = projectConfiguration.getScm();
                    ScmContext scmContext = scmManager.createContext(projectConfiguration.getProjectId(), scmConfiguration);
                    scmContext.lock();
                    try
                    {
                        cleanupScmDirectoryIfRequired(scmContext.getPersistentWorkingDir());

                        scmClient = scmManager.createClient(scmConfiguration);
                        scmClient.init(scmContext, new ScmFeedbackHandler()
                        {
                            public void status(String message)
                            {
                                eventManager.publish(new ProjectStatusEvent(ProjectInitialisationService.this, projectConfiguration, message));
                            }

                            public void checkCancelled() throws ScmCancelledException
                            {
                                // not cancellable
                            }
                        });
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

    public void setEventManager(EventManager eventManager)
    {
        this.eventManager = eventManager;
    }

    public void setScmManager(ScmManager scmManager)
    {
        this.scmManager = scmManager;
    }
}
