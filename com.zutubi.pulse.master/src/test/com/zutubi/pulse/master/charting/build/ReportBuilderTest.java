package com.zutubi.pulse.master.charting.build;

import com.zutubi.pulse.core.model.RecipeResult;
import com.zutubi.pulse.core.test.api.PulseTestCase;
import com.zutubi.pulse.master.charting.model.DataPoint;
import com.zutubi.pulse.master.charting.model.ReportData;
import com.zutubi.pulse.master.charting.model.SeriesData;
import com.zutubi.pulse.master.model.BuildResult;
import com.zutubi.pulse.master.model.RecipeResultNode;
import com.zutubi.pulse.master.model.UnknownBuildReason;
import com.zutubi.pulse.master.tove.config.project.reports.*;
import com.zutubi.util.math.AggregationFunction;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.LinkedList;
import java.util.List;

public class ReportBuilderTest extends PulseTestCase
{
    private static final String STAGE_LINUX = "linux";
    private static final String STAGE_WINDOWS = "windows";
    private static final String STAGE_MAC = "mac";
    private static final String SERIES_PREFIX = "prefix";

    private static final long DAY_1 = new GregorianCalendar(2000, 5, 10).getTimeInMillis();
    private static final long DAY_2 = new GregorianCalendar(2000, 5, 11).getTimeInMillis();
    private static final long DAY_3 = new GregorianCalendar(2000, 5, 12).getTimeInMillis();
    private static final long DAY_4 = new GregorianCalendar(2000, 5, 13).getTimeInMillis();
    private static final long DAY_5 = new GregorianCalendar(2000, 5, 14).getTimeInMillis();

    private static final String SERIES_BROKEN = "broken";
    private static final String SERIES_SUCCESS = "success";

    private CustomFieldSource fieldSource;
    private List<BuildResult> builds = new LinkedList<BuildResult>();
    private long nextRecipeId = 1000;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();

        fieldSource = new CustomFieldSource()
        {
            public String getFieldValue(RecipeResult recipeResult, String name)
            {
                return "3";
            }
        };

