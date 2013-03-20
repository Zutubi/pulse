package com.zutubi.pulse.master.tove.config.project.reports;

import com.zutubi.pulse.master.model.BuildResult;
import com.zutubi.util.logging.Logger;

/**
 * Metrics that apply to a whole build result.
 *
 * @see StageMetric
 */
public enum BuildMetric
{
    /**
     * The time the build took to execute, in seconds.
     */
    ELAPSED_TIME(true)
            {
                public void extractMetrics(BuildResult buildResult, BuildReportSeriesConfiguration config, ReportContext context)
                {
                    context.addMetricValue(config.getName(), buildResult, buildResult.getStamps().getElapsed());
                }
            },
    /**
     * The total number of tests in the build.
     */
    TEST_TOTAL_COUNT(false)
            {
                public void extractMetrics(BuildResult buildResult, BuildReportSeriesConfiguration config, ReportContext context)
                {
                    context.addMetricValue(config.getName(), buildResult, buildResult.getTestSummary().getTotal());
        }
    },
    /**
     * The total number of passed tests in the build.
     */
    TEST_PASS_COUNT(false)
    {
        public void extractMetrics(BuildResult buildResult, BuildReportSeriesConfiguration config, ReportContext context)
        {
            context.addMetricValue(config.getName(), buildResult, buildResult.getTestSummary().getPassed());
        }
    },
    /**
     * The total number of expected failure tests in the build.
     */
    TEST_EXPECTED_FAIL_COUNT(false)
    {
        public void extractMetrics(BuildResult buildResult, BuildReportSeriesConfiguration config, ReportContext context)
        {
            context.addMetricValue(config.getName(), buildResult, buildResult.getTestSummary().getExpectedFailures());
        }
    },
    /**
     * The total number of failed tests in the build.
     */
    TEST_FAIL_COUNT(false)
    {
        public void extractMetrics(BuildResult buildResult, BuildReportSeriesConfiguration config, ReportContext context)
        {
            context.addMetricValue(config.getName(), buildResult, buildResult.getTestSummary().getFailures());
        }
    },
    /**
     * The total number of errored tests in the build.
     */
    TEST_ERROR_COUNT(false)
    {
        public void extractMetrics(BuildResult buildResult, BuildReportSeriesConfiguration config, ReportContext context)
        {
            context.addMetricValue(config.getName(), buildResult, buildResult.getTestSummary().getErrors());
        }
    },
    /**
     * The total number of skipped tests in the build.
     */
    TEST_SKIPPED_COUNT(false)
    {
        public void extractMetrics(BuildResult buildResult, BuildReportSeriesConfiguration config, ReportContext context)
        {
            context.addMetricValue(config.getName(), buildResult, buildResult.getTestSummary().getSkipped());
        }
    },
    /**
     * The total number of broken (failed or errored) tests in the build.
     */
    TEST_BROKEN_COUNT(false)
    {
        public void extractMetrics(BuildResult buildResult, BuildReportSeriesConfiguration config, ReportContext context)
        {
            context.addMetricValue(config.getName(), buildResult, buildResult.getTestSummary().getBroken());
        }
    },
    /**
     * The percentage of tests that succeeded.
     */
    TEST_SUCCESS_PERCENTAGE(false)
    {
        public void extractMetrics(BuildResult buildResult, BuildReportSeriesConfiguration config, ReportContext context)
        {
            context.addMetricValue(config.getName(), buildResult, buildResult.getTestSummary().getSuccessPercent());
        }
    },
    /**
     * The percentage of tests that were broken.
     */
    TEST_BROKEN_PERCENTAGE(false)
    {
        public void extractMetrics(BuildResult buildResult, BuildReportSeriesConfiguration config, ReportContext context)
        {
            context.addMetricValue(config.getName(), buildResult, 100 - buildResult.getTestSummary().getSuccessPercent());
        }
    },
    /**
     * The number of error features detected.
     */
    ERROR_COUNT(false)
    {
        public void extractMetrics(BuildResult buildResult, BuildReportSeriesConfiguration config, ReportContext context)
        {
            context.addMetricValue(config.getName(), buildResult, buildResult.getErrorFeatureCount());
        }
    },
    /**
     * The number of warning features detected.
     */
    WARNING_COUNT(false)
    {
        public void extractMetrics(BuildResult buildResult, BuildReportSeriesConfiguration config, ReportContext context)
        {
            context.addMetricValue(config.getName(), buildResult, buildResult.getWarningFeatureCount());
        }
    },
    /**
     * Returns 1 for broken builds, 0 for successful ones (useful to accumulate over a day).
     */
    BROKEN_COUNT(false)
    {
        public void extractMetrics(BuildResult buildResult, BuildReportSeriesConfiguration config, ReportContext context)
        {
            context.addMetricValue(config.getName(), buildResult, buildResult.getState().isBroken() ? 1 : 0);
        }
    },
    /**
     * Returns 1 for successful builds, 0 for broken ones (useful to accumulate over a day).
     */
    SUCCESS_COUNT(false)
    {
        public void extractMetrics(BuildResult buildResult, BuildReportSeriesConfiguration config, ReportContext context)
        {
            context.addMetricValue(config.getName(), buildResult, buildResult.getState().isBroken() ? 0 : 1);
        }
    },
    /**
     * The value of a custom field whose name is specified in the {@link BuildReportSeriesConfiguration}.
     */
    CUSTOM_FIELD(false)
            {
                public void extractMetrics(BuildResult buildResult, BuildReportSeriesConfiguration config, ReportContext context)
                {
                    String fieldName = config.getField();
                    String fieldValue = context.getFieldValue(buildResult, fieldName);
                    if (fieldValue != null)
                    {
                        try
                        {
                            context.addMetricValue(config.getName(), buildResult, config.getFieldType().parse(fieldValue));
                        }
                        catch (NumberFormatException e)
                        {
                            LOG.warning("Unable to parse value of field '" + fieldName + "' (" + fieldValue + ") as a number for reporting");
                        }
                    }
                }
            };

    private static final Logger LOG = Logger.getLogger(BuildMetric.class);
    private boolean timeBased;

    BuildMetric(boolean timeBased)
    {
        this.timeBased = timeBased;
    }

    /**
     * Extracts values for this metric from the given build, adding them to the given context.
     *
     * @param buildResult the build to extract the values from
     * @param config the series configuration this metric is being used for
     * @param context the context to add values to
     */
    public abstract void extractMetrics(BuildResult buildResult, BuildReportSeriesConfiguration config, ReportContext context);

    public boolean isTimeBased()
    {
        return timeBased;
    }
}
