/* Copyright 2017 Zutubi Pty Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zutubi.pulse.master.charting.build;

import com.google.common.base.Function;
import com.zutubi.pulse.core.model.RecipeResult;
import com.zutubi.pulse.core.model.Result;
import com.zutubi.pulse.core.test.api.PulseTestCase;
import com.zutubi.pulse.master.charting.model.DataPoint;
import com.zutubi.pulse.master.charting.model.ReportData;
import com.zutubi.pulse.master.charting.model.SeriesData;
import com.zutubi.pulse.master.model.BuildResult;
import com.zutubi.pulse.master.model.RecipeResultNode;
import com.zutubi.pulse.master.model.UnknownBuildReason;
import com.zutubi.pulse.master.tove.config.project.reports.*;
import com.zutubi.util.adt.Pair;
import com.zutubi.util.math.AggregationFunction;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

import static com.google.common.collect.Lists.transform;
import static com.zutubi.util.CollectionUtils.asPair;
import static java.util.Arrays.asList;

public class ReportBuilderTest extends PulseTestCase
{
    private static final String STAGE_LINUX = "linux";
    private static final String STAGE_WINDOWS = "windows";
    private static final String STAGE_MAC = "mac";
    private static final String SERIES_PREFIX = "prefix";
    private static final List<String> SERIES_SUFFIXES = asList("1", "2");

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
            public String getFieldValue(Result result, String name)
            {
                return "3";
            }

            public List<Pair<String, String>> getAllFieldValues(Result result, Pattern namePattern)
            {
                if (namePattern.pattern().contains("*"))
                {
                    // When there is a wildcard we return a set of pairs:
                    //   (SERIES_PREFIX + suffix, suffix)
                    // for each suffix in SERIES_SUFFIXES -- note their use as values means they
                    // should be integral numbers.
                    return transform(SERIES_SUFFIXES, new Function<String, Pair<String, String>>()
                    {
                        public Pair<String, String> apply(String suffix)
                        {
                            return asPair(SERIES_PREFIX + suffix, suffix);
                        }
                    });
                }
                else
                {
                    return asList(asPair(namePattern.pattern(), "3"));
                }
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

            result.addStage(createRecipe(STAGE_LINUX, time));
            result.addStage(createRecipe(STAGE_WINDOWS, time));
            result.addStage(createRecipe(STAGE_MAC, time));

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
        reportConfig.addSeries(createCustomSeriesConfig(SERIES_PREFIX));
        reportConfig.setDomainUnits(DomainUnit.BUILD_IDS);

        ReportBuilder builder = new ReportBuilder(reportConfig, fieldSource);
        ReportData reportData = builder.build(builds);

        assertEquals(3, reportData.getSeriesList().size());
        assertEquals(createCustomSeries(null, STAGE_LINUX, 3), reportData.getSeriesList().get(0));
        assertEquals(createCustomSeries(null, STAGE_WINDOWS, 3), reportData.getSeriesList().get(1));
        assertEquals(createCustomSeries(null, STAGE_MAC, 3), reportData.getSeriesList().get(2));
    }

    public void testReportCustomStageFieldWildcardSeparated()
    {
        ReportConfiguration reportConfig = new ReportConfiguration();
        reportConfig.addSeries(createCustomSeriesConfig(SERIES_PREFIX + ".*"));
        reportConfig.setDomainUnits(DomainUnit.BUILD_IDS);

        ReportBuilder builder = new ReportBuilder(reportConfig, fieldSource);
        ReportData reportData = builder.build(builds);

        List<String> stageNames = asList(STAGE_LINUX, STAGE_WINDOWS, STAGE_MAC);
        assertEquals(stageNames.size() * SERIES_SUFFIXES.size(), reportData.getSeriesList().size());
        int i = 0;
        for (String stage : stageNames)
        {
            for (String suffix : SERIES_SUFFIXES)
            {
                assertEquals(createCustomSeries(suffix, stage, Integer.parseInt(suffix)), reportData.getSeriesList().get(i++));
            }
        }
    }

    public void testReportCustomStageFieldCombined()
    {
        ReportConfiguration reportConfig = new ReportConfiguration();
        StageReportSeriesConfiguration seriesConfig = createCustomSeriesConfig(SERIES_PREFIX);
        seriesConfig.setCombineStages(true);
        seriesConfig.setAggregationFunction(AggregationFunction.SUM);
        reportConfig.addSeries(seriesConfig);
        reportConfig.setDomainUnits(DomainUnit.BUILD_IDS);

        ReportBuilder builder = new ReportBuilder(reportConfig, fieldSource);
        ReportData reportData = builder.build(builds);

        assertEquals(1, reportData.getSeriesList().size());
        assertEquals(createCustomSeries(null, null, 9), reportData.getSeriesList().get(0));
    }

    public void testReportCustomStageFieldCombineStagesAndDays()
    {
        ReportConfiguration reportConfig = new ReportConfiguration();
        StageReportSeriesConfiguration seriesConfig = createCustomSeriesConfig(SERIES_PREFIX);
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

    private StageReportSeriesConfiguration createCustomSeriesConfig(String field)
    {
        StageReportSeriesConfiguration seriesConfig = new StageReportSeriesConfiguration();
        seriesConfig.setName("anything");
        seriesConfig.setMetric(StageMetric.CUSTOM_FIELD);
        seriesConfig.setField(field);
        seriesConfig.setFieldType(MetricType.INTEGRAL);
        seriesConfig.setCombineStages(false);
        return seriesConfig;
    }

    private SeriesData createCustomSeries(String suffix, String stageName, int value)
    {
        String seriesName = SERIES_PREFIX;
        if (suffix != null)
        {
            seriesName += suffix;
        }

        if (stageName != null)
        {
            seriesName += " (" + stageName + ")";
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