        // Set up 10 build results:
        //   - one   on 10/06/2000
        //   - two   on 11/06/2000
        //   - three on 12/06/2000
        //   - three on 13/06/2000
        //   - one   on 14/06/2000
        //
        // All builds have three stages:
        //   - linux
        //   - windows
        //   - mac
        //
        // Every second build (starting from the first) fails.
        Calendar calendar = new GregorianCalendar(2000, 5, 10, 3, 30);
        for (int i = 0; i < 10; i++)
        {
            switch(i)
            {
                case 1:
                case 3:
                case 6:
                case 9:
                    calendar.add(Calendar.DAY_OF_MONTH, 1);
            }

            BuildResult result = new BuildResult(new UnknownBuildReason(), null, i + 1, false);
            result.setId(i + 1);

            long time = calendar.getTimeInMillis();

            RecipeResultNode root = result.getRoot();
            root.addChild(createRecipe(STAGE_LINUX, time));
            root.addChild(createRecipe(STAGE_WINDOWS, time));
            root.addChild(createRecipe(STAGE_MAC, time));

            result.commence(time);
            if (i % 2 == 0)
            {
                result.failure("Oops");
            }

            result.complete(time);
            builds.add(result);
        }
    }

    private RecipeResultNode createRecipe(String stageName, long time)
    {
        RecipeResult recipeResult = new RecipeResult(null);
        recipeResult.setId(nextRecipeId++);
        recipeResult.commence(time);
        recipeResult.complete(time);
        return new RecipeResultNode(stageName, 0, recipeResult);
    }

    public void testReportBuildResultByBuild()
    {
        ReportConfiguration reportConfig = createBuildResultConfig();
        reportConfig.setDomainUnits(DomainUnit.BUILD_IDS);

        ReportBuilder builder = new ReportBuilder(reportConfig, null);
        ReportData reportData = builder.build(builds);

        assertEquals(2, reportData.getSeriesList().size());
        assertEquals(createSeries(SERIES_BROKEN, true, 1, 1, 2, 0, 3, 1, 4, 0, 5, 1, 6, 0, 7, 1, 8, 0, 9, 1, 10, 0), reportData.getSeriesList().get(0));
        assertEquals(createSeries(SERIES_SUCCESS, true, 1, 0, 2, 1, 3, 0, 4, 1, 5, 0, 6, 1, 7, 0, 8, 1, 9, 0, 10, 1), reportData.getSeriesList().get(1));
    }

    public void testReportBuildResultByDay()
    {
        ReportConfiguration reportConfig = createBuildResultConfig();
        reportConfig.setDomainUnits(DomainUnit.DAYS);
        reportConfig.setAggregationFunction(AggregationFunction.SUM);
        
        ReportBuilder builder = new ReportBuilder(reportConfig, null);
        ReportData reportData = builder.build(builds);

        assertEquals(2, reportData.getSeriesList().size());
        assertEquals(createSeries(SERIES_BROKEN, true, DAY_1, 1, DAY_2, 1, DAY_3, 1, DAY_4, 2, DAY_5, 0), reportData.getSeriesList().get(0));
        assertEquals(createSeries(SERIES_SUCCESS, true, DAY_1, 0, DAY_2, 1, DAY_3, 2, DAY_4, 1, DAY_5, 1), reportData.getSeriesList().get(1));
    }

    private ReportConfiguration createBuildResultConfig()
    {
        ReportConfiguration reportConfig = new ReportConfiguration();
        BuildReportSeriesConfiguration seriesConfig = new BuildReportSeriesConfiguration();
        seriesConfig.setName(SERIES_BROKEN);
        seriesConfig.setMetric(BuildMetric.BROKEN_COUNT);
        reportConfig.addSeries(seriesConfig);

        seriesConfig = new BuildReportSeriesConfiguration();
        seriesConfig.setName(SERIES_SUCCESS);
        seriesConfig.setMetric(BuildMetric.SUCCESS_COUNT);
        reportConfig.addSeries(seriesConfig);
        return reportConfig;
    }

    public void testReportCustomStageFieldSeparated()
    {
        ReportConfiguration reportConfig = new ReportConfiguration();
        reportConfig.addSeries(createCustomSeriesConfig());
        reportConfig.setDomainUnits(DomainUnit.BUILD_IDS);

        ReportBuilder builder = new ReportBuilder(reportConfig, fieldSource);
        ReportData reportData = builder.build(builds);

        assertEquals(3, reportData.getSeriesList().size());
        assertEquals(createCustomSeries(STAGE_LINUX, 3), reportData.getSeriesList().get(0));
        assertEquals(createCustomSeries(STAGE_WINDOWS, 3), reportData.getSeriesList().get(1));
        assertEquals(createCustomSeries(STAGE_MAC, 3), reportData.getSeriesList().get(2));
    }

    public void testReportCustomStageFieldCombined()
    {
        ReportConfiguration reportConfig = new ReportConfiguration();
        StageReportSeriesConfiguration seriesConfig = createCustomSeriesConfig();
        seriesConfig.setCombineStages(true);
        seriesConfig.setAggregationFunction(AggregationFunction.SUM);
        reportConfig.addSeries(seriesConfig);
        reportConfig.setDomainUnits(DomainUnit.BUILD_IDS);

        ReportBuilder builder = new ReportBuilder(reportConfig, fieldSource);
        ReportData reportData = builder.build(builds);

        assertEquals(1, reportData.getSeriesList().size());
        assertEquals(createCustomSeries(null, 9), reportData.getSeriesList().get(0));
    }

    public void testReportCustomStageFieldCombineStagesAndDays()
    {
        ReportConfiguration reportConfig = new ReportConfiguration();
        StageReportSeriesConfiguration seriesConfig = createCustomSeriesConfig();
        seriesConfig.setCombineStages(true);
        seriesConfig.setAggregationFunction(AggregationFunction.SUM);
        reportConfig.addSeries(seriesConfig);
        reportConfig.setDomainUnits(DomainUnit.DAYS);
        reportConfig.setAggregationFunction(AggregationFunction.SUM);

        ReportBuilder builder = new ReportBuilder(reportConfig, fieldSource);
        ReportData reportData = builder.build(builds);

        assertEquals(1, reportData.getSeriesList().size());
        assertEquals(createSeries(SERIES_PREFIX, false, DAY_1, 9, DAY_2, 18, DAY_3, 27, DAY_4, 27, DAY_5, 9), reportData.getSeriesList().get(0));
    }

    private StageReportSeriesConfiguration createCustomSeriesConfig()
    {
        StageReportSeriesConfiguration seriesConfig = new StageReportSeriesConfiguration();
        seriesConfig.setName(SERIES_PREFIX);
        seriesConfig.setMetric(StageMetric.CUSTOM_FIELD);
        seriesConfig.setField("anything");
        seriesConfig.setFieldType(MetricType.INTEGRAL);
        seriesConfig.setCombineStages(false);
        return seriesConfig;
    }

    private SeriesData createCustomSeries(String stageName, int value)
    {
        String seriesName;
        if (stageName == null)
        {
            seriesName = SERIES_PREFIX;
        }
        else
        {
            seriesName = ReportBuilder.getStageSeriesName(SERIES_PREFIX, stageName);
        }

        return createSeries(seriesName, false, 1, value, 2, value, 3, value, 4, value, 5, value, 6, value, 7, value, 8, value, 9, value, 10, value);
    }

    private SeriesData createSeries(String name, boolean asInts, long... points)
    {
        SeriesData data = new SeriesData(name);

        for (int i = 0; i < points.length; i += 2)
        {
            Number y = points[i + 1];
            if(asInts)
            {
                y = y.intValue();
            }
            data.addPoint(new DataPoint(points[i], y));
        }

        return data;
    }
}
