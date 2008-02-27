package com.zutubi.pulse.prototype.config.project.hooks;

import com.zutubi.prototype.config.ConfigurationProvider;
import com.zutubi.pulse.MasterBuildProperties;
import com.zutubi.pulse.bootstrap.MasterConfigurationManager;
import com.zutubi.pulse.core.ExecutionContext;
import com.zutubi.pulse.core.model.Feature;
import com.zutubi.pulse.core.model.Result;
import com.zutubi.pulse.core.model.ResultState;
import com.zutubi.pulse.events.Event;
import com.zutubi.pulse.events.EventListener;
import com.zutubi.pulse.events.EventManager;
import com.zutubi.pulse.events.build.BuildEvent;
import com.zutubi.pulse.events.build.StageEvent;
import com.zutubi.pulse.model.BuildResult;
import com.zutubi.pulse.model.RecipeResultNode;
import com.zutubi.pulse.model.persistence.hibernate.HibernateBuildResultDao;
import com.zutubi.pulse.prototype.config.admin.GeneralAdminConfiguration;
import com.zutubi.util.UnaryProcedure;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Manages the execution of build hooks at various points in the build
 * process.
 */
public class BuildHookManager implements EventListener
{
    private ExecutorService executor = Executors.newSingleThreadExecutor();

    private MasterConfigurationManager configurationManager;
    private ConfigurationProvider configurationProvider;

    public void handleEvent(Event event)
    {
        BuildEvent be = (BuildEvent) event;
        BuildResult buildResult = be.getBuildResult();
        if (!buildResult.isPersonal())
        {
            ExecutionContext context = new ExecutionContext(be.getContext());
            for(BuildHookConfiguration hook: buildResult.getProject().getConfig().getBuildHooks().values())
            {
                if(hook.enabled() && hook.triggeredBy(be))
                {
                    RecipeResultNode resultNode = null;
                    if(be instanceof StageEvent)
                    {
                        resultNode = ((StageEvent)be).getStageNode();
                    }
                    executeTask(hook, context, be.getBuildResult(), resultNode, false);
                }
            }
        }
    }

    public void manualTrigger(final BuildHookConfiguration hook, final BuildResult result)
    {
        HibernateBuildResultDao.intialise(result);
        executor.execute(new Runnable()
        {
            public void run()
            {
                final ExecutionContext context = new ExecutionContext();
                MasterBuildProperties.addAllBuildProperties(context, result, configurationProvider.get(GeneralAdminConfiguration.class), configurationManager);
                if (hook.appliesTo(result))
                {
                    executeTask(hook, context, result, null, true);
                }

                result.getRoot().forEachNode(new UnaryProcedure<RecipeResultNode>()
                {
                    public void process(RecipeResultNode recipeResultNode)
                    {
                        if(hook.appliesTo(recipeResultNode))
                        {
                            context.push();
                            try
                            {
                                MasterBuildProperties.addStageProperties(context, result, recipeResultNode, configurationManager, false);
                                executeTask(hook, context, result, recipeResultNode, true);
                            }
                            finally
                            {
                                context.pop();
                            }
                        }
                    }
                });
            }
        });
    }

    private void executeTask(BuildHookConfiguration hook, ExecutionContext context, BuildResult buildResult, RecipeResultNode resultNode, boolean manual)
    {
        BuildHookTaskConfiguration task = hook.getTask();
        try
        {
            task.execute(context, buildResult, resultNode);
        }
        catch (Exception e)
        {
            if(!manual)
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

    public Class[] getHandledEvents()
    {
        return new Class[]{BuildEvent.class};
    }

    public void setEventManager(EventManager eventManager)
    {
        eventManager.register(this);
    }

    public void setConfigurationManager(MasterConfigurationManager configurationManager)
    {
        this.configurationManager = configurationManager;
    }

    public void setConfigurationProvider(ConfigurationProvider configurationProvider)
    {
        this.configurationProvider = configurationProvider;
    }
}
