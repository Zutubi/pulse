package com.zutubi.pulse.master.charting.model;

import com.zutubi.util.CollectionUtils;
import com.zutubi.util.Mapping;
import com.zutubi.util.Predicate;
import com.zutubi.util.adt.Pair;
import com.zutubi.util.math.AggregationFunction;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Holds the data for a full report: a collection of multiple series.
 */
public class ReportData
{
    private List<SeriesData> seriesList = new LinkedList<SeriesData>();

    /**
     * @return an unmodifiable list of all the series in this report
     */
    public List<SeriesData> getSeriesList()
    {
        return Collections.unmodifiableList(seriesList);
    }

    /**
     * Adds a series to the end of the list for this report.
     *
     * @param series the series to add
     */
    public void addSeries(SeriesData series)
    {
        seriesList.add(series);
    }

    /**
     * Indicates if this report is completely empty: i.e. there are no series
     * or none of the series have any points.
     *
     * @return true iff this report contains no data points
     */
    public boolean isEmpty()
    {
        return !CollectionUtils.contains(seriesList, new Predicate<SeriesData>()
        {
            public boolean satisfied(SeriesData seriesData)
            {
                return seriesData.getPoints().size() > 0;
            }
        });
    }

    /**
     * Returns the minimum and maximum range values across all series in this
     * report.  All points in all series are tested to find the smallest and
     * largest y (range) values.
     * <p/>
     * Must not be called on an empty report.
     *
     * @return a pair (min, max) of values giving the range limits for this
     *         report
     * @see #isEmpty() 
     */
    public Pair<Number, Number> getRangeLimits()
    {
        if (isEmpty())
        {
            throw new IllegalStateException("Can't get limits for empty report");
        }

        List<Number> allValues = new LinkedList<Number>();
        for (SeriesData series: seriesList)
        {
            CollectionUtils.map(series.getPoints(), new Mapping<DataPoint, Number>()
            {
                public Number map(DataPoint dataPoint)
                {
                    return dataPoint.getY();
                }
            }, allValues);
        }

        return CollectionUtils.asPair(AggregationFunction.MIN.aggregate(allValues), AggregationFunction.MAX.aggregate(allValues));
    }
}
