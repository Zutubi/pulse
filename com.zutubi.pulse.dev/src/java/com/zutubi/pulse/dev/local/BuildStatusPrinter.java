package com.zutubi.pulse.dev.local;

import com.opensymphony.util.TextUtils;
import com.zutubi.events.Event;
import com.zutubi.events.EventListener;
import com.zutubi.pulse.core.events.*;
import com.zutubi.pulse.core.model.*;
import com.zutubi.util.TimeStamps;
import com.zutubi.util.io.ForkOutputStream;

import java.io.File;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.List;
import java.util.Locale;

/**
 * Prints status information to standard out while doing a local build.
 */
public class BuildStatusPrinter implements EventListener
{
    private Indenter indenter;
    private String baseDir;
    private File outputDir;
    private RecipeResult result;
    private Locale locale;
    private int failureLimit;

    public BuildStatusPrinter(File base, File output, OutputStream logStream, int failureLimit)
    {
        ForkOutputStream fork = new ForkOutputStream(System.out, logStream);
        indenter = new Indenter(new PrintStream(fork), "  ");
        baseDir = base.getAbsolutePath() + File.separatorChar;
        outputDir = output;
        this.failureLimit = failureLimit;
        result = new RecipeResult();
        locale = Locale.getDefault();        
    }


    public void handleEvent(Event event)
    {
        RecipeEvent recipeEvent = (RecipeEvent) event;

        if (recipeEvent instanceof RecipeCommencedEvent)
        {
            handleRecipeCommenced((RecipeCommencedEvent) recipeEvent);
        }
        else if (recipeEvent instanceof CommandCommencedEvent)
        {
            handleCommandCommenced((CommandCommencedEvent) recipeEvent);
        }
        else if (recipeEvent instanceof CommandCompletedEvent)
        {
            handleCommandCompleted((CommandCompletedEvent) recipeEvent);
        }
        else if (recipeEvent instanceof RecipeCompletedEvent)
        {
            handleRecipeCompleted((RecipeCompletedEvent) recipeEvent);
        }
        else if (recipeEvent instanceof RecipeErrorEvent)
        {
            handleRecipeError((RecipeErrorEvent) recipeEvent);
        }
    }

    private void handleRecipeCommenced(RecipeCommencedEvent event)
    {
        String recipeName = event.getName();

        if (recipeName == null)
        {
            recipeName = "<default>";
        }

        result.commence(event.getName(), event.getStartTime());

        indenter.println("[" + recipeName + "]");
        indenter.indent();
        indenter.println("commenced: " + TimeStamps.getPrettyDate(event.getStartTime(), locale));
    }

    private void handleCommandCommenced(CommandCommencedEvent event)
    {
        indenter.println("[" + event.getName() + "]");
        indenter.indent();
        indenter.println("commenced: " + TimeStamps.getPrettyDate(event.getStartTime(), locale));
    }

    private void handleCommandCompleted(CommandCompletedEvent event)
    {
        CommandResult commandResult = event.getResult();
        result.add(commandResult);

        indenter.println("completed: " + commandResult.getStamps().getPrettyEndDate(locale));
        indenter.println("elapsed  : " + commandResult.getStamps().getPrettyElapsed());
        indenter.println("result   : " + commandResult.getState().getPrettyString());

        showMessages(commandResult);

        List<StoredArtifact> artifacts = commandResult.getArtifacts();
        if (artifacts.size() > 0)
        {
            showArtifacts(commandResult, artifacts);
        }

        indenter.println();
        indenter.dedent();
    }

    private void showMessages(Result result)
    {
        if (result.hasDirectMessages(Feature.Level.ERROR))
        {
            indenter.println("errors   :");
            showMessages(result, Feature.Level.ERROR);
        }

        if (result.hasDirectMessages(Feature.Level.WARNING))
        {
            indenter.println("warnings :");
            showMessages(result, Feature.Level.WARNING);
        }

        if (result.hasDirectMessages(Feature.Level.INFO))
        {
            indenter.println("info     :");
            showMessages(result, Feature.Level.INFO);
        }
    }

    private void showMessages(Result result, Feature.Level level)
    {
        indenter.indent();
        for (Feature feature : result.getFeatures(level))
        {
            indenter.println(feature.getSummary());
        }
        indenter.dedent();
    }

