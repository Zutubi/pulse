package com.zutubi.pulse.core.model;

import com.zutubi.pulse.core.engine.api.Feature;
import com.zutubi.pulse.core.engine.api.ResultState;
import com.zutubi.util.FileSystemUtils;
import com.zutubi.util.StringUtils;
import com.zutubi.util.logging.Logger;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

/**
 * Stores the result of running a single recipe (sequence of commands).
 */
public class RecipeResult extends Result
{
    public static final String TEST_DIR = "tests";

    private static final Logger LOG = Logger.getLogger(RecipeResult.class);

    private String recipeName;
    private List<CommandResult> results = new LinkedList<CommandResult>();
    private TestResultSummary testSummary = new TestResultSummary();

    // This field is not persisted with the recipe result in the database, and needs to
    // be loaded separately via the loadFailedTestResults method.
    private PersistentTestSuiteResult failedTestResults;

    public RecipeResult()
    {
        state = ResultState.PENDING;
    }

    public RecipeResult(String recipeName)
    {
        this.recipeName = recipeName;
        state = ResultState.PENDING;
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
        // Adjust the command's output directory to the local one
        File remoteDir = new File(FileSystemUtils.localiseSeparators(result.getOutputDir()));
        File localDir = new File(getOutputDir(), remoteDir.getName());
        result.setOutputDir(localDir.getPath());

        // Update the result (always last in the list).
        CommandResult currentResult = results.remove(results.size() - 1);
        currentResult.update(result);

        // The add call checks for the command state
        add(currentResult);
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
        if (!StringUtils.stringSet(recipeName))
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

    public List<StoredArtifact> getArtifacts()
    {
        List<StoredArtifact> artifacts = new LinkedList<StoredArtifact>();
        for (CommandResult result : results)
        {
            artifacts.addAll(result.getArtifacts());
        }
        return artifacts;
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
        File outputDir = getAbsoluteOutputDir(dataRoot);
        return outputDir == null ? null : outputDir.getParentFile();
    }

    public PersistentTestSuiteResult getFailedTestResults()
    {
        return failedTestResults;
    }

    public void setFailedTestResults(PersistentTestSuiteResult failedTestResults)
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
        return testSummary.getBroken() - getLoadedTestFailures();
    }

    public int getLoadedTestFailures()
    {
        if (failedTestResults != null)
        {
            return failedTestResults.getSummary().getBroken();
        }
        return 0;
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
