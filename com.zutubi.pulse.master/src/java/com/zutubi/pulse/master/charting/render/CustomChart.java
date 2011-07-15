package com.zutubi.pulse.master.charting.render;

import com.zutubi.pulse.master.charting.build.ReportBuilder;
import com.zutubi.pulse.master.charting.model.ReportData;
import com.zutubi.pulse.master.model.BuildResult;
import com.zutubi.pulse.master.model.RecipeResultNode;
import com.zutubi.pulse.master.tove.config.project.reports.*;
import com.zutubi.util.CollectionUtils;
import com.zutubi.util.Mapping;
import com.zutubi.util.Pair;
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
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

        XYPlot plot = new XYPlot(xyDataset, createDomainAxis(), rangeAxis, configureRenderer(xyDataset));

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

    private XYItemRenderer configureRenderer(XYDataset dataset)
    {
        XYItemRenderer renderer = createRenderer();
        for (ReportSeriesConfiguration seriesConfig: configuration.getSeriesMap().values())
        {
            if (seriesConfig.isUseCustomColour())
            {
                try
                {
                    Color colour = ColourValidator.parseColour(seriesConfig.getCustomColour());
                    for (String name: getAllSeriesNames(seriesConfig))
                    {
                        renderer.setSeriesPaint(getSeriesIndex(dataset, name), colour);
                    }
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

    private List<String> getAllSeriesNames(ReportSeriesConfiguration seriesConfig)
    {
        if (seriesConfig instanceof StageReportSeriesConfiguration)
        {
            final StageReportSeriesConfiguration stageConfig = (StageReportSeriesConfiguration) seriesConfig;
            if (!stageConfig.isCombineStages())
            {
                return CollectionUtils.map(getAllStageNames(builds), new Mapping<String, String>()
                {
                    public String map(String s)
                    {
                        return ReportBuilder.getStageSeriesName(stageConfig.getName(), s);
                    }
                });
            }
        }

        return Arrays.asList(seriesConfig.getName());
    }

    private Set<String> getAllStageNames(List<BuildResult> builds)
    {
        Set<String> stages = new HashSet<String>();
        for (BuildResult buildResult: builds)
        {
            for (RecipeResultNode node: buildResult.getStages())
            {
                stages.add(node.getStageName());
            }
        }

        return stages;
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
