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

import com.zutubi.pulse.master.charting.build.ReportBuilder;
import com.zutubi.pulse.master.charting.model.ReportData;
import com.zutubi.pulse.master.charting.model.SeriesData;
import com.zutubi.pulse.master.model.BuildResult;
import com.zutubi.pulse.master.tove.config.project.reports.*;
import com.zutubi.util.adt.Pair;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.StandardChartTheme;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.xy.*;
import org.jfree.chart.util.RelativeDateFormat;
import org.jfree.data.xy.XYDataset;

import java.awt.*;
import java.text.DecimalFormat;
import java.util.List;

/**
 * Abstract base for charts built from {@link com.zutubi.pulse.master.tove.config.project.reports.ReportConfiguration}.
 * Handles many of the common details, delegating the rest to subclasses.
 */
public abstract class CustomChart implements Chart
{
    private ReportConfiguration configuration;
    private List<BuildResult> builds;
    private CustomFieldSource customFieldSource;

    static
    {
        ChartFactory.setChartTheme(StandardChartTheme.createLegacyTheme());
        BarRenderer.setDefaultShadowsVisible(false);
        XYBarRenderer.setDefaultShadowsVisible(false);
    }
    
    public CustomChart(ReportConfiguration configuration, List<BuildResult> builds, CustomFieldSource customFieldSource)
    {
        this.builds = builds;
        this.configuration = configuration;
        this.customFieldSource = customFieldSource;
    }

    public ReportConfiguration getConfiguration()
    {
        return configuration;
    }

    public JFreeChart render()
    {
        ReportBuilder builder = new ReportBuilder(configuration, customFieldSource);
        ReportData reportData = builder.build(builds);
        XYDataset xyDataset = generateDataSet(reportData);

        ValueAxis rangeAxis;
        if (allMetricsTimeBased(configuration))
        {
            DateAxis dateAxis = new DateAxis(configuration.getRangeLabel());
            RelativeDateFormat rdf = new RelativeDateFormat();
            rdf.setShowZeroDays(false);
            rdf.setMinuteFormatter(new DecimalFormat("00"));
            rdf.setSecondFormatter(new DecimalFormat("00"));
            dateAxis.setDateFormatOverride(rdf);
            
            rangeAxis = dateAxis;
        }
        else
        {
            NumberAxis numberAxis = new NumberAxis(configuration.getRangeLabel());

            if (configuration.getType() == ChartType.LINE_CHART && configuration.isZoomRange() && !reportData.isEmpty())
            {
                Pair<Number, Number> minMax = reportData.getRangeLimits();

                double buffer = Math.max(minMax.second.doubleValue() / 15, 5);
                numberAxis.setLowerBound(Math.max(0, minMax.first.doubleValue() - buffer));
                numberAxis.setUpperBound(minMax.second.doubleValue() + buffer);
            }
            
            rangeAxis = numberAxis;
        }

        XYPlot plot = new XYPlot(xyDataset, createDomainAxis(), rangeAxis, configureRenderer(xyDataset, reportData));

        JFreeChart chart = new JFreeChart(configuration.getName(), plot);
        chart.setBackgroundPaint(Color.WHITE);
        chart.setBorderVisible(false);

        return chart;
    }

    private boolean allMetricsTimeBased(ReportConfiguration configuration)
    {
        for (ReportSeriesConfiguration series: configuration.getSeriesMap().values())
        {
            if (!series.timeBased())
            {
                return false;
            }
        }
        
        return true;
    }

    private XYItemRenderer configureRenderer(XYDataset dataset, ReportData reportData)
    {
        XYItemRenderer renderer = createRenderer();
        for (SeriesData series : reportData.getSeriesList())
        {
            String customColour = series.getCustomColour();
            if (customColour != null)
            {
                try
                {
                    Color colour = ColourValidator.parseColour(customColour);
                    renderer.setSeriesPaint(getSeriesIndex(dataset, series.getName()), colour);
                }
                catch (IllegalArgumentException e)
                {
                    // Just take the default colour.
                }

            }
        }

        return renderer;
    }

    private int getSeriesIndex(XYDataset dataset, String name)
    {
        for (int i = 0; i < dataset.getSeriesCount(); i++)
        {
            if (dataset.getSeriesKey(i).equals(name))
            {
                return i;
            }
        }

        return -1;
    }

    private XYItemRenderer createRenderer()
    {
        switch (configuration.getType())
        {
            case BAR_CHART:
                return new ClusteredXYBarRenderer();
            case LINE_CHART:
                return new DefaultXYItemRenderer();
            case STACKED_AREA_CHART:
                return new StackedXYAreaRenderer2();
            case STACKED_BAR_CHART:
                return new StackedXYBarRenderer();
        }

        throw new IllegalArgumentException("Unrecognised chart type '" + configuration.getType() + "'");
    }

    /**
     * Creates the domain axis based on the specific report type.  Override to
     * create an appropriate axis.
     *
     * @return the domain (horizontal) axis to use for this chart
     */
    protected abstract ValueAxis createDomainAxis();

    /**
     * Generates the JFreeChart dataset used to render this report from the
     * given raw data.
     *
     * @param reportData the raw report data
     * @return a dataset in JFreeChart form
     */
    protected abstract XYDataset generateDataSet(ReportData reportData);
}
