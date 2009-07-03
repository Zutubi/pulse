package com.zutubi.pulse.master.tove.config.project.reports;

import com.zutubi.pulse.master.model.BuildResult;
import com.zutubi.util.BinaryFunction;
import com.zutubi.util.Constants;
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
    ELAPSED_TIME
    {
        public BinaryFunction<BuildResult, CustomFieldSource, Number> getExtractionFunction(BuildReportSeriesConfiguration config)
        {
            return new BinaryFunction<BuildResult, CustomFieldSource, Number>()
            {
                public Number process(BuildResult buildResult, CustomFieldSource recipeFields)
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
        public BinaryFunction<BuildResult, CustomFieldSource, Number> getExtractionFunction(BuildReportSeriesConfiguration config)
        {
            return new BinaryFunction<BuildResult, CustomFieldSource, Number>()
            {
                public Number process(BuildResult buildResult, CustomFieldSource recipeFields)
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
        public BinaryFunction<BuildResult, CustomFieldSource, Number> getExtractionFunction(BuildReportSeriesConfiguration config)
        {
            return new BinaryFunction<BuildResult, CustomFieldSource, Number>()
            {
                public Number process(BuildResult buildResult, CustomFieldSource recipeFields)
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
        public BinaryFunction<BuildResult, CustomFieldSource, Number> getExtractionFunction(BuildReportSeriesConfiguration config)
        {
            return new BinaryFunction<BuildResult, CustomFieldSource, Number>()
            {
                public Number process(BuildResult buildResult, CustomFieldSource recipeFields)
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
        public BinaryFunction<BuildResult, CustomFieldSource, Number> getExtractionFunction(BuildReportSeriesConfiguration config)
        {
            return new BinaryFunction<BuildResult, CustomFieldSource, Number>()
            {
                public Number process(BuildResult buildResult, CustomFieldSource recipeFields)
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
        public BinaryFunction<BuildResult, CustomFieldSource, Number> getExtractionFunction(BuildReportSeriesConfiguration config)
        {
            return new BinaryFunction<BuildResult, CustomFieldSource, Number>()
            {
                public Number process(BuildResult buildResult, CustomFieldSource recipeFields)
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
        public BinaryFunction<BuildResult, CustomFieldSource, Number> getExtractionFunction(BuildReportSeriesConfiguration config)
        {
            return new BinaryFunction<BuildResult, CustomFieldSource, Number>()
            {
                public Number process(BuildResult buildResult, CustomFieldSource recipeFields)
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
        public BinaryFunction<BuildResult, CustomFieldSource, Number> getExtractionFunction(BuildReportSeriesConfiguration config)
        {
            return new BinaryFunction<BuildResult, CustomFieldSource, Number>()
            {
                public Number process(BuildResult buildResult, CustomFieldSource recipeFields)
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
        public BinaryFunction<BuildResult, CustomFieldSource, Number> getExtractionFunction(BuildReportSeriesConfiguration config)
        {
            return new BinaryFunction<BuildResult, CustomFieldSource, Number>()
            {
                public Number process(BuildResult buildResult, CustomFieldSource recipeFields)
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
        public BinaryFunction<BuildResult, CustomFieldSource, Number> getExtractionFunction(BuildReportSeriesConfiguration config)
        {
            return new BinaryFunction<BuildResult, CustomFieldSource, Number>()
            {
                public Number process(BuildResult buildResult, CustomFieldSource recipeFields)
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
        public BinaryFunction<BuildResult, CustomFieldSource, Number> getExtractionFunction(BuildReportSeriesConfiguration config)
        {
            return new BinaryFunction<BuildResult, CustomFieldSource, Number>()
            {
                public Number process(BuildResult buildResult, CustomFieldSource recipeFields)
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
        public BinaryFunction<BuildResult, CustomFieldSource, Number> getExtractionFunction(BuildReportSeriesConfiguration config)
        {
            return new BinaryFunction<BuildResult, CustomFieldSource, Number>()
            {
                public Number process(BuildResult buildResult, CustomFieldSource recipeFields)
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
        public BinaryFunction<BuildResult, CustomFieldSource, Number> getExtractionFunction(BuildReportSeriesConfiguration config)
        {
            return new BinaryFunction<BuildResult, CustomFieldSource, Number>()
            {
                public Number process(BuildResult buildResult, CustomFieldSource recipeFields)
                {
                    return buildResult.getState().isBroken() ? 0 : 1;
                }
            };
        }
    },
    /**
     * The value of a custom field whose name is specified in the {@link BuildReportSeriesConfiguration}.
     */
    CUSTOM_FIELD
    {
        public BinaryFunction<BuildResult, CustomFieldSource, Number> getExtractionFunction(final BuildReportSeriesConfiguration config)
        {
            return new BinaryFunction<BuildResult, CustomFieldSource, Number>()
            {
                public Number process(BuildResult buildResult, CustomFieldSource recipeFields)
                {
                    String fieldName = config.getField();
                    String fieldValue = recipeFields.getFieldValue(buildResult, fieldName);
                    if (fieldValue != null)
                    {
                        try
                        {
                            return config.getFieldType().parse(fieldValue);
                        }
                        catch (NumberFormatException e)
                        {
                            LOG.warning("Unable to parse value of field '" + fieldName + "' (" + fieldValue + ") as a number for reporting");
                        }
                    }

                    return null;
                }
            };
        }
    };

    private static final Logger LOG = Logger.getLogger(BuildMetric.class);
    
    /**
     * Returns a function that can extract a value for this metric from a build
     * result.  The returned function may return null if passed a result that
     * does not have a value for this metric.
     *
     * @param config the series configuration this metric is being used for
     * @return a function that can extract the metric value from build results
     */
    public abstract BinaryFunction<BuildResult, CustomFieldSource, Number> getExtractionFunction(BuildReportSeriesConfiguration config);
}
