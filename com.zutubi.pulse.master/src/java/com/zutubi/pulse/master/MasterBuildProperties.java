package com.zutubi.pulse.master;

import com.zutubi.i18n.Messages;
import com.zutubi.pulse.core.engine.api.BuildProperties;
import com.zutubi.pulse.core.engine.api.ExecutionContext;
import com.zutubi.pulse.core.model.CommandResult;
import com.zutubi.pulse.core.model.RecipeResult;
import com.zutubi.pulse.core.postprocessors.api.PostProcessorConfiguration;
import com.zutubi.pulse.core.resources.api.ResourcePropertyConfiguration;
import com.zutubi.pulse.core.scm.config.api.CheckoutScheme;
import com.zutubi.pulse.master.agent.MasterLocationProvider;
import com.zutubi.pulse.master.bootstrap.MasterConfigurationManager;
import com.zutubi.pulse.master.model.*;
import com.zutubi.pulse.master.tove.config.project.BuildStageConfiguration;
import com.zutubi.pulse.master.tove.config.project.ProjectConfiguration;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * A utility extension of the build properties class with static members for
 * populating the build context with data that only makes sense on the
 * master.
 */
public class MasterBuildProperties extends BuildProperties
{
    private static final Messages I18N = Messages.getInstance(MasterBuildProperties.class);

    public static void addProjectProperties(ExecutionContext context, ProjectConfiguration projectConfiguration)
    {
        for(ResourcePropertyConfiguration property: projectConfiguration.getProperties().values())
        {
            context.add(property.asResourceProperty());
        }

        for (PostProcessorConfiguration postProcessor: projectConfiguration.getPostProcessors().values())
        {
            context.addValue(postProcessor.getName(), postProcessor);
        }
    }

    public static void addAllBuildProperties(ExecutionContext context, BuildResult result, MasterLocationProvider masterLocationProvider, MasterConfigurationManager configurationManager)
    {
        addBuildProperties(context, result, result.getProject(), result.getAbsoluteOutputDir(configurationManager.getDataDirectory()), masterLocationProvider.getMasterUrl());
        if(result.getRevision() != null)
        {
            context.addString(NAMESPACE_INTERNAL, PROPERTY_BUILD_REVISION, result.getRevision().getRevisionString());
        }
        context.addString(NAMESPACE_INTERNAL, PROPERTY_BUILD_TIMESTAMP, new SimpleDateFormat(TIMESTAMP_FORMAT_STRING).format(result.getStamps().getStartTime()));
        context.addString(NAMESPACE_INTERNAL, PROPERTY_BUILD_TIMESTAMP_MILLIS, Long.toString(result.getStamps().getStartTime()));
        addProjectProperties(context, result.getProject().getConfig());
        addCompletedBuildProperties(context, result, configurationManager);
    }

    public static void addBuildProperties(ExecutionContext context, BuildResult buildResult, Project project, File buildOutputDir, String masterUrl)
    {
        ProjectConfiguration projectConfig = project.getConfig();
        context.addString(NAMESPACE_INTERNAL, PROPERTY_BUILD_NUMBER, Long.toString(buildResult.getNumber()));
        context.addString(NAMESPACE_INTERNAL, PROPERTY_PROJECT, projectConfig.getName());
        context.addString(NAMESPACE_INTERNAL, PROPERTY_ORGANISATION, projectConfig.getOrganisation());
        context.addValue(NAMESPACE_INTERNAL, PROPERTY_PROJECT_HANDLE, projectConfig.getHandle());
        if (buildOutputDir != null)
        {
            context.addString(NAMESPACE_INTERNAL, PROPERTY_BUILD_DIRECTORY, buildOutputDir.getAbsolutePath());
        }

        BuildReason buildReason = buildResult.getReason();
        context.addString(NAMESPACE_INTERNAL, PROPERTY_BUILD_REASON, buildReason.getSummary());
        if(buildReason instanceof TriggerBuildReason)
        {
            context.addString(NAMESPACE_INTERNAL, PROPERTY_BUILD_TRIGGER, ((TriggerBuildReason)buildReason).getTriggerName());
        }

        context.addString(NAMESPACE_INTERNAL, PROPERTY_LOCAL_BUILD, Boolean.toString(false));
        context.addString(NAMESPACE_INTERNAL, PROPERTY_PERSONAL_BUILD, Boolean.toString(buildResult.isPersonal()));

        context.addString(NAMESPACE_INTERNAL, PROPERTY_MASTER_URL, masterUrl);
        context.addString(NAMESPACE_INTERNAL, PROPERTY_BUILD_COUNT, Integer.toString(project.getBuildCount()));
        context.addString(NAMESPACE_INTERNAL, PROPERTY_SUCCESS_COUNT, Integer.toString(project.getSuccessCount()));

        String owner;
        if (buildResult.isPersonal())
        {
            owner = ((User) buildResult.getOwner()).getLogin();
            context.addString(NAMESPACE_INTERNAL, PROPERTY_PERSONAL_USER, owner);
        }
        else
        {
            owner = buildResult.getProject().getName();
        }
        context.addString(NAMESPACE_INTERNAL, PROPERTY_OWNER, owner);

        CheckoutScheme checkoutScheme = projectConfig.getScm().getCheckoutScheme();
        context.addString(NAMESPACE_INTERNAL, PROPERTY_INCREMENTAL_BOOTSTRAP, Boolean.toString(!buildResult.isPersonal() && checkoutScheme != CheckoutScheme.CLEAN_CHECKOUT));
        context.addString(NAMESPACE_INTERNAL, PROPERTY_INCREMENTAL_BUILD, Boolean.toString(!buildResult.isPersonal() && checkoutScheme == CheckoutScheme.INCREMENTAL_UPDATE));

        context.addString(NAMESPACE_INTERNAL, PROPERTY_COMPRESS_ARTIFACTS, Boolean.toString(true));
        context.addString(NAMESPACE_INTERNAL, PROPERTY_PERSISTENT_WORK_PATTERN, projectConfig.getOptions().getPersistentWorkDir());
    }

