package com.zutubi.pulse.core.model;

import com.zutubi.pulse.util.FileSystemUtils;
import com.zutubi.pulse.util.Sort;

import java.io.File;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

/**
 * 
 *
 */
public class RecipeResult extends Result
{
    public static final String RECIPE_LOG = "recipe.log";

    private String recipeName;
    private List<CommandResult> results = new LinkedList<CommandResult>();

    public RecipeResult()
    {
        state = ResultState.INITIAL;
    }

    public RecipeResult(String recipeName)
    {
        this.recipeName = recipeName;
        state = ResultState.INITIAL;
    }

    public void commence(String recipeName, long startTime)
    {
        this.recipeName = recipeName;
        super.commence(startTime);
    }

    public void add(CommandResult result)
    {
        results.add(result);

        if (state != ResultState.ERROR)
        {
            switch (result.state)
            {
                case ERROR:
                    error("Error executing command '" + result.getCommandName() + "'");
                    break;
                case FAILURE:
                    if (state != ResultState.FAILURE)
                    {
                        failure("Command '" + result.getCommandName() + "' failed");
                    }
                    break;
            }
        }
    }

    public void update(CommandResult result)
    {
        // lets save this command result by replacing the existing persistent result
        // with the new one... simple.
        CommandResult currentResult = results.remove(results.size() - 1);
        result.setId(currentResult.getId());
        result.getStamps().setStartTime(currentResult.getStamps().getStartTime());
        add(result);

        // Adjust the command's output directory to the local one
        File remoteDir = new File(FileSystemUtils.localiseSeparators(result.getOutputDir()));
        File localDir = new File(getOutputDir(), remoteDir.getName());
        result.setOutputDir(localDir.getPath());
    }

    public void update(RecipeResult result)
    {
        // Update our state to the worse of our current state and the state
        // of the incoming result.
        switch (result.state)
        {
            case ERROR:
                state = ResultState.ERROR;
                break;
            case FAILURE:
                if (state != ResultState.ERROR)
                {
                    state = ResultState.FAILURE;
                }
                break;
        }

        // Copy across features
        features.addAll(result.features);

        this.stamps.setEndTime(result.stamps.getEndTime());
    }

    public List<CommandResult> getCommandResults()
    {
        return results;
    }

    private void setCommandResults(List<CommandResult> results)
    {
        this.results = results;
    }

    public String getRecipeName()
    {
        return recipeName;
    }

    public String getRecipeNameSafe()
    {
        return getRecipeSafe(recipeName);
    }

    private void setRecipeName(String recipeName)
    {
        this.recipeName = recipeName;
    }

    public static String getRecipeSafe(String recipeName)
    {
        if (recipeName == null)
        {
            return "[default]";
        }
        else
        {
            return recipeName;
        }
    }

    public void abortUnfinishedCommands()
    {
        for (CommandResult result : results)
        {
            if (!result.completed())
            {
                result.error("Build aborted");
            }
        }
    }

    public List<String> collectErrors()
    {
        List<String> errors = super.collectErrors();
        for (CommandResult result : results)
        {
            errors.addAll(result.collectErrors());
        }
        return errors;
    }

    public boolean hasMessages(Feature.Level level)
    {
        if (hasDirectMessages(level))
        {
            return true;
        }

        for (CommandResult result : results)
        {
            if (result.hasMessages(level))
            {
                return true;
            }
        }

        return false;
    }

    public boolean hasArtifacts()
    {
        for (CommandResult result : results)
        {
            if (result.hasArtifacts())
            {
                return true;
            }
        }

        return false;
    }

    public void accumulateTestSummary(TestResultSummary summary)
    {
        for (CommandResult c : results)
        {
            c.accumulateTestSummary(summary);
        }
    }

    public List<TestResult> getAllTestResults()
    {
        List<TestResult> tests = new LinkedList<TestResult>();
        for(CommandResult command: results)
        {
            command.addAllTestResults(tests);
        }

        final Comparator<String> packageComp = new Sort.PackageComparator();

        Collections.sort(tests, new Comparator<TestResult>()
        {
            public int compare(TestResult t1, TestResult t2)
            {
                return packageComp.compare(t1.getName(), t2.getName());
            }
        });

        return tests;
    }
}
