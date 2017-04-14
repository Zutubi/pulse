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

package com.zutubi.pulse.master.charting.render;

import com.zutubi.pulse.master.charting.model.DataPoint;
import com.zutubi.pulse.master.charting.model.ReportData;
import com.zutubi.pulse.master.charting.model.SeriesData;
import com.zutubi.pulse.master.model.BuildResult;
import com.zutubi.pulse.master.tove.config.project.reports.CustomFieldSource;
import com.zutubi.pulse.master.tove.config.project.reports.ReportConfiguration;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.data.time.Day;
import org.jfree.data.time.TimeTableXYDataset;

import java.util.Date;
import java.util.List;

/**
 * A chart that reports over days by aggregating builds on the same day.
 */
public class ByDayChart extends CustomChart
{
    public ByDayChart(ReportConfiguration configuration, List<BuildResult> builds, CustomFieldSource customFieldSource)
    {
        super(configuration, builds, customFieldSource);
    }

    @Override
    protected ValueAxis createDomainAxis()
    {
        return new DateAxis(getConfiguration().getDomainUnits().getLabel());
    }

    protected TimeTableXYDataset generateDataSet(ReportData reportData)
    {
        TimeTableXYDataset dataset = new TimeTableXYDataset();
        for (SeriesData seriesData: reportData.getSeriesList())
        {
            for (DataPoint point: seriesData.getPoints())
            {
                dataset.add(new Day(new Date(point.getX())), point.getY(), seriesData.getName(), false);
            }
        }

        return dataset;
    }
}
