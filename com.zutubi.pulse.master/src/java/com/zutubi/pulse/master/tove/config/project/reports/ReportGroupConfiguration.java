package com.zutubi.pulse.master.tove.config.project.reports;

import com.zutubi.tove.annotations.Form;
import com.zutubi.tove.annotations.Ordered;
import com.zutubi.tove.annotations.SymbolicName;
import com.zutubi.tove.annotations.Table;
import com.zutubi.tove.config.api.AbstractNamedConfiguration;
import com.zutubi.validation.annotations.Numeric;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A group of reports, all show together on one page.
 */
@SymbolicName("zutubi.reportGroupConfig")
@Form(fieldOrder = {"name", "defaultTimeFrame", "defaultTimeUnit"})
@Table(columns = {"name"})
public class ReportGroupConfiguration extends AbstractNamedConfiguration
{
    @Numeric(min = 1)
    private int defaultTimeFrame = 45;
    private ReportTimeUnit defaultTimeUnit = ReportTimeUnit.DAYS;
    @Ordered
    private Map<String, ReportConfiguration> reports = new LinkedHashMap<String, ReportConfiguration>();

    public ReportGroupConfiguration()
    {
    }

    public ReportGroupConfiguration(String name)
    {
        super(name);
    }

    public int getDefaultTimeFrame()
    {
        return defaultTimeFrame;
    }

    public void setDefaultTimeFrame(int defaultTimeFrame)
    {
        this.defaultTimeFrame = defaultTimeFrame;
    }

    public ReportTimeUnit getDefaultTimeUnit()
    {
        return defaultTimeUnit;
    }

    public void setDefaultTimeUnit(ReportTimeUnit defaultTimeUnit)
    {
        this.defaultTimeUnit = defaultTimeUnit;
    }

    public Map<String, ReportConfiguration> getReports()
    {
        return reports;
    }

    public void setReports(Map<String, ReportConfiguration> reports)
    {
        this.reports = reports;
    }

    public void addReport(ReportConfiguration report)
    {
        reports.put(report.getName(), report);
    }
}
