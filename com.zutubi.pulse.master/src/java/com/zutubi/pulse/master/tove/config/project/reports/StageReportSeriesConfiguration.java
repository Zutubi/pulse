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

import com.zutubi.tove.annotations.ControllingCheckbox;
import com.zutubi.tove.annotations.ControllingSelect;
import com.zutubi.tove.annotations.Form;
import com.zutubi.tove.annotations.SymbolicName;
import com.zutubi.util.math.AggregationFunction;

/**
 * A report series that takes data from build stages.
 */
@SymbolicName("zutubi.stageReportSeriesConfig")
@Form(fieldOrder = {"name", "metric", "field", "fieldType", "combineStages", "aggregationFunction", "successfulOnly", "useCustomColour", "customColour"})
public class StageReportSeriesConfiguration extends ReportSeriesConfiguration
{
    @ControllingSelect(dependentFields = {"field", "fieldType"}, enableSet = {"CUSTOM_FIELD"})
    private StageMetric metric;
    @ControllingCheckbox(checkedFields = {"aggregationFunction"})
    private boolean combineStages;
    private AggregationFunction aggregationFunction;

    public StageReportSeriesConfiguration()
    {
    }

    @Override
    public boolean timeBased()
    {
        return metric.isTimeBased();
    }

    public StageReportSeriesConfiguration(String name, StageMetric metric, boolean successfulOnly)
    {
        super(name, successfulOnly);
        this.metric = metric;
    }

    public StageReportSeriesConfiguration(String name, StageMetric metric, boolean successfulOnly, String customColour)
    {
        super(name, successfulOnly, customColour);
        this.metric = metric;
    }

    public StageReportSeriesConfiguration(String name, StageMetric metric, boolean successfulOnly, AggregationFunction aggregationFunction)
    {
        super(name, successfulOnly);
        this.metric = metric;
        this.combineStages = true;
        this.aggregationFunction = aggregationFunction;
    }

    public StageReportSeriesConfiguration(String name, StageMetric metric, boolean successfulOnly, String customColour, AggregationFunction aggregationFunction)
    {
        super(name, successfulOnly, customColour);
        this.metric = metric;
        this.combineStages = true;
        this.aggregationFunction = aggregationFunction;
    }

    public StageMetric getMetric()
    {
        return metric;
    }

    public void setMetric(StageMetric metric)
    {
        this.metric = metric;
    }

    public boolean isCombineStages()
    {
        return combineStages;
    }

    public void setCombineStages(boolean combineStages)
    {
        this.combineStages = combineStages;
    }

    public AggregationFunction getAggregationFunction()
    {
        return aggregationFunction;
    }

    public void setAggregationFunction(AggregationFunction aggregationFunction)
    {
        this.aggregationFunction = aggregationFunction;
    }
}