    private void showArtifacts(CommandResult result, List<StoredArtifact> artifacts)
    {
        indenter.println("artifacts:");
        indenter.indent();
        for (StoredArtifact artifact : artifacts)
        {
            indenter.println("* " + artifact.getName());
            indenter.indent();

            for (StoredFileArtifact fileArtifact : artifact.getChildren())
            {
                indenter.println("* " + getFilePath(result, fileArtifact.getPath()));

                for (Feature.Level level : Feature.Level.values())
                {
                    List<Feature> features = fileArtifact.getFeatures(level);
                    if (features.size() > 0)
                    {
                        indenter.indent();
                        showFeatures(level, features);
                        indenter.dedent();
                    }
                }
            }

            indenter.dedent();
        }
        indenter.dedent();
    }

    private String getFilePath(CommandResult commandResult, String name)
    {
        File path = new File(commandResult.getOutputDir(), name);
        String result = path.getPath();

        if (result.startsWith(baseDir))
        {
            result = result.substring(baseDir.length());
        }

        return result;
    }

    private void showFeatures(Feature.Level level, List<Feature> features)
    {
        indenter.println(level.toString().toLowerCase() + " features:");
        indenter.indent();

        for (Feature f : features)
        {
            indenter.println("* " + f.getSummary());
        }

        indenter.dedent();
    }

    private void handleRecipeCompleted(RecipeCompletedEvent event)
    {
        result.update(event.getResult());
        complete();
    }

    private void handleRecipeError(RecipeErrorEvent event)
    {
        result.error(event.getErrorMessage());
        complete();
    }

    private void showTestResults()
    {
        TestResultSummary testSummary = result.getTestSummary();
        if(testSummary.hasTests())
        {
            String message = "tests    : " + testSummary.getTotal() + " (";
            if(testSummary.allPassed())
            {
                message += "all passed";
            }
            else
            {
                message += testSummary.getPassed() + " passed";
                if(testSummary.getFailures() > 0)
                {
                    message += ", " + testSummary.getFailures() + " failed";
                }
                if(testSummary.getErrors() > 0)
                {
                    message += ", " + testSummary.getErrors() + " error" + (testSummary.getErrors() > 1 ? "s" : "");
                }
            }

            message += ")";
            indenter.println(message);

            showtestFailures(testSummary);
        }
    }

    private void showtestFailures(TestResultSummary testSummary)
    {
        if (!testSummary.allPassed() && failureLimit > 0)
        {
            indenter.indent();
            if (testSummary.getBroken() > failureLimit)
            {
                indenter.println("NOTE: Test failure limit (" + failureLimit + ") reached, not all failures reported.");
            }

            TestSuitePersister persister = new TestSuitePersister();
            try
            {
                TestSuiteResult failedTests = persister.read(null, new File(outputDir, RecipeResult.TEST_DIR), true, true, failureLimit);
                showTestSuite(failedTests, "");
            }
            catch (Exception e)
            {
                indenter.println("Unable to load failed test results: " + e.getMessage());
            }
            indenter.dedent();
        }
    }

    private void showTestSuite(TestSuiteResult suiteResult, String prefix)
    {
        if(suiteResult.getName() != null)
        {
            prefix += suiteResult.getName() + ".";
        }

        for(TestSuiteResult nested: suiteResult.getSuites())
        {
            showTestSuite(nested, prefix);
        }

        for(TestCaseResult caseResult: suiteResult.getCases())
        {
            String message = String.format("%s%-7s: %s", prefix, caseResult.getStatus().toString().toLowerCase(), caseResult.getName());
            if(TextUtils.stringSet(caseResult.getMessage()))
            {
                message += ": " + caseResult.getMessage();
            }
            
            indenter.println(message);
        }
    }

    private void complete()
    {
        result.complete();

        indenter.println("completed: " + result.getStamps().getPrettyEndDate(locale));
        indenter.println("elapsed  : " + result.getStamps().getPrettyElapsed());
        indenter.println("result   : " + result.getState().getPrettyString());

        showMessages(result);

        showTestResults();

        indenter.dedent();
    }

    public Class[] getHandledEvents()
    {
        return new Class[]{RecipeEvent.class};
    }
}
