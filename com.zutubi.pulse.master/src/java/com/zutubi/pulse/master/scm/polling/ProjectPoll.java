package com.zutubi.pulse.master.scm.polling;

import com.zutubi.events.EventManager;
import com.zutubi.i18n.Messages;
import com.zutubi.pulse.core.scm.api.Revision;
import com.zutubi.pulse.core.scm.api.ScmClient;
import com.zutubi.pulse.core.scm.api.ScmContext;
import com.zutubi.pulse.core.scm.api.ScmException;
import com.zutubi.pulse.core.scm.config.api.Pollable;
import com.zutubi.pulse.master.model.Project;
import com.zutubi.pulse.master.model.ProjectManager;
import com.zutubi.pulse.master.project.events.ProjectStatusEvent;
import com.zutubi.pulse.master.scm.ScmManager;
import com.zutubi.pulse.master.tove.config.project.ProjectConfiguration;
import com.zutubi.util.Constants;
import com.zutubi.util.io.IOUtils;
import com.zutubi.util.logging.Logger;
import com.zutubi.util.time.Clock;
import com.zutubi.util.time.TimeStamps;

import java.util.List;
import java.util.concurrent.Callable;

/**
 * Runs a single SCM poll on a project, returning the new polling state.
 */
public class ProjectPoll implements Callable<ProjectPollingState>
{
    private static final Messages I18N = Messages.getInstance(ProjectPoll.class);
    private static final Logger LOG = Logger.getLogger(ProjectPoll.class);

    private Project project;
    private ProjectPollingState initialState;

    private Clock clock;
    private EventManager eventManager;
    private ProjectManager projectManager;
    private ScmManager scmManager;

    /**
     * Creates a new poll operation for the given project with the given
     * previous polling state.
     *
     * @param project the project to poll
     * @param initialState state from the previous poll
     * @param clock used to get timestamps (e.g. to process the quiet period)
     */
    public ProjectPoll(Project project, ProjectPollingState initialState, Clock clock)
    {
        this.project = project;
        this.initialState = initialState;
        this.clock = clock;
    }

    /**
     * Polls the project and returns the new polling state.
     *
     * @return the new polling state for the project
     * @throws Exception on any error
     */
    public ProjectPollingState call() throws Exception
    {
        ProjectConfiguration projectConfig = project.getConfig();
        Pollable pollable = (Pollable) projectConfig.getScm();
        long projectId = projectConfig.getProjectId();

        ScmClient client = null;
        ProjectPollingState newState = initialState;
        try
        {
            long now = clock.getCurrentTimeMillis();

            publishStatusMessage(projectConfig, I18N.format("polling.start"));
            projectManager.updateLastPollTime(projectId, now);

            client = createClient(projectConfig);
            ScmContext context = createContext(project, client.getImplicitResource());

            // When was the last time that we checked?  If never, get the latest revision.
            Revision previous = initialState.getLatestRevision();
            if (previous == null)
            {
                newState = initialiseRevision(projectConfig, client, context);
            }
            else
            {
                if (pollable.isQuietPeriodEnabled())
                {
                    int quietPeriod = pollable.getQuietPeriod();
                    if (initialState.isInQuietPeriod())
                    {
                        if (initialState.hasQuietPeriodElapsed(now))
                        {
                            newState = postQuietPeriod(projectConfig, quietPeriod, client, context);
                        }
                    }
                    else
                    {
                        Revision latest = getLatestRevisionSince(previous, client, context);
                        if (latest != null)
                        {
                            newState = startQuietPeriod(projectConfig, latest, quietPeriod);
                        }
                    }
                }
                else
                {
                    Revision latest = getLatestRevisionSince(previous, client, context);
                    if (latest != null)
                    {
                        newState = new ProjectPollingState(initialState.getProjectId(), latest);
                    }
                }
            }

            publishStatusMessage(projectConfig, I18N.format("polling.end", TimeStamps.getPrettyElapsed(clock.getCurrentTimeMillis() - now)));
            return newState;
        }
        catch (ScmException e)
        {
            publishStatusMessage(projectConfig, I18N.format("polling.error", e.getMessage()));
            LOG.debug(e);

            if (e.isReinitialiseRequired())
            {
                projectManager.makeStateTransition(projectConfig.getProjectId(), Project.Transition.INITIALISE);
            }

            return newState;
        }
        finally
        {
            IOUtils.close(client);
        }
    }

