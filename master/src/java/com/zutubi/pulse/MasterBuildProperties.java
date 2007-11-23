package com.zutubi.pulse;

import com.zutubi.pulse.bootstrap.MasterConfigurationManager;
import com.zutubi.pulse.core.BuildProperties;
import com.zutubi.pulse.core.BuildRevision;
import com.zutubi.pulse.core.ExecutionContext;
import com.zutubi.pulse.core.ResourceRepository;
import com.zutubi.pulse.core.config.Resource;
import com.zutubi.pulse.core.config.ResourceProperty;
import com.zutubi.pulse.core.config.ResourceVersion;
import com.zutubi.pulse.core.model.CommandResult;
import com.zutubi.pulse.core.model.RecipeResult;
import com.zutubi.pulse.core.model.TestResultSummary;
import com.zutubi.pulse.core.scm.CheckoutScheme;
import com.zutubi.pulse.model.*;
import com.zutubi.pulse.prototype.config.admin.GeneralAdminConfiguration;
import com.zutubi.pulse.prototype.config.project.ProjectConfiguration;
import com.zutubi.util.TextUtils;

import java.io.File;
import java.util.Date;
import java.util.List;

/**
 * A utility extension of the build properties class with static members for
 * populating the build context with data that only makes sense on the
 * master.
 */
public class MasterBuildProperties extends BuildProperties
{
    public static void addProjectProperties(ExecutionContext context, ProjectConfiguration projectConfig)
    {
        for(ResourceProperty property: projectConfig.getProperties().values())
        {
            context.add(property);
        }
    }

    public static void addAllBuildProperties(ExecutionContext context, BuildResult result, GeneralAdminConfiguration adminConfiguration, MasterConfigurationManager configurationManager)
    {
        String masterUrl = MasterAgentService.constructMasterUrl(adminConfiguration, configurationManager.getSystemConfig());
        addBuildProperties(context, result, result.getProject(), result.getAbsoluteOutputDir(configurationManager.getDataDirectory()), masterUrl);
        if(result.getRevision() != null)
        {
            context.addInternalString(PROPERTY_BUILD_REVISION, result.getRevision().getRevisionString());
        }
        context.addInternalString(PROPERTY_BUILD_TIMESTAMP, TIMESTAMP_FORMAT.format(result.getStamps().getStartTime()));
        context.addInternalString(PROPERTY_BUILD_TIMESTAMP_MILLIS, Long.toString(result.getStamps().getStartTime()));
        addCompletedBuildProperties(context, result, configurationManager);
    }

    public static void addBuildProperties(ExecutionContext context, BuildResult buildResult, Project project, File buildDir, String masterUrl)
    {
        ProjectConfiguration projectConfig = project.getConfig();
        context.addInternalString(PROPERTY_BUILD_NUMBER, Long.toString(buildResult.getNumber()));
        context.addInternalString(PROPERTY_PROJECT, projectConfig.getName());
        context.addInternalString(PROPERTY_BUILD_DIRECTORY, buildDir.getAbsolutePath());

        BuildReason buildReason = buildResult.getReason();
        context.addInternalString(PROPERTY_BUILD_REASON, buildReason.getSummary());
        if(buildReason instanceof TriggerBuildReason)
        {
            context.addInternalString(PROPERTY_BUILD_TRIGGER, ((TriggerBuildReason)buildReason).getTriggerName());
        }

        context.addInternalString(PROPERTY_MASTER_URL, masterUrl);
        context.addInternalString(PROPERTY_BUILD_COUNT, Integer.toString(project.getBuildCount()));
        context.addInternalString(PROPERTY_SUCCESS_COUNT, Integer.toString(project.getSuccessCount()));

        CheckoutScheme checkoutScheme = projectConfig.getScm().getCheckoutScheme();
        context.addInternalString(PROPERTY_INCREMENTAL_BUILD, Boolean.toString(!buildResult.isPersonal() && checkoutScheme == CheckoutScheme.INCREMENTAL_UPDATE));

        context.addInternalString(PROPERTY_COMPRESS_ARTIFACTS, "true");
        context.addInternalString(PROPERTY_COMPRESS_WORKING_DIR, Boolean.toString(projectConfig.getOptions().getRetainWorkingCopy()));

        addProjectProperties(context, projectConfig);
    }

