package com.zutubi.pulse.master.agent;

import com.zutubi.i18n.Messages;
import com.zutubi.pulse.master.tove.config.agent.AgentConfiguration;
import com.zutubi.pulse.master.tove.config.project.BootstrapConfiguration;
import com.zutubi.pulse.master.tove.config.project.BuildStageConfiguration;
import com.zutubi.pulse.master.tove.config.project.ProjectConfiguration;
import com.zutubi.pulse.servercore.AgentRecipeDetails;
import com.zutubi.pulse.servercore.agent.DeleteDirectoryTask;
import com.zutubi.pulse.servercore.agent.SynchronisationMessage;
import com.zutubi.pulse.servercore.agent.SynchronisationTaskFactory;
import com.zutubi.pulse.servercore.util.background.BackgroundServiceSupport;
import com.zutubi.tove.variables.api.Variable;
import com.zutubi.tove.variables.api.VariableMap;
import com.zutubi.util.adt.Pair;

import java.util.*;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static com.zutubi.util.CollectionUtils.asPair;

/**
 * Queueing all the synchronisation messages required to clean up persistent working directories may
 * take significant time.  This can lead to, e.g. a timeout in the UI when deleting a build stage.
 * This services avoids that by making the process asynchronous (CIB-2993).
 */
public class WorkDirectoryCleanupService extends BackgroundServiceSupport
{
    private static final Messages I18N = Messages.getInstance(WorkDirectoryCleanupService.class);
    private static final int MAX_THREADS = 6;
    private static final long THREAD_IDLE_TIMEOUT = 60L;

    private AgentManager agentManager;
    private SynchronisationTaskFactory synchronisationTaskFactory;

    public WorkDirectoryCleanupService()
    {
        super("Work Directory Cleanup Service",
              new ThreadPoolExecutor(0, MAX_THREADS, THREAD_IDLE_TIMEOUT, TimeUnit.SECONDS, new SynchronousQueue<Runnable>(), new ThreadPoolExecutor.CallerRunsPolicy()),
              false);
    }

    /**
     * Submits a background task that enqueues the synchronisation messages required to clean up the
     * work directories for stages of a project.  This method returns immediately after submitting
     * the task.
     * 
     * @param projectConfig the project to clean up directories of
     * @param specificStage a single stage to clean up, or null to clean all stages of the project
     */
    public void asyncEnqueueCleanupMessages(final ProjectConfiguration projectConfig, final BuildStageConfiguration specificStage)
    {
        getExecutorService().submit(new Runnable()
        {
            public void run()
            {
                enqueueCleanupMessages(projectConfig, specificStage);
            }
        });
    }

    /**
     * Enqueues the synchronisation messages required to clean up the work directories for stages of
     * a project.  This method synchronously enqueues the messages.
     *
     * @param projectConfig the project to clean up directories of
     * @param specificStage a single stage to clean up, or null to clean all stages of the project
     */
    public void enqueueCleanupMessages(ProjectConfiguration projectConfig, BuildStageConfiguration specificStage)
    {
        BootstrapConfiguration bootstrap = projectConfig.getBootstrap();
        if (bootstrap != null)
        {
            final String deleteTaskType = SynchronisationTaskFactory.getTaskType(DeleteDirectoryTask.class);
            String workDirPattern = bootstrap.getPersistentDirPattern();

            AgentRecipeDetails details = new AgentRecipeDetails();
            details.setProject(projectConfig.getName());
            details.setProjectHandle(projectConfig.getHandle());
            for (Agent agent: agentManager.getAllAgents())
            {
                List<Pair<Properties, String>> propertiesDescriptionPairs = new LinkedList<Pair<Properties, String>>();

                AgentConfiguration agentConfig = agent.getConfig();
                details.setAgent(agent.getName());
                details.setAgentHandle(agentConfig.getHandle());

                Collection<BuildStageConfiguration> stageConfigs;
                if (specificStage == null)
                {
                    stageConfigs = projectConfig.getStages().values();
                }
                else
                {
                    stageConfigs = Arrays.asList(specificStage);
                }

                for (BuildStageConfiguration stageConfig: stageConfigs)
                {
                    details.setStage(stageConfig.getName());
                    details.setStageHandle(stageConfig.getHandle());

                    DeleteDirectoryTask deleteTask = new DeleteDirectoryTask(agentConfig.getStorage().getDataDirectory(), workDirPattern, agentConfig.getStorage().isOutsideCleanupAllowed(), getVariables(details));
                    SynchronisationMessage message = synchronisationTaskFactory.toMessage(deleteTask);
                    propertiesDescriptionPairs.add(asPair(message.getArguments(), I18N.format("cleanup.stage.directory", details.getProject(), details.getStage())));
                }

                if (propertiesDescriptionPairs.size() > 0)
                {
                    agentManager.enqueueSynchronisationMessages(agent, deleteTaskType, propertiesDescriptionPairs);
                }
            }
        }
    }

    private Map<String, String> getVariables(AgentRecipeDetails details)
    {
        VariableMap variables = details.createPathVariableMap();
        Map<String, String> result = new HashMap<String, String>();
        for (Variable variable: variables.getVariables())
        {
            result.put(variable.getName(), variable.getValue().toString());
        }

        return result;
    }

    public void setAgentManager(AgentManager agentManager)
    {
        this.agentManager = agentManager;
    }

    public void setSynchronisationTaskFactory(SynchronisationTaskFactory synchronisationTaskFactory)
    {
        this.synchronisationTaskFactory = synchronisationTaskFactory;
    }
}
