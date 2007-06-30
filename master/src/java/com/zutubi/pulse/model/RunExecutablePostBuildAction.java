package com.zutubi.pulse.model;

import com.zutubi.pulse.MasterBuildPaths;
import com.zutubi.pulse.bootstrap.MasterConfigurationManager;
import com.zutubi.pulse.core.FileLoadException;
import com.zutubi.pulse.core.Scope;
import com.zutubi.pulse.core.VariableHelper;
import com.zutubi.pulse.core.model.*;
import com.zutubi.pulse.util.IOUtils;

import java.util.LinkedList;
import java.util.List;

/**
 * A post build action to run an executable.
 */
public class RunExecutablePostBuildAction extends PostBuildAction
{
    private String command;
    private String arguments;
    private MasterConfigurationManager configurationManager;

    public RunExecutablePostBuildAction()
    {
    }

    public RunExecutablePostBuildAction(String name, List<BuildSpecification> specifications, List<ResultState> states, boolean failOnError, String command, String arguments)
    {
        super(name, specifications, states, failOnError);
        this.command = command;
        this.arguments = arguments;
    }

    protected void internalExecute(BuildResult build, RecipeResultNode recipe, List<ResourceProperty> properties)
    {
        Process child = null;
        try
        {
            List<String> commandLine = new LinkedList<String>();
            commandLine.add(command);
            addArguments(commandLine, build, recipe, properties);

            ProcessBuilder builder = new ProcessBuilder(commandLine);
            child = builder.start();
            IOUtils.joinStreams(child.getInputStream(), System.out);
            int code = child.waitFor();
            if(code != 0)
            {
                addError("Command exited with non-zero exit code (" + code + ")");
            }
        }
        catch (Exception e)
        {
            addError(e.getMessage());
        }
        finally
        {
            if (child != null)
            {
                child.destroy();
            }
        }
    }

    public String getType()
    {
        return "run executable";
    }

    public PostBuildAction copy()
    {
        RunExecutablePostBuildAction copy = new RunExecutablePostBuildAction();
        copyCommon(copy);
        copy.command = command;
        copy.arguments = arguments;

        return copy;
    }

    private void addArguments(List<String> commandLine, BuildResult build, RecipeResultNode recipe, List<ResourceProperty> properties) throws FileLoadException
    {
        Scope scope = getScope(build, recipe, properties, configurationManager);
        commandLine.addAll(VariableHelper.splitAndReplaceVariables(arguments, scope, true));
    }

    public static Scope getScope(BuildResult result, RecipeResultNode recipe, List<ResourceProperty> properties, MasterConfigurationManager configurationManager)
    {
        MasterBuildPaths paths = new MasterBuildPaths(configurationManager);

        Scope scope = new Scope();
        scope.add(properties);
        
        scope.add(new Property("project", result.getProject().getName()));
        scope.add(new Property("number", Long.toString(result.getNumber())));

        BuildScmDetails buildScmDetails = result.getScmDetails();
        if(buildScmDetails != null && buildScmDetails.getRevision() != null)
        {
            scope.add(new Property("revision", buildScmDetails.getRevision().getRevisionString()));
        }
        
        scope.add(new Property("specification", result.getBuildSpecification()));
        scope.add(new Property("build.dir", paths.getBuildDir(result).getAbsolutePath()));

        if(recipe == null)
        {
            scope.add(new Property("status", result.getState().getString()));
            scope.add(new Property("reason", result.getReason().getSummary()));

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

            scope.add(new Property("test.summary", testSummary));

            for(RecipeResultNode node: result.getRoot().getChildren())
            {
                addStageProperties(result, node, scope, paths, configurationManager, true);
            }
        }
        else
        {
            addStageProperties(result, recipe, scope, paths, configurationManager, false);
        }

        return scope;
    }

    private static void addStageProperties(BuildResult result, RecipeResultNode node, Scope scope, MasterBuildPaths paths, MasterConfigurationManager configurationManager, boolean includeName)
    {
        String name = node.getStage();
        String prefix = "stage.";

        if(includeName)
        {
            prefix += name + ".";
        }

        RecipeResult recipeResult = node.getResult();
        scope.add(new Property(prefix + "agent", node.getHostSafe()));
        if(result != null)
        {
            scope.add(new Property(prefix + "recipe", recipeResult.getRecipeNameSafe()));
            scope.add(new Property(prefix + "status", recipeResult.getState().getString()));
            scope.add(new Property(prefix + "dir", paths.getRecipeDir(result, recipeResult.getId()).getAbsolutePath()));

            for(CommandResult command: recipeResult.getCommandResults())
            {
                addCommandProperties(node, command, scope, configurationManager, includeName);
            }
        }
    }

    private static void addCommandProperties(RecipeResultNode node, CommandResult commandResult, Scope scope, MasterConfigurationManager configurationManager, boolean includeName)
    {
        String stageName = node.getStage();
        String commandName = commandResult.getCommandName();
        String prefix = "";

        if(includeName)
        {
            prefix = "stage." + stageName + ".";
        }

        prefix += "command." + commandName + ".";

        scope.add(new Property(prefix + "status", commandResult.getState().getString()));
        scope.add(new Property(prefix + "dir", commandResult.getAbsoluteOutputDir(configurationManager.getDataDirectory()).getAbsolutePath()));
    }

    public String getCommand()
    {
        return command;
    }

    public void setCommand(String command)
    {
        this.command = command;
    }

    public String getArguments()
    {
        return arguments;
    }

    public void setArguments(String arguments)
    {
        this.arguments = arguments;
    }

    public void setConfigurationManager(MasterConfigurationManager configurationManager)
    {
        this.configurationManager = configurationManager;
    }
}
