package com.zutubi.pulse.master.tove.config.project.reports;

import com.zutubi.pulse.core.model.RecipeResult;
import com.zutubi.pulse.master.model.BuildResult;

/**
 * Metrics that can be extracted from individual build stages.
 *
 * @see com.zutubi.pulse.master.tove.config.project.reports.BuildMetric
 */
public enum StageMetric
{
    /**
     * The time taken to execute the stage, in seconds.
     */
    ELAPSED_TIME(true)
    {
        public void extractMetrics(BuildResult buildResult, RecipeResult recipeResult, StageReportSeriesConfiguration config, ReportContext context)
        {
            context.addMetricValue(config.getName(), buildResult, (double)recipeResult.getStamps().getElapsed());
        }
    },
    /**
     * The total number of tests run in the stage.
     */
    TEST_TOTAL_COUNT(false)
    {
        public void extractMetrics(BuildResult buildResult, RecipeResult recipeResult, StageReportSeriesConfiguration config, ReportContext context)
        {
            context.addMetricValue(config.getName(), buildResult, recipeResult.getTestSummary().getTotal());
        }
    },
    /**
     * The number of passed tests in the stage.
     */
    TEST_PASS_COUNT(false)
    {
        public void extractMetrics(BuildResult buildResult, RecipeResult recipeResult, StageReportSeriesConfiguration config, ReportContext context)
        {
            context.addMetricValue(config.getName(), buildResult, recipeResult.getTestSummary().getPassed());
        }
    },
    /**
     * The number of expected failure tests in the stage.
     */
    TEST_EXPECTED_FAIL_COUNT(false)
    {
        public void extractMetrics(BuildResult buildResult, RecipeResult recipeResult, StageReportSeriesConfiguration config, ReportContext context)
        {
            context.addMetricValue(config.getName(), buildResult, recipeResult.getTestSummary().getExpectedFailures());
        }
    },
    /**
     * The number of failed tests in the stage.
     */
    TEST_FAIL_COUNT(false)
    {
        public void extractMetrics(BuildResult buildResult, RecipeResult recipeResult, StageReportSeriesConfiguration config, ReportContext context)
        {
            context.addMetricValue(config.getName(), buildResult, recipeResult.getTestSummary().getFailures());
        }
    },
    /**
     * The number of errored tests in the stage.
     */
    TEST_ERROR_COUNT(false)
    {
        public void extractMetrics(BuildResult buildResult, RecipeResult recipeResult, StageReportSeriesConfiguration config, ReportContext context)
        {
            context.addMetricValue(config.getName(), buildResult, recipeResult.getTestSummary().getErrors());
        }
    },
    /**
     * The number of skipped tests in the stage.
     */
    TEST_SKIPPED_COUNT(false)
    {
        public void extractMetrics(BuildResult buildResult, RecipeResult recipeResult, StageReportSeriesConfiguration config, ReportContext context)
        {
            context.addMetricValue(config.getName(), buildResult, recipeResult.getTestSummary().getSkipped());
        }
    },
    /**
     * The number of broken (failed or errored) tests in the stage.
     */
    TEST_BROKEN_COUNT(false)
    {
        public void extractMetrics(BuildResult buildResult, RecipeResult recipeResult, StageReportSeriesConfiguration config, ReportContext context)
        {
            context.addMetricValue(config.getName(), buildResult, recipeResult.getTestSummary().getBroken());
        }
    },
    /**
     * The percentage of tests that succeeded.
     */
    TEST_SUCCESS_PERCENTAGE(false)
    {
        public void extractMetrics(BuildResult buildResult, RecipeResult recipeResult, StageReportSeriesConfiguration config, ReportContext context)
        {
            context.addMetricValue(config.getName(), buildResult, recipeResult.getTestSummary().getSuccessPercent());
        }
    },
    /**
     * The percentage of tests that failed.
     */
    TEST_FAILED_PERCENTAGE(false)
    {
        public void extractMetrics(BuildResult buildResult, RecipeResult recipeResult, StageReportSeriesConfiguration config, ReportContext context)
        {
            context.addMetricValue(config.getName(), buildResult, 100 - recipeResult.getTestSummary().getSuccessPercent());
        }
    },
    /**
     * The number of error features detected in the stage.
     */
    ERROR_COUNT(false)
    {
        public void extractMetrics(BuildResult buildResult, RecipeResult recipeResult, StageReportSeriesConfiguration config, ReportContext context)
        {
            context.addMetricValue(config.getName(), buildResult, recipeResult.getErrorFeatureCount());
        }
    },
    /**
     * The number of warning features detected in the stage.
     */
    WARNING_COUNT(false)
    {
        public void extractMetrics(BuildResult buildResult, RecipeResult recipeResult, StageReportSeriesConfiguration config, ReportContext context)
        {
            context.addMetricValue(config.getName(), buildResult, recipeResult.getWarningFeatureCount());
        }
    },
    /**
     * Returns 1 for broken stages, 0 for successful ones (useful to accumulate).
     */
    BROKEN_COUNT(false)
    {
        public void extractMetrics(BuildResult buildResult, RecipeResult recipeResult, StageReportSeriesConfiguration config, ReportContext context)
        {
            context.addMetricValue(config.getName(), buildResult, recipeResult.getState().isBroken() ? 1 : 0);
        }
    },
    /**
     * Returns 0 for broken stages, 1 for successful ones (useful to accumulate).
     */
    SUCCESS_COUNT(false)
    {
        public void extractMetrics(BuildResult buildResult, RecipeResult recipeResult, StageReportSeriesConfiguration config, ReportContext context)
        {
            context.addMetricValue(config.getName(), buildResult, recipeResult.getState().isBroken() ? 0 : 1);
        }
    },
    /**
     * The value of a custom field whose name is specified in the {@link StageReportSeriesConfiguration}.
     */
    CUSTOM_FIELD(false)
    {
        public void extractMetrics(BuildResult buildResult, RecipeResult recipeResult, StageReportSeriesConfiguration config, ReportContext context)
        {
            MetricUtils.extractMetrics(buildResult, recipeResult, config, context);
        }
    };

    private boolean timeBased;

    StageMetric(boolean timeBased)
    {
        this.timeBased = timeBased;
    }
    
    /**
     * Extracts values for this metric from the given stage, adding them to the given context.
     *
     * @param buildResult the build the stage belongs to
     * @param recipeResult the recipe result from the stage to extract the values from
     * @param config the series configuration this metric is being used for
     * @param context the context to add values to
     */
    public abstract void extractMetrics(BuildResult buildResult, RecipeResult recipeResult, StageReportSeriesConfiguration config, ReportContext context);

    public boolean isTimeBased()
    {
        return timeBased;
    }
}