    private ProjectPollingState initialiseRevision(ProjectConfiguration projectConfig, ScmClient client, ScmContext context) throws ScmException
    {
        Revision latest = client.getLatestRevision(context);
        // slightly paranoid, but we can not rely on the scm implementations to behave as expected.
        if (latest == null)
        {
            publishStatusMessage(projectConfig, I18N.format("polling.error", "Failed to return latest revision."));
        }
        else
        {
            publishStatusMessage(projectConfig, I18N.format("polling.initial", latest.getRevisionString()));
        }

        return new ProjectPollingState(initialState.getProjectId(), latest);
    }

    private ProjectPollingState startQuietPeriod(ProjectConfiguration projectConfig, Revision latest, int quietPeriod)
    {
        if (quietPeriod != 0)
        {
            publishStatusMessage(projectConfig, I18N.format("polling.quiet.start", latest.getRevisionString()));
            return new ProjectPollingState(initialState.getProjectId(), initialState.getLatestRevision(), clock.getCurrentTimeMillis() + quietPeriod * Constants.MINUTE, latest);
        }
        else
        {
            return new ProjectPollingState(initialState.getProjectId(), latest);
        }
    }

    private ProjectPollingState postQuietPeriod(ProjectConfiguration projectConfig, int quietPeriod, ScmClient client, ScmContext context) throws ScmException
    {
        Revision latest = getLatestRevisionSince(initialState.getQuietPeriodRevision(), client, context);
        if (latest != null)
        {
            // there has been a commit during the 'quiet period', reset the timer.
            publishStatusMessage(projectConfig, I18N.format("polling.quiet.continue", latest.getRevisionString()));
            return new ProjectPollingState(initialState.getProjectId(), initialState.getLatestRevision(), clock.getCurrentTimeMillis() + quietPeriod * Constants.MINUTE, latest);
        }
        else
        {
            // there have been no commits during the 'quiet period', trigger a change.
            publishStatusMessage(projectConfig, I18N.format("polling.quiet.end"));
            //changes.add(new ScmChangeEvent(projectConfig, lastChange, previous));
            return new ProjectPollingState(initialState.getProjectId(), initialState.getQuietPeriodRevision());
        }
    }

    private void publishStatusMessage(ProjectConfiguration project, String message)
    {
        eventManager.publish(new ProjectStatusEvent(this, project, message));
    }

    private ScmContext createContext(Project project, String implicitResource) throws ScmException
    {
        return scmManager.createContext(project.getConfig(), project.getState(), implicitResource);
    }

    private ScmClient createClient(ProjectConfiguration project) throws ScmException
    {
        return scmManager.createClient(project, project.getScm());
    }

    private Revision getLatestRevisionSince(Revision revision, ScmClient client, ScmContext context) throws ScmException
    {
        // this assumes that getting the revision since revision x is more efficient than getting the latest revision.
        List<Revision> revisions = client.getRevisions(context, revision, null);
        if (revisions.size() > 0)
        {
            // get the latest revision.
            return revisions.get(revisions.size() - 1);
        }
        return null;
    }

    public void setEventManager(EventManager eventManager)
    {
        this.eventManager = eventManager;
    }

    public void setProjectManager(ProjectManager projectManager)
    {
        this.projectManager = projectManager;
    }

    public void setScmManager(ScmManager scmManager)
    {
        this.scmManager = scmManager;
    }
}
