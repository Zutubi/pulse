package com.zutubi.pulse.dev.local;

import com.opensymphony.util.TextUtils;
import com.zutubi.pulse.core.model.*;
import com.zutubi.pulse.core.postprocessors.api.Feature;

import java.util.List;

/**
 * Result printing code shared by local build and post-processing commands.
 */
public class PrintSupport
{
    public static void showFeatures(Indenter indenter, StoredFileArtifact fileArtifact)
    {
        for (Feature.Level level : Feature.Level.values())
        {
            List<PersistentFeature> features = fileArtifact.getFeatures(level);
            if (features.size() > 0)
            {
                indenter.indent();
                showFeatures(indenter, level, features);
                indenter.dedent();
            }
        }
    }

    public static void showFeatures(Indenter indenter, Feature.Level level, List<PersistentFeature> features)
    {
        indenter.println(level.toString().toLowerCase() + " features:");
        indenter.indent();

        for (PersistentFeature f : features)
        {
            indenter.println("* " + f.getSummary());
        }

        indenter.dedent();
    }

    public static String summariseTestCounts(TestResultSummary testSummary)
    {
        if (testSummary.allPassed())
        {
            return "all passed";
        }
        else
        {
            String result = testSummary.getPassed() + " passed";
            if (testSummary.getFailures() > 0)
            {
                result += ", " + testSummary.getFailures() + " failed";
            }
            if (testSummary.getErrors() > 0)
            {
                result += ", " + testSummary.getErrors() + " error" + (testSummary.getErrors() > 1 ? "s" : "");
            }
            return result;
        }
    }


    public static void showTestSuite(Indenter indenter, PersistentTestSuiteResult suiteResult)
    {
        showTestSuite(indenter, suiteResult, "");
    }

    private static void showTestSuite(Indenter indenter, PersistentTestSuiteResult suiteResult, String prefix)
    {
        if(suiteResult.getName() != null)
        {
            prefix += suiteResult.getName() + ".";
        }

        for(PersistentTestSuiteResult nested: suiteResult.getSuites())
        {
            showTestSuite(indenter, nested, prefix);
        }

        for(PersistentTestCaseResult caseResult: suiteResult.getCases())
        {
            String message = String.format("%s%-7s: %s", prefix, caseResult.getStatus().toString().toLowerCase(), caseResult.getName());
            if(TextUtils.stringSet(caseResult.getMessage()))
            {
                message += ": " + caseResult.getMessage();
            }

            indenter.println(message);
        }
    }
}
