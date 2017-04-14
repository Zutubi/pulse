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

package com.zutubi.pulse.master.tove.config.project.reports;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Maps;
import com.zutubi.pulse.core.model.Result;
import com.zutubi.pulse.master.charting.model.DataPoint;
import com.zutubi.pulse.master.charting.model.SeriesData;
import com.zutubi.pulse.master.model.BuildResult;
import com.zutubi.util.adt.Pair;
import com.zutubi.util.math.AggregationFunction;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Implementation of {@link ReportContext} which handles both build and stage metrics and
 * separating/aggregating stage values.  When using for stage metrics, the stage name must be set
 * before the metric is evaluated for that stage (see {@link #setStageName(String)}.
 */
public class DefaultReportContext implements ReportContext
{
    private String              customColour;
    private AggregationFunction aggregationFunction;
    private CustomFieldSource   fieldSource;
    private Map<String, SeriesData> seriesDataByField = Maps.newLinkedHashMap();

    private BuildResult currentBuild;
    private ListMultimap<String, Number> currentBuildValues = ArrayListMultimap.create();

    private String stageName = null;

    /**
     * Creates a new context to collect values for the given series configuration.
     *
     * @param config configuration of the series to gather metrics for
     * @param fieldSource source of custom field values, used as a delegate
     */
    public DefaultReportContext(ReportSeriesConfiguration config, CustomFieldSource fieldSource)
    {
        this.customColour = config.isUseCustomColour() ? config.getCustomColour() : null;
        if (config instanceof StageReportSeriesConfiguration)
        {
            StageReportSeriesConfiguration stageConfig = (StageReportSeriesConfiguration) config;
            if (stageConfig.isCombineStages())
            {
                aggregationFunction = stageConfig.getAggregationFunction();
            }
        }

        this.fieldSource = fieldSource;
    }

    public String getFieldValue(Result result, String name)
    {
        return fieldSource.getFieldValue(result, name);
    }

    public List<Pair<String, String>> getAllFieldValues(Result result, Pattern namePattern)
    {
        return fieldSource.getAllFieldValues(result, namePattern);
    }

    public void addMetricValue(String name, BuildResult build, Number value)
    {
        if (aggregationFunction == null)
        {
            String series = name;
            if (stageName != null)
            {
                series += " (" + stageName + ")";
            }

            addDataPoint(series, build.getNumber(), value);
        }
        else
        {
            if (build != currentBuild)
            {
                aggregateCurrentValues();
                currentBuild = build;
            }

            currentBuildValues.put(name, value);
        }
    }

    private void aggregateCurrentValues()
    {
        if (currentBuild != null)
        {
            Map<String, Collection<Number>> valueMap = currentBuildValues.asMap();
            for (String seriesName: valueMap.keySet())
            {
                List<Number> values = currentBuildValues.get(seriesName);
                if (!values.isEmpty())
                {
                    addDataPoint(seriesName, currentBuild.getNumber(), aggregationFunction.aggregate(values));
                }
            }

            // This cannot be done in the above loop as it modifies the collection.  We just empty the collection as it
            // is likely to be reused later as future builds will have similar series'.
            for (Collection<Number> values: valueMap.values())
            {
                values.clear();
            }

            currentBuild = null;
        }
    }

    private void addDataPoint(String seriesName, long buildNumber, Number value)
    {
        SeriesData seriesData = seriesDataByField.get(seriesName);
        if (seriesData == null)
        {
            seriesData = new SeriesData(seriesName, customColour);
            seriesDataByField.put(seriesName, seriesData);
        }

        seriesData.addPoint(new DataPoint(buildNumber, value));
    }

    /**
     * Sets the current stage name.  This must be called before evaluating a metric for a stage.
     *
     * @param stageName the name of the stage
     */
    public void setStageName(String stageName)
    {
        this.stageName = stageName;
    }

    /**
     * Gets all metric data added to this context as resolved series', ready for entering into a
     * report.
     *
     * @return all metric data collected by this context
     */
    public Collection<SeriesData> getAllSeriesData()
    {
        // Catch any values from the last build.
        aggregateCurrentValues();
        return seriesDataByField.values();
    }
}
