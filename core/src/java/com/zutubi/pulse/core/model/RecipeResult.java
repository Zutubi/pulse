package com.zutubi.pulse.core.model;

import com.zutubi.pulse.util.FileSystemUtils;
import com.zutubi.util.logging.Logger;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

/**
 * 
 *
 */
public class RecipeResult extends Result
{
    public static final String RECIPE_LOG = "recipe.log";
    public static final String TEST_DIR = "tests";

    private static final Logger LOG = Logger.getLogger(RecipeResult.class);

    private String recipeName;
    private List<CommandResult> results = new LinkedList<CommandResult>();
    private TestSuiteResult failedTestResults;
    private TestResultSummary testSummary = new TestResultSummary();

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

        // And the test summary (test results come across on disk as they may be large)
        testSummary = result.testSummary;

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
                result.complete();
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

    public StoredArtifact getArtifact(String name)
    {
        for (CommandResult result : results)
        {
            StoredArtifact artifact = result.getArtifact(name);
            if (artifact != null)
            {
                return artifact;
            }
        }
        return null;
    }

    public void accumulateTestSummary(TestResultSummary summary)
    {
        if (testSummary != null)
        {
            summary.add(testSummary);
        }
    }

    public CommandResult getCommandResult(String name)
    {
        for(CommandResult r: results)
        {
            if(name.equals(r.getCommandName()))
            {
                return r;
            }
        }

        return null;
    }

    public File getRecipeDir(File dataRoot)
    {
        return getAbsoluteOutputDir(dataRoot).getParentFile();
    }

    public TestSuiteResult getFailedTestResults()
    {
        return failedTestResults;
    }

    public void setFailedTestResults(TestSuiteResult failedTestResults)
    {
        this.failedTestResults = failedTestResults;
    }

    public void loadFeatures(File dataRoot)
    {
        for(CommandResult command: results)
        {
            command.loadFeatures(getRecipeDir(dataRoot));
        }
    }

    public void loadFailedTestResults(File dataRoot, int limit)
    {
        File output = getAbsoluteOutputDir(dataRoot);
        File testDir = new File(output, TEST_DIR);
        if(testDir.exists())
        {
            try
            {
                TestSuitePersister persister = new TestSuitePersister();
                failedTestResults = persister.read(null, testDir, true, true, limit);
            }
            catch (Exception e)
            {
                // There is other info to show, don't totally explode
                // because we can't load the tests
                LOG.severe("Unable to load test results: " + e.getMessage(), e);
            }
        }
    }

    /**
     * As there is a limit imposed on the number of failed test cases that
     * are loaded, various views need to know if there are excess failures to
     * be reported.
     *
     * @return the number of test failures that have not been loaded
     */
    public int getExcessFailureCount()
    {
        int loadedCount = 0;

        if(failedTestResults != null)
        {
            loadedCount = failedTestResults.getSummary().getBroken();
        }

        return testSummary.getBroken() - loadedCount;
    }

    public TestResultSummary getTestSummary()
    {
        return testSummary;
    }

    public void setTestSummary(TestResultSummary testSummary)
    {
        this.testSummary = testSummary;
    }

    public boolean hasBrokenTests()
    {
        return testSummary != null && testSummary.getBroken() > 0;
    }

    /**
     * Calculate the feature counts for this result instance.
     */
    public void calculateFeatureCounts()
    {
        super.calculateFeatureCounts();

        for (CommandResult result: results)
        {
            result.calculateFeatureCounts();
            warningFeatureCount += result.getWarningFeatureCount();
            errorFeatureCount += result.getErrorFeatureCount();
        }
    }
}
