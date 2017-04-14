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

import com.zutubi.tove.annotations.ControllingSelect;
import com.zutubi.tove.annotations.Form;
import com.zutubi.tove.annotations.SymbolicName;

/**
 * A report series that takes data from builds.
 */
@SymbolicName("zutubi.buildReportSeriesConfig")
@Form(fieldOrder = {"name", "metric", "field", "fieldType", "successfulOnly", "useCustomColour", "customColour"})
public class BuildReportSeriesConfiguration extends ReportSeriesConfiguration
{
    @ControllingSelect(dependentFields = {"field", "fieldType"}, enableSet = {"CUSTOM_FIELD"})
    private BuildMetric metric;

    public BuildReportSeriesConfiguration()
    {
    }

    @Override
    public boolean timeBased()
    {
        return metric.isTimeBased();
    }

    public BuildReportSeriesConfiguration(String name, BuildMetric metric, boolean successfulOnly)
    {
        super(name, successfulOnly);
        this.metric = metric;
    }

    public BuildReportSeriesConfiguration(String name, BuildMetric metric, boolean successfulOnly, String customColour)
    {
        super(name, successfulOnly, customColour);
        this.metric = metric;
    }

    public BuildMetric getMetric()
    {
        return metric;
    }

    public void setMetric(BuildMetric metric)
    {
        this.metric = metric;
    }
}