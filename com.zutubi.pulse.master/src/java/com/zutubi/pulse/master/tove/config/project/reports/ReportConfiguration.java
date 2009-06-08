package com.zutubi.pulse.master.tove.config.project.reports;

import com.zutubi.tove.annotations.ControllingSelect;
import com.zutubi.tove.annotations.Form;
import com.zutubi.tove.annotations.SymbolicName;
import com.zutubi.tove.annotations.Table;
import com.zutubi.tove.config.api.AbstractNamedConfiguration;
import com.zutubi.util.math.AggregationFunction;
import com.zutubi.validation.annotations.Numeric;
import com.zutubi.validation.annotations.Required;

import java.util.HashMap;
import java.util.Map;

/**
 * Configures a single report, shown as a chart in the web interface.  A
 * single chart can include multiple series'.
 */
@SymbolicName("zutubi.reportConfig")
@Form(fieldOrder = {"name", "type", "zoomRange", "domainUnits", "aggregationFunction", "rangeLabel", "width", "height"})
@Table(columns = {"name", "type"})
public class ReportConfiguration extends AbstractNamedConfiguration
{
    @ControllingSelect(dependentFields = {"zoomRange"}, enableSet = {"LINE_CHART"})
    private ChartType type = ChartType.LINE_CHART;
    private boolean zoomRange = false;
    @ControllingSelect(dependentFields = {"aggregationFunction"}, enableSet = {"DAYS"})
    private DomainUnit domainUnits = DomainUnit.BUILD_IDS;
    private AggregationFunction aggregationFunction = AggregationFunction.MEAN;
    @Required
    private String rangeLabel;
    @Required @Numeric(min = 200)
    private int width = 400;
    @Required @Numeric(min = 200)
    private int height = 300;
    private Map<String, ReportSeriesConfiguration> seriesMap = new HashMap<String, ReportSeriesConfiguration>();

    public ChartType getType()
    {
        return type;
    }

    public void setType(ChartType type)
    {
        this.type = type;
    }

    public boolean isZoomRange()
    {
        return zoomRange;
    }

    public void setZoomRange(boolean zoomRange)
    {
        this.zoomRange = zoomRange;
    }

    public DomainUnit getDomainUnits()
    {
        return domainUnits;
    }

    public void setDomainUnits(DomainUnit domainUnits)
    {
        this.domainUnits = domainUnits;
    }

    public AggregationFunction getAggregationFunction()
    {
        return aggregationFunction;
    }

    public void setAggregationFunction(AggregationFunction aggregationFunction)
    {
        this.aggregationFunction = aggregationFunction;
    }

    public String getRangeLabel()
    {
        return rangeLabel;
    }

    public void setRangeLabel(String rangeLabel)
    {
        this.rangeLabel = rangeLabel;
    }

    public Map<String, ReportSeriesConfiguration> getSeriesMap()
    {
        return seriesMap;
    }

    public int getWidth()
    {
        return width;
    }

    public void setWidth(int width)
    {
        this.width = width;
    }

    public int getHeight()
    {
        return height;
    }

    public void setHeight(int height)
    {
        this.height = height;
    }

    public void setSeriesMap(Map<String, ReportSeriesConfiguration> seriesMap)
    {
        this.seriesMap = seriesMap;
    }

    public void addSeries(ReportSeriesConfiguration series)
    {
        seriesMap.put(series.getName(), series);
    }
}
