package com.zutubi.pulse.master.tove.config.project.reports;

import com.zutubi.pulse.master.model.BuildResult;
import com.zutubi.util.Constants;
import com.zutubi.util.UnaryFunction;

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
    ELAPSED_TIME
    {
        public UnaryFunction<BuildResult, Number> getExtractionFunction(BuildReportSeriesConfiguration config)
        {
            return new UnaryFunction<BuildResult, Number>()
            {
                public Number process(BuildResult buildResult)
                {
                    return (double)buildResult.getStamps().getElapsed() / Constants.SECOND;
                }
            };
        }
    },
    /**
     * The total number of tests in the build.
     */
    TEST_TOTAL_COUNT
    {
        public UnaryFunction<BuildResult, Number> getExtractionFunction(BuildReportSeriesConfiguration config)
        {
            return new UnaryFunction<BuildResult, Number>()
            {
                public Number process(BuildResult buildResult)
                {
                    return buildResult.getTestSummary().getTotal();
                }
            };
        }
    },
    /**
     * The total number of passed tests in the build.
     */
    TEST_PASS_COUNT
    {
        public UnaryFunction<BuildResult, Number> getExtractionFunction(BuildReportSeriesConfiguration config)
        {
            return new UnaryFunction<BuildResult, Number>()
            {
                public Number process(BuildResult buildResult)
                {
                    return buildResult.getTestSummary().getPassed();
                }
            };
        }
    },
    /**
     * The total number of failed tests in the build.
     */
    TEST_FAIL_COUNT
    {
        public UnaryFunction<BuildResult, Number> getExtractionFunction(BuildReportSeriesConfiguration config)
        {
            return new UnaryFunction<BuildResult, Number>()
            {
                public Number process(BuildResult buildResult)
                {
                    return buildResult.getTestSummary().getFailures();
                }
            };
        }
    },
    /**
     * The total number of errored tests in the build.
     */
    TEST_ERROR_COUNT
    {
        public UnaryFunction<BuildResult, Number> getExtractionFunction(BuildReportSeriesConfiguration config)
        {
            return new UnaryFunction<BuildResult, Number>()
            {
                public Number process(BuildResult buildResult)
                {
                    return buildResult.getTestSummary().getErrors();
                }
            };
        }
    },
    /**
     * The total number of skipped tests in the build.
     */
    TEST_SKIPPED_COUNT
    {
        public UnaryFunction<BuildResult, Number> getExtractionFunction(BuildReportSeriesConfiguration config)
        {
            return new UnaryFunction<BuildResult, Number>()
            {
                public Number process(BuildResult buildResult)
                {
                    return buildResult.getTestSummary().getSkipped();
                }
            };
        }
    },
    /**
     * The total number of broken (failed or errored) tests in the build.
     */
    TEST_BROKEN_COUNT
    {
        public UnaryFunction<BuildResult, Number> getExtractionFunction(BuildReportSeriesConfiguration config)
        {
            return new UnaryFunction<BuildResult, Number>()
            {
                public Number process(BuildResult buildResult)
                {
                    return buildResult.getTestSummary().getBroken();
                }
            };
        }
    },
    /**
     * The percentage of tests that succeeded.
     */
    TEST_SUCCESS_PERCENTAGE
    {
        public UnaryFunction<BuildResult, Number> getExtractionFunction(BuildReportSeriesConfiguration config)
        {
            return new UnaryFunction<BuildResult, Number>()
            {
                public Number process(BuildResult buildResult)
                {
                    return buildResult.getTestSummary().getSuccessPercent();
                }
            };
        }
    },
    /**
     * The percentage of tests that were broken.
     */
    TEST_BROKEN_PERCENTAGE
    {
        public UnaryFunction<BuildResult, Number> getExtractionFunction(BuildReportSeriesConfiguration config)
        {
            return new UnaryFunction<BuildResult, Number>()
            {
                public Number process(BuildResult buildResult)
                {
                    return 100 - buildResult.getTestSummary().getSuccessPercent();
                }
            };
        }
    },
    /**
     * The number of error features detected.
     */
    ERROR_COUNT
    {
        public UnaryFunction<BuildResult, Number> getExtractionFunction(BuildReportSeriesConfiguration config)
        {
            return new UnaryFunction<BuildResult, Number>()
            {
                public Number process(BuildResult buildResult)
                {
                    return buildResult.getErrorFeatureCount();
                }
            };
        }
    },
    /**
     * The number of warning features detected.
     */
    WARNING_COUNT
    {
        public UnaryFunction<BuildResult, Number> getExtractionFunction(BuildReportSeriesConfiguration config)
        {
            return new UnaryFunction<BuildResult, Number>()
            {
                public Number process(BuildResult buildResult)
                {
                    return buildResult.getWarningFeatureCount();
                }
            };
        }
    },
    /**
     * Returns 1 for broken builds, 0 for successful ones (useful to accumulate over a day).
     */
    BROKEN_COUNT
    {
        public UnaryFunction<BuildResult, Number> getExtractionFunction(BuildReportSeriesConfiguration config)
        {
            return new UnaryFunction<BuildResult, Number>()
            {
                public Number process(BuildResult buildResult)
                {
                    return buildResult.getState().isBroken() ? 1 : 0;
                }
            };
        }
    },
    /**
     * Returns 1 for successful builds, 0 for broken ones (useful to accumulate over a day).
     */
    SUCCESS_COUNT
    {
        public UnaryFunction<BuildResult, Number> getExtractionFunction(BuildReportSeriesConfiguration config)
        {
            return new UnaryFunction<BuildResult, Number>()
            {
                public Number process(BuildResult buildResult)
                {
                    return buildResult.getState().isBroken() ? 0 : 1;
                }
            };
        }
    };

    /**
     * Returns a function that can extract a value for this metric from a build
     * result.  The returned function may return null if passed a result that
     * does not have a value for this metric.
     *
     * @param config the series configuration this metric is being used for
     * @return a function that can extract the metric value from build results
     */
    public abstract UnaryFunction<BuildResult, Number> getExtractionFunction(BuildReportSeriesConfiguration config);
}
