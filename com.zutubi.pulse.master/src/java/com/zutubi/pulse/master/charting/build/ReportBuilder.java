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

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.zutubi.pulse.master.charting.model.DataPoint;
import com.zutubi.pulse.master.charting.model.ReportData;
import com.zutubi.pulse.master.charting.model.SeriesData;
import com.zutubi.pulse.master.model.BuildResult;
import com.zutubi.pulse.master.model.RecipeResultNode;
import com.zutubi.pulse.master.tove.config.project.reports.*;
import com.zutubi.util.adt.Pair;
import com.zutubi.util.math.AggregationFunction;

import java.util.*;

import static com.zutubi.util.CollectionUtils.asPair;

/**
 * Builds reports by combining configuration with a set of build results.
 */
public class ReportBuilder
{
    private ReportConfiguration configuration;
    private CustomFieldSource customFieldSource;

    public ReportBuilder(ReportConfiguration configuration, CustomFieldSource customFieldSource)
    {
        this.configuration = configuration;
        this.customFieldSource = customFieldSource;
    }

    public ReportData build(List<BuildResult> dataSet)
    {
        if (configuration.getDomainUnits() == DomainUnit.BUILD_IDS)
        {
            return buildByBuildNumber(dataSet);
        }
        else
        {
            return buildByDay(dataSet, configuration.getAggregationFunction());
        }
    }

    private ReportData buildByBuildNumber(List<BuildResult> dataSet)
    {
        ReportData reportData = new ReportData();
        for (final ReportSeriesConfiguration seriesConfig: configuration.getSeriesMap().values())
        {
            Iterable<BuildResult> filteredDataSet = Iterables.filter(dataSet, new Predicate<BuildResult>()
            {
                public boolean apply(BuildResult buildResult)
                {
                    return !seriesConfig.isSuccessfulOnly() || buildResult.healthy();
                }
            });

            if (seriesConfig instanceof BuildReportSeriesConfiguration)
            {
                addBuildSeries(reportData, (BuildReportSeriesConfiguration) seriesConfig, filteredDataSet);
            }
            else
            {
                addStageSeries(reportData, (StageReportSeriesConfiguration) seriesConfig, filteredDataSet);
            }
        }

        return reportData;
    }

    private void addBuildSeries(ReportData reportData, BuildReportSeriesConfiguration seriesConfig, Iterable<BuildResult> dataSet)
    {
        DefaultReportContext context = new DefaultReportContext(seriesConfig, customFieldSource);
        for (BuildResult build: dataSet)
        {
            seriesConfig.getMetric().extractMetrics(build, seriesConfig, context);
        }

        reportData.addAllSeries(context.getAllSeriesData());
    }

    private void addStageSeries(ReportData reportData, StageReportSeriesConfiguration seriesConfig, Iterable<BuildResult> dataSet)
    {
        DefaultReportContext context = new DefaultReportContext(seriesConfig, customFieldSource);
        for (BuildResult build: dataSet)
        {
            for (RecipeResultNode node: build.getStages())
            {
                context.setStageName(node.getStageName());
                seriesConfig.getMetric().extractMetrics(build, node.getResult(), seriesConfig, context);
            }
        }

        reportData.addAllSeries(context.getAllSeriesData());
    }

    private ReportData buildByDay(List<BuildResult> dataSet, AggregationFunction fn)
    {
        ReportData byBuild = buildByBuildNumber(dataSet);
        ReportData byDay = new ReportData();

        Map<Long, Pair<Integer, Integer>> numberToYearAndDay = mapBuildsToDays(dataSet);
        for (SeriesData series: byBuild.getSeriesList())
        {
            byDay.addSeries(aggregateSeries(series, fn, numberToYearAndDay));
        }

        return byDay;
    }

    private Map<Long, Pair<Integer, Integer>> mapBuildsToDays(List<BuildResult> dataSet)
    {
        Calendar calendar = new GregorianCalendar();
        Map<Long, Pair<Integer, Integer>> numberToYearAndDay = new HashMap<Long, Pair<Integer, Integer>>();
        for (BuildResult build: dataSet)
        {
            calendar.setTime(new Date(build.getStamps().getStartTime()));
            numberToYearAndDay.put(build.getNumber(), asPair(calendar.get(Calendar.YEAR), calendar.get(Calendar.DAY_OF_YEAR)));
        }
        return numberToYearAndDay;
    }

    private SeriesData aggregateSeries(SeriesData seriesData, AggregationFunction fn, Map<Long, Pair<Integer, Integer>> numberToYearAndDay)
    {
        Map<Pair<Integer, Integer>, List<Number>> valuesByDay = getValuesByDay(seriesData, numberToYearAndDay);

        Calendar calendar = new GregorianCalendar();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        SeriesData aggregatedSeries = new SeriesData(seriesData.getName(), seriesData.getCustomColour());
        List<DataPoint> points = new LinkedList<DataPoint>();
        for (Map.Entry<Pair<Integer, Integer>, List<Number>> entry: valuesByDay.entrySet())
        {
            Pair<Integer, Integer> yearAndDay = entry.getKey();
            calendar.set(Calendar.YEAR, yearAndDay.getFirst());
            calendar.set(Calendar.DAY_OF_YEAR, yearAndDay.getSecond());

            points.add(new DataPoint(calendar.getTime().getTime(), fn.aggregate(entry.getValue())));
        }

        Collections.sort(points);
        aggregatedSeries.addPoints(points);
        return aggregatedSeries;
    }

    private Map<Pair<Integer, Integer>, List<Number>> getValuesByDay(SeriesData seriesData, Map<Long, Pair<Integer, Integer>> numberToYearAndDay)
    {
        Map<Pair<Integer, Integer>, List<Number>> valuesByDay = new HashMap<Pair<Integer, Integer>, List<Number>>();
        for (DataPoint point: seriesData.getPoints())
        {
            Pair<Integer, Integer> yearAndDay = numberToYearAndDay.get(point.getX());
            List<Number> values = valuesByDay.get(yearAndDay);
            if (values == null)
            {
                values = new LinkedList<Number>();
                valuesByDay.put(yearAndDay, values);
            }

            values.add(point.getY());
        }

        return valuesByDay;
    }

}
