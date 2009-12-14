package com.zutubi.pulse.master.tove.config.project.hooks;

import com.zutubi.events.Event;
import com.zutubi.pulse.core.PulseExecutionContext;
import com.zutubi.pulse.core.engine.api.ExecutionContext;
import com.zutubi.pulse.core.engine.api.Feature;
import com.zutubi.pulse.core.engine.api.ResultState;
import com.zutubi.pulse.core.model.Result;
import com.zutubi.pulse.master.MasterBuildPaths;
import com.zutubi.pulse.master.MasterBuildProperties;
import com.zutubi.pulse.master.agent.MasterLocationProvider;
import com.zutubi.pulse.master.bootstrap.MasterConfigurationManager;
import com.zutubi.pulse.master.build.log.*;
import com.zutubi.pulse.master.events.build.BuildEvent;
import com.zutubi.pulse.master.events.build.StageEvent;
import com.zutubi.pulse.master.model.BuildResult;
import com.zutubi.pulse.master.model.Project;
import com.zutubi.pulse.master.model.RecipeResultNode;
import com.zutubi.pulse.master.model.persistence.hibernate.HibernateBuildResultDao;
import com.zutubi.util.UnaryProcedure;
import com.zutubi.util.io.IOUtils;

import java.io.OutputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Manages the execution of build hooks at various points in the build
 * process.
 */
public class BuildHookManager
{
    private ExecutorService executor = Executors.newSingleThreadExecutor();

    private MasterConfigurationManager configurationManager;
    private MasterLocationProvider masterLocationProvider;

    public void handleEvent(Event event, HookLogger logger)
    {
        final BuildEvent be = (BuildEvent) event;
        BuildResult buildResult = be.getBuildResult();

        PulseExecutionContext context = new PulseExecutionContext(be.getContext());
        Project project = buildResult.getProject();
        for (BuildHookConfiguration hook : project.getConfig().getBuildHooks().values())
        {
            if (hook.enabled() && hook.triggeredBy(be))
            {
                RecipeResultNode resultNode = null;
                if (be instanceof StageEvent)
                {
                    resultNode = ((StageEvent) be).getStageNode();
                }

                logAndExecuteTask(hook, context, logger, buildResult, resultNode, false);
            }
        }
    }

    public void manualTrigger(final BuildHookConfiguration hook, final BuildResult result)
    {
        if (hook.canManuallyTriggerFor(result))
        {
            HibernateBuildResultDao.initialise(result);
            executor.execute(new Runnable()
            {
                public void run()
                {
                    final PulseExecutionContext context = new PulseExecutionContext();
                    MasterBuildProperties.addAllBuildProperties(context, result, masterLocationProvider, configurationManager);
                    if (hook.appliesTo(result))
                    {
                        HookLogger buildLogger = createBuildLogger(result);
                        try
                        {
                            logAndExecuteTask(hook, context, buildLogger, result, null, true);
                        }
                        finally
                        {
                            IOUtils.close(buildLogger);
                        }
                    }

                    result.getRoot().forEachNode(new UnaryProcedure<RecipeResultNode>()
                    {
                        public void process(RecipeResultNode recipeResultNode)
                        {
                            if (hook.appliesTo(recipeResultNode))
                            {
                                context.push();
                                HookLogger recipeLogger = createRecipeLogger(result, recipeResultNode);
                                try
                                {
                                    MasterBuildProperties.addStageProperties(context, result, recipeResultNode, configurationManager, false);
                                    logAndExecuteTask(hook, context, recipeLogger, result, recipeResultNode, true);
                                }
                                finally
                                {
                                    IOUtils.close(recipeLogger);
                                    context.pop();
                                }
                            }
                        }
                    });
                }
            });
        }
    }

    private DefaultBuildLogger createBuildLogger(BuildResult result)
    {
        MasterBuildPaths paths = new MasterBuildPaths(configurationManager);
        DefaultBuildLogger logger = new DefaultBuildLogger(new BuildLogFile(result, paths));
        logger.prepare();
        return logger;
    }

    private DefaultRecipeLogger createRecipeLogger(BuildResult buildResult, RecipeResultNode recipeResultNode)
    {
        MasterBuildPaths paths = new MasterBuildPaths(configurationManager);
        DefaultRecipeLogger logger = new DefaultRecipeLogger(new RecipeLogFile(buildResult, recipeResultNode.getResult().getId(), paths));
        logger.prepare();
        return logger;
    }

    private void logAndExecuteTask(BuildHookConfiguration hook, PulseExecutionContext context, HookLogger logger, BuildResult buildResult, RecipeResultNode resultNode, boolean manual)
    {
        logger.hookCommenced(hook.getName());
        OutputStream out = null;
        try
        {
            // stream the output to whoever is listening.
            out = new OutputLoggerOutputStream(logger);
            context.setOutputStream(out);
            executeTask(hook, context, buildResult, resultNode, manual);
        }
        finally
        {
            IOUtils.close(out);
        }
        logger.hookCompleted(hook.getName());
    }

    private void executeTask(BuildHookConfiguration hook, ExecutionContext context, BuildResult buildResult, RecipeResultNode resultNode, boolean manual)
    {
        BuildHookTaskConfiguration task = hook.getTask();
        if (task != null)
        {
            try
            {
                task.execute(context, buildResult, resultNode);
            }
            catch (Exception e)
            {
                if (!manual)
                {
                    Result result = resultNode == null ? buildResult : resultNode.getResult();
                    result.addFeature(Feature.Level.ERROR, "Error executing task for hook '" + hook.getName() + "': " + e.getMessage());
                    if (hook.failOnError())
                    {
                        result.setState(ResultState.ERROR);
                    }
                }
            }
        }
    }

    public void setConfigurationManager(MasterConfigurationManager configurationManager)
    {
        this.configurationManager = configurationManager;
    }

    public void setMasterLocationProvider(MasterLocationProvider masterLocationProvider)
    {
        this.masterLocationProvider = masterLocationProvider;
    }
}
