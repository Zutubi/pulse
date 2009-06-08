package com.zutubi.pulse.master.tove.config.project.reports;

import com.zutubi.pulse.core.model.RecipeResult;
import com.zutubi.util.BinaryFunction;
import com.zutubi.util.Constants;

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
    ELAPSED_TIME
    {
        public BinaryFunction<RecipeResult, CustomFieldSource, Number> getExtractionFunction(final StageReportSeriesConfiguration config)
        {
            return new BinaryFunction<RecipeResult, CustomFieldSource, Number>()
            {
                public Number process(RecipeResult recipeResult, CustomFieldSource recipeFields)
                {
                    return (double)recipeResult.getStamps().getElapsed() / Constants.SECOND;
                }
            };
        }
    },
    /**
     * The total number of tests run in the stage.
     */
    TEST_TOTAL_COUNT
    {
        public BinaryFunction<RecipeResult, CustomFieldSource, Number> getExtractionFunction(final StageReportSeriesConfiguration config)
        {
            return new BinaryFunction<RecipeResult, CustomFieldSource, Number>()
            {
                public Number process(RecipeResult recipeResult, CustomFieldSource recipeFields)
                {
                    return recipeResult.getTestSummary().getTotal();
                }
            };
        }
    },
    /**
     * The number of passed tests in the stage.
     */
    TEST_PASS_COUNT
    {
        public BinaryFunction<RecipeResult, CustomFieldSource, Number> getExtractionFunction(final StageReportSeriesConfiguration config)
        {
            return new BinaryFunction<RecipeResult, CustomFieldSource, Number>()
            {
                public Number process(RecipeResult recipeResult, CustomFieldSource recipeFields)
                {
                    return recipeResult.getTestSummary().getPassed();
                }
            };
        }
    },
    /**
     * The number of failed tests in the stage.
     */
    TEST_FAIL_COUNT
    {
        public BinaryFunction<RecipeResult, CustomFieldSource, Number> getExtractionFunction(final StageReportSeriesConfiguration config)
        {
            return new BinaryFunction<RecipeResult, CustomFieldSource, Number>()
            {
                public Number process(RecipeResult recipeResult, CustomFieldSource recipeFields)
                {
                    return recipeResult.getTestSummary().getFailures();
                }
            };
        }
    },
    /**
     * The number of errored tests in the stage.
     */
    TEST_ERROR_COUNT
    {
        public BinaryFunction<RecipeResult, CustomFieldSource, Number> getExtractionFunction(final StageReportSeriesConfiguration config)
        {
            return new BinaryFunction<RecipeResult, CustomFieldSource, Number>()
            {
                public Number process(RecipeResult recipeResult, CustomFieldSource recipeFields)
                {
                    return recipeResult.getTestSummary().getErrors();
                }
            };
        }
    },
    /**
     * The number of skipped tests in the stage.
     */
    TEST_SKIPPED_COUNT
    {
        public BinaryFunction<RecipeResult, CustomFieldSource, Number> getExtractionFunction(final StageReportSeriesConfiguration config)
        {
            return new BinaryFunction<RecipeResult, CustomFieldSource, Number>()
            {
                public Number process(RecipeResult recipeResult, CustomFieldSource recipeFields)
                {
                    return recipeResult.getTestSummary().getSkipped();
                }
            };
        }
    },
    /**
     * The number of broken (failed or errored) tests in the stage.
     */
    TEST_BROKEN_COUNT
    {
        public BinaryFunction<RecipeResult, CustomFieldSource, Number> getExtractionFunction(final StageReportSeriesConfiguration config)
        {
            return new BinaryFunction<RecipeResult, CustomFieldSource, Number>()
            {
                public Number process(RecipeResult recipeResult, CustomFieldSource recipeFields)
                {
                    return recipeResult.getTestSummary().getBroken();
                }
            };
        }
    },
    /**
     * The percentage of tests that succeeded.
     */
    TEST_SUCCESS_PERCENTAGE
    {
        public BinaryFunction<RecipeResult, CustomFieldSource, Number> getExtractionFunction(final StageReportSeriesConfiguration config)
        {
            return new BinaryFunction<RecipeResult, CustomFieldSource, Number>()
            {
                public Number process(RecipeResult recipeResult, CustomFieldSource recipeFields)
                {
                    return recipeResult.getTestSummary().getSuccessPercent();
                }
            };
        }
    },
    /**
     * The percentage of tests that failed.
     */
    TEST_FAILED_PERCENTAGE
    {
        public BinaryFunction<RecipeResult, CustomFieldSource, Number> getExtractionFunction(final StageReportSeriesConfiguration config)
        {
            return new BinaryFunction<RecipeResult, CustomFieldSource, Number>()
            {
                public Number process(RecipeResult recipeResult, CustomFieldSource recipeFields)
                {
                    return 100 - recipeResult.getTestSummary().getSuccessPercent();
                }
            };
        }
    },
    /**
     * The number of error features detected in the stage.
     */
    ERROR_COUNT
    {
        public BinaryFunction<RecipeResult, CustomFieldSource, Number> getExtractionFunction(final StageReportSeriesConfiguration config)
        {
            return new BinaryFunction<RecipeResult, CustomFieldSource, Number>()
            {
                public Number process(RecipeResult recipeResult, CustomFieldSource recipeFields)
                {
                    return recipeResult.getErrorFeatureCount();
                }
            };
        }
    },
    /**
     * The number of warning features detected in the stage.
     */
    WARNING_COUNT
    {
        public BinaryFunction<RecipeResult, CustomFieldSource, Number> getExtractionFunction(final StageReportSeriesConfiguration config)
        {
            return new BinaryFunction<RecipeResult, CustomFieldSource, Number>()
            {
                public Number process(RecipeResult recipeResult, CustomFieldSource recipeFields)
                {
                    return recipeResult.getWarningFeatureCount();
                }
            };
        }
    },
    /**
     * Returns 1 for broken stages, 0 for successful ones (useful to accumulate).
     */
    BROKEN_COUNT
    {
        public BinaryFunction<RecipeResult, CustomFieldSource, Number> getExtractionFunction(final StageReportSeriesConfiguration config)
        {
            return new BinaryFunction<RecipeResult, CustomFieldSource, Number>()
            {
                public Number process(RecipeResult recipeResult, CustomFieldSource recipeFields)
                {
                    return recipeResult.getState().isBroken() ? 1 : 0;
                }
            };
        }
    },
    /**
     * Returns 0 for broken stages, 1 for successful ones (useful to accumulate).
     */
    SUCCESS_COUNT
    {
        public BinaryFunction<RecipeResult, CustomFieldSource, Number> getExtractionFunction(final StageReportSeriesConfiguration config)
        {
            return new BinaryFunction<RecipeResult, CustomFieldSource, Number>()
            {
                public Number process(RecipeResult recipeResult, CustomFieldSource recipeFields)
                {
                    return recipeResult.getState().isBroken() ? 0 : 1;
                }
            };
        }
    },
    /**
     * The value of a custom field whose name is specified in the {@link StageReportSeriesConfiguration}.
     */
    CUSTOM_FIELD
    {
        public BinaryFunction<RecipeResult, CustomFieldSource, Number> getExtractionFunction(final StageReportSeriesConfiguration config)
        {
            return new BinaryFunction<RecipeResult, CustomFieldSource, Number>()
            {
                public Number process(RecipeResult recipeResult, CustomFieldSource recipeFields)
                {
                    String fieldValue = recipeFields.getFieldValue(recipeResult, config.getField());
                    if (fieldValue != null)
                    {
                        try
                        {
                            return config.getFieldType().parse(fieldValue);
                        }
                        catch (NumberFormatException e)
                        {
                            // Fall through to return null.
                        }
                    }

                    return null;
                }
            };
        }
    };

    /**
     * Returns a function that can extract a value for this metric from a
     * recipe result.  The returned function may return null if passed a result
     * that does not have a value for this metric.
     *
     * @param config the series configuration this metric is being used for
     * @return a function that can extract the metric value from recipe results
     */
    public abstract BinaryFunction<RecipeResult, CustomFieldSource, Number> getExtractionFunction(StageReportSeriesConfiguration config);
}