    public static void addRevisionProperties(ExecutionContext context, BuildResult buildResult)
    {
        context.addString(NAMESPACE_INTERNAL, PROPERTY_BUILD_REVISION, buildResult.getRevision().getRevisionString());
        context.addString(NAMESPACE_INTERNAL, PROPERTY_BUILD_TIMESTAMP, new SimpleDateFormat(TIMESTAMP_FORMAT_STRING).format(new Date(buildResult.getStartTime())));
        context.addString(NAMESPACE_INTERNAL, PROPERTY_BUILD_TIMESTAMP_MILLIS, Long.toString(buildResult.getStartTime()));
    }

    public static void addCompletedBuildProperties(ExecutionContext context, BuildResult result, MasterConfigurationManager configurationManager)
    {
        context.addString(NAMESPACE_INTERNAL, PROPERTY_STATUS, result.getState().getString());
        context.addString(NAMESPACE_INTERNAL, PROPERTY_TEST_SUMMARY, result.getTestSummary().format(I18N));
        for (RecipeResultNode node: result.getStages())
        {
            addStageProperties(context, result, node, configurationManager, true);
            addCompletedStageProperties(context, result, node, configurationManager, true);
        }
    }

    public static void addStageProperties(ExecutionContext context, BuildResult result, RecipeResultNode node, MasterConfigurationManager configurationManager, boolean includeName)
    {
        String prefix = getStagePropertyPrefix(node, includeName);

        RecipeResult recipeResult = node.getResult();
        if (!includeName)
        {
            context.addString(NAMESPACE_INTERNAL, PROPERTY_AGENT, node.getAgentNameSafe());
            context.addString(NAMESPACE_INTERNAL, PROPERTY_RECIPE, recipeResult.getRecipeNameSafe());
            context.addString(NAMESPACE_INTERNAL, PROPERTY_STAGE, node.getStageName());
            context.addValue(NAMESPACE_INTERNAL, PROPERTY_STAGE_HANDLE, node.getStageHandle());
        }
        
        context.addString(NAMESPACE_INTERNAL, prefix + PROPERTY_AGENT, node.getAgentNameSafe());
        if (result != null)
        {
            BuildStageConfiguration stage = result.getProject().getConfig().getStage(node.getStageName());
            if (stage != null)
            {
                for (ResourcePropertyConfiguration property: stage.getProperties().values())
                {
                    if (includeName)
                    {
                        context.addString(prefix + property.getName(), property.getValue());
                    }
                    else
                    {
                        context.add(property.asResourceProperty());
                    }
                }
            }

            context.addString(NAMESPACE_INTERNAL, prefix + PROPERTY_RECIPE, recipeResult.getRecipeNameSafe());
            File recipeDir = recipeResult.getRecipeDir(configurationManager.getDataDirectory());
            if (recipeDir != null)
            {
                context.addString(NAMESPACE_INTERNAL, prefix + SUFFIX_DIRECTORY, recipeDir.getAbsolutePath());
            }

            for (CommandResult command: recipeResult.getCommandResults())
            {
                addCommandProperties(context, node, command, configurationManager, includeName);
            }
        }
    }

    public static void addCompletedStageProperties(ExecutionContext context, BuildResult result, RecipeResultNode node, MasterConfigurationManager configurationManager, boolean includeName)
    {
        String prefix = getStagePropertyPrefix(node, includeName);

        RecipeResult recipeResult = node.getResult();
        if (!includeName)
        {
            context.addString(NAMESPACE_INTERNAL, PROPERTY_STATUS, recipeResult.getState().getString());
        }

        if (result != null)
        {
            context.addString(NAMESPACE_INTERNAL, prefix + PROPERTY_STATUS, recipeResult.getState().getString());
            for (CommandResult command: recipeResult.getCommandResults())
            {
                addCompletedCommandProperties(context, node, command, configurationManager, includeName);
            }
        }
    }

    private static String getStagePropertyPrefix(RecipeResultNode node, boolean includeName)
    {
        String name = node.getStageName();
        String prefix = "stage.";

        if (includeName)
        {
            prefix += name + ".";
        }
        return prefix;
    }

    private static void addCommandProperties(ExecutionContext context, RecipeResultNode node, CommandResult commandResult, MasterConfigurationManager configurationManager, boolean includeName)
    {
        String prefix = getCommentPropertyPrefix(node, commandResult, includeName);

        File outputDir = commandResult.getAbsoluteOutputDir(configurationManager.getDataDirectory());
        if (outputDir != null)
        {
            context.addString(NAMESPACE_INTERNAL, prefix + SUFFIX_DIRECTORY, outputDir.getAbsolutePath());
        }
    }

    private static void addCompletedCommandProperties(ExecutionContext context, RecipeResultNode node, CommandResult commandResult, MasterConfigurationManager configurationManager, boolean includeName)
    {
        String prefix = getCommentPropertyPrefix(node, commandResult, includeName);
        context.addString(NAMESPACE_INTERNAL, prefix + PROPERTY_STATUS, commandResult.getState().getString());
    }

    private static String getCommentPropertyPrefix(RecipeResultNode node, CommandResult commandResult, boolean includeName)
    {
        String stageName = node.getStageName();
        String commandName = commandResult.getCommandName();
        String prefix = "";

        if (includeName)
        {
            prefix = "stage." + stageName + ".";
        }

        prefix += "command." + commandName + ".";
        return prefix;
    }
}
