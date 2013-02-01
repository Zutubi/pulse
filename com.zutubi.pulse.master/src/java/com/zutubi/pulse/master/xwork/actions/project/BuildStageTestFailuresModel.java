package com.zutubi.pulse.master.xwork.actions.project;

import com.google.common.base.Function;
import com.zutubi.pulse.core.model.PersistentTestCaseResult;
import com.zutubi.pulse.core.model.PersistentTestResult;
import com.zutubi.pulse.core.model.PersistentTestSuiteResult;
import com.zutubi.pulse.core.model.RecipeResult;
import com.zutubi.pulse.master.model.BuildResult;
import com.zutubi.pulse.master.model.RecipeResultNode;
import com.zutubi.pulse.master.webwork.Urls;
import com.zutubi.util.CollectionUtils;
import com.zutubi.util.EnumUtils;
import com.zutubi.util.WebUtils;

import java.util.Collection;
import java.util.List;

/**
 * JSON data model for failed tests in a build stage.
 */
public class BuildStageTestFailuresModel
{
    private String name;
    private String testsUrl;
    private String recipeName;
    private String agentName;
    private String testSummary;
    private int excessFailureCount;
    private TestSuiteModel tests;

    public BuildStageTestFailuresModel(BuildResult buildResult, RecipeResultNode stageResult, Urls urls)
    {
        name = stageResult.getStageName();
        testsUrl = urls.stageTests(buildResult, stageResult);
        RecipeResult recipeResult = stageResult.getResult();
        recipeName = recipeResult.getRecipeNameSafe();
        agentName = stageResult.getAgentNameSafe();
        testSummary = stageResult.getTestSummary().toString();
        excessFailureCount = recipeResult.getExcessFailureCount();

        PersistentTestSuiteResult failures = recipeResult.getFailedTestResults();
        if (failures != null)
        {
            tests = new TestSuiteModel(failures);
        }
    }

    public String getName()
    {
        return name;
    }

    public String getSafeName()
    {
        return WebUtils.toValidHtmlName(name);
    }

    public String getTestsUrl()
    {
        return testsUrl;
    }

    public String getRecipeName()
    {
        return recipeName;
    }

    public String getAgentName()
    {
        return agentName;
    }

    public String getTestSummary()
    {
        return testSummary;
    }

    public int getExcessFailureCount()
    {
        return excessFailureCount;
    }

    public TestSuiteModel getTests()
    {
        return tests;
    }

    public static class TestResultModel
    {
        private String name;
        private String duration;

        public TestResultModel(String name, String duration)
        {
            this.name = name;
            this.duration = duration;
        }

        public TestResultModel(PersistentTestResult testResult)
        {
            this.name = testResult.getName();
            this.duration = testResult.getDuration() >= 0 ? testResult.getPrettyDuration() : null;
        }

        public String getName()
        {
            return name;
        }

        public String getDuration()
        {
            return duration;
        }
    }

    public static class TestSuiteModel extends TestResultModel
    {
        private List<TestSuiteModel> suites;
        private List<TestCaseModel> cases;

        public TestSuiteModel(String name, String duration)
        {
            super(name, duration);
        }

        public TestSuiteModel(PersistentTestSuiteResult suiteResult)
        {
            super(suiteResult);
            
            List<PersistentTestSuiteResult> childSuites = suiteResult.getSuites();
            if (childSuites.size() > 0)
            {
                suites = CollectionUtils.map(childSuites, new Function<PersistentTestSuiteResult, TestSuiteModel>()
                {
                    public TestSuiteModel apply(PersistentTestSuiteResult childSuite)
                    {
                        return new TestSuiteModel(childSuite);
                    }
                });
            }

            Collection<PersistentTestCaseResult> childCases = suiteResult.getCases();
            if (childCases.size() > 0)
            {
                cases = CollectionUtils.map(childCases, new Function<PersistentTestCaseResult, TestCaseModel>()
                {
                    public TestCaseModel apply(PersistentTestCaseResult childCase)
                    {
                        return new TestCaseModel(childCase);
                    }
                });
            }
        }

        public List<TestSuiteModel> getSuites()
        {
            return suites;
        }

        public List<TestCaseModel> getCases()
        {
            return cases;
        }
    }

    public static class TestCaseModel extends TestResultModel
    {
        private String status;
        private String message;
        private long brokenNumber;

        public TestCaseModel(PersistentTestCaseResult caseResult)
        {
            super(caseResult);
            status = EnumUtils.toPrettyString(caseResult.getStatus());
            message = caseResult.getMessage();
            brokenNumber = caseResult.getBrokenNumber();
        }

        public String getStatus()
        {
            return status;
        }

        public String getMessage()
        {
            return message;
        }

        public long getBrokenNumber()
        {
            return brokenNumber;
        }
    }
}
