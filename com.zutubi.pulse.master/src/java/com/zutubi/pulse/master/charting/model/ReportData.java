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

package com.zutubi.pulse.master.charting.model;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.zutubi.util.CollectionUtils;
import com.zutubi.util.adt.Pair;
import com.zutubi.util.math.AggregationFunction;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import static com.google.common.collect.Collections2.transform;
import static com.google.common.collect.Iterables.any;

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
     * Adds all series from a collection to the end of the list for this report.
     *
     * @param allSeriesData colleciton of series to add
     */
    public void addAllSeries(Collection<SeriesData> allSeriesData)
    {
        seriesList.addAll(allSeriesData);
    }

    /**
     * Indicates if this report is completely empty: i.e. there are no series
     * or none of the series have any points.
     *
     * @return true iff this report contains no data points
     */
    public boolean isEmpty()
    {
        return !any(seriesList, new Predicate<SeriesData>()
        {
            public boolean apply(SeriesData seriesData)
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
            allValues.addAll(transform(series.getPoints(), new Function<DataPoint, Number>()
            {
                public Number apply(DataPoint dataPoint)
                {
                    return dataPoint.getY();
                }
            }));
        }

        return CollectionUtils.asPair(AggregationFunction.MIN.aggregate(allValues), AggregationFunction.MAX.aggregate(allValues));
    }
}
