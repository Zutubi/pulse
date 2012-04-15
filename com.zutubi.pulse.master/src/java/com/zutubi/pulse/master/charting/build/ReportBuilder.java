package com.zutubi.pulse.master.charting.build;

import com.zutubi.pulse.core.model.RecipeResult;
import com.zutubi.pulse.master.charting.model.DataPoint;
import com.zutubi.pulse.master.charting.model.ReportData;
import com.zutubi.pulse.master.charting.model.SeriesData;
import com.zutubi.pulse.master.model.BuildResult;
import com.zutubi.pulse.master.model.RecipeResultNode;
import com.zutubi.pulse.master.tove.config.project.reports.*;
import com.zutubi.util.*;
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
            List<BuildResult> filteredDataSet = CollectionUtils.filter(dataSet, new Predicate<BuildResult>()
            {
                public boolean satisfied(BuildResult buildResult)
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

    private void addBuildSeries(ReportData reportData, BuildReportSeriesConfiguration seriesConfig, List<BuildResult> dataSet)
    {
        SeriesData seriesData = new SeriesData(seriesConfig.getName());
        BinaryFunction<BuildResult, CustomFieldSource, Number> extractFn = seriesConfig.getMetric().getExtractionFunction(seriesConfig);

        for (BuildResult build: dataSet)
        {
            Number value = extractFn.process(build, customFieldSource);
            if (value != null)
            {
                seriesData.addPoint(new DataPoint(build.getNumber(), value));
            }
        }

        reportData.addSeries(seriesData);
    }

    private void addStageSeries(ReportData reportData, StageReportSeriesConfiguration seriesConfig, List<BuildResult> dataSet)
    {
        if (seriesConfig.isCombineStages())
        {
            addCombinedStageSeries(reportData, seriesConfig, dataSet);
        }
        else
        {
            addSeparateStageSeries(reportData, seriesConfig, dataSet);
        }
    }

    private void addCombinedStageSeries(ReportData reportData, StageReportSeriesConfiguration seriesConfig, List<BuildResult> dataSet)
    {
        SeriesData seriesData = new SeriesData(seriesConfig.getName());
        final BinaryFunction<RecipeResult, CustomFieldSource, Number> extractFn = seriesConfig.getMetric().getExtractionFunction(seriesConfig);

        for (BuildResult build: dataSet)
        {
            List<Number> values = CollectionUtils.map(build.getStages(), new Mapping<RecipeResultNode, Number>()
            {
                public Number map(RecipeResultNode node)
                {
                    return extractFn.process(node.getResult(), customFieldSource);
                }
            });

            values = CollectionUtils.filter(values, new NotNullPredicate<Number>());
            if (values.size()  > 0)
            {
                seriesData.addPoint(new DataPoint(build.getNumber(), seriesConfig.getAggregationFunction().aggregate(values)));
            }
        }

        reportData.addSeries(seriesData);
    }

    private void addSeparateStageSeries(ReportData reportData, StageReportSeriesConfiguration seriesConfig, List<BuildResult> dataSet)
    {
        BinaryFunction<RecipeResult, CustomFieldSource, Number> extractFn = seriesConfig.getMetric().getExtractionFunction(seriesConfig);
        Map<String, SeriesData> seriesByStage = new LinkedHashMap<String, SeriesData>();

        for (BuildResult build: dataSet)
        {
            for (RecipeResultNode node: build.getStages())
            {
                String stageName = node.getStageName();
                SeriesData series = seriesByStage.get(stageName);
                if (series == null)
                {
                    String seriesName = seriesConfig.getName();
                    series = new SeriesData(getStageSeriesName(seriesName, stageName));
                    seriesByStage.put(stageName, series);
                }

                Number value = extractFn.process(node.getResult(), customFieldSource);
                if (value != null)
                {
                    series.addPoint(new DataPoint(build.getNumber(), value));
                }
            }
        }

        for (SeriesData series: seriesByStage.values())
        {
            reportData.addSeries(series);
        }
    }

    public static String getStageSeriesName(String seriesName, String stageName)
    {
        return seriesName + " (" + stageName + ")";
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

        SeriesData aggregatedSeries = new SeriesData(seriesData.getName());
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