    public static void addRevisionProperties(ExecutionContext context, BuildRevision buildRevision)
    {
        context.addInternalString(PROPERTY_BUILD_REVISION, buildRevision.getRevision().getRevisionString());
        context.addInternalString(PROPERTY_BUILD_TIMESTAMP, TIMESTAMP_FORMAT.format(new Date(buildRevision.getTimestamp())));
        context.addInternalString(PROPERTY_BUILD_TIMESTAMP_MILLIS, Long.toString(buildRevision.getTimestamp()));
    }

    public static void addResourceProperties(ExecutionContext context, List<ResourceRequirement> resourceRequirements, ResourceRepository resourceRepository)
    {
        if (resourceRequirements != null)
        {
            for(ResourceRequirement requirement: resourceRequirements)
            {
                Resource resource = resourceRepository.getResource(requirement.getResource());
                if(resource == null)
                {
                    return;
                }

                for(ResourceProperty property: resource.getProperties().values())
                {
                    context.add(property);
                }

                String importVersion = requirement.getVersion();
                if(importVersion == null)
                {
                    importVersion = resource.getDefaultVersion();
                }

                if(TextUtils.stringSet(importVersion))
                {
                    ResourceVersion version = resource.getVersion(importVersion);
                    if(version == null)
                    {
                        return;
                    }

                    for(ResourceProperty property: version.getProperties().values())
                    {
                        context.add(property);
                    }
                }
            }
        }
    }

    public static void addCompletedBuildProperties(ExecutionContext context, BuildResult result, MasterConfigurationManager configurationManager)
    {
        context.addInternalString(PROPERTY_STATUS, result.getState().getString());

        TestResultSummary tests = result.getTestSummary();
        String testSummary;
        if(tests.getTotal() > 0)
        {
            if(tests.allPassed())
            {
                testSummary = "all " + tests.getTotal() + " tests passed";
            }
            else
            {
                testSummary = Integer.toString(tests.getBroken()) + " of " + tests.getTotal() + " tests broken";
            }
        }
        else
        {
            testSummary = "no tests";
        }

        context.addInternalString(PROPERTY_TEST_SUMMARY, testSummary);
        for(RecipeResultNode node: result.getRoot().getChildren())
        {
            addStageProperties(context, result, node, configurationManager, true);
        }
    }

    public static void addStageProperties(ExecutionContext context, BuildResult result, RecipeResultNode node, MasterConfigurationManager configurationManager, boolean includeName)
    {
        MasterBuildPaths paths = new MasterBuildPaths(configurationManager);

        String name = node.getStageName();
        String prefix = "stage.";

        RecipeResult recipeResult = node.getResult();
        if(includeName)
        {
            prefix += name + ".";
        }
        else
        {
            context.addInternalString(PROPERTY_RECIPE, recipeResult.getRecipeNameSafe());
            context.addInternalString(PROPERTY_STAGE, node.getStageName());
            context.addInternalString(PROPERTY_STATUS, recipeResult.getState().getString());
        }

        context.addInternalString(prefix + PROPERTY_AGENT, node.getHostSafe());
        if(result != null)
        {
            context.addInternalString(prefix + PROPERTY_RECIPE, recipeResult.getRecipeNameSafe());
            context.addInternalString(prefix + PROPERTY_STATUS, recipeResult.getState().getString());
            context.addInternalString(prefix + PROPERTY_DIRECTORY, paths.getRecipeDir(result, recipeResult.getId()).getAbsolutePath());

            for(CommandResult command: recipeResult.getCommandResults())
            {
                addCommandProperties(context, node, command, configurationManager, includeName);
            }
        }
    }

    private static void addCommandProperties(ExecutionContext context, RecipeResultNode node, CommandResult commandResult, MasterConfigurationManager configurationManager, boolean includeName)
    {
        String stageName = node.getStageName();
        String commandName = commandResult.getCommandName();
        String prefix = "";

        if(includeName)
        {
            prefix = "stage." + stageName + ".";
        }

        prefix += "command." + commandName + ".";

        context.addInternalString(prefix + PROPERTY_STATUS, commandResult.getState().getString());
        context.addInternalString(prefix + PROPERTY_DIRECTORY, commandResult.getAbsoluteOutputDir(configurationManager.getDataDirectory()).getAbsolutePath());
    }
}
