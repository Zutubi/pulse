package com.zutubi.pulse.master.tove.config.project.reports;

import com.zutubi.tove.annotations.Form;
import com.zutubi.tove.annotations.SymbolicName;

/**
 * A report series that takes data from builds.
 */
@SymbolicName("zutubi.buildReportSeriesConfig")
@Form(fieldOrder = {"name", "metric", "successfulOnly", "useCustomColour", "customColour"})
public class BuildReportSeriesConfiguration extends ReportSeriesConfiguration
{
    private BuildMetric metric;

    public BuildReportSeriesConfiguration()
    {
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