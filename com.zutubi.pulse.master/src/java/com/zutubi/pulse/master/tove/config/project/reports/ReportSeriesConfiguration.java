package com.zutubi.pulse.master.tove.config.project.reports;

import com.zutubi.tove.annotations.ControllingCheckbox;
import com.zutubi.tove.annotations.SymbolicName;
import com.zutubi.tove.annotations.Table;
import com.zutubi.tove.config.api.AbstractNamedConfiguration;
import com.zutubi.validation.annotations.Constraint;

/**
 * Specifies a single set of data to show as part of a report.
 */
@SymbolicName("zutubi.reportSeriesConfig")
@Table(columns = {"name"})
public abstract class ReportSeriesConfiguration extends AbstractNamedConfiguration
{
    private String field;
    private MetricType fieldType;
    private boolean successfulOnly;
    @ControllingCheckbox(checkedFields = {"customColour"})
    private boolean useCustomColour;
    @Constraint("ColourValidator")
    private String customColour;

    protected ReportSeriesConfiguration()
    {
    }

    protected ReportSeriesConfiguration(String name, boolean successfulOnly)
    {
        super(name);
        this.successfulOnly = successfulOnly;
    }

    protected ReportSeriesConfiguration(String name, boolean successfulOnly, String customColour)
    {
        super(name);
        this.successfulOnly = successfulOnly;
        this.useCustomColour = true;
        this.customColour = customColour;
    }

    /**
     * Indicates if this series measures a time-based metric.
     * 
     * @return true if this series is time-based
     */
    public abstract boolean timeBased();
    
    public String getField()
    {
        return field;
    }

    public void setField(String field)
    {
        this.field = field;
    }

    public MetricType getFieldType()
    {
        return fieldType;
    }

    public void setFieldType(MetricType fieldType)
    {
        this.fieldType = fieldType;
    }

    public boolean isSuccessfulOnly()
    {
        return successfulOnly;
    }

    public void setSuccessfulOnly(boolean successfulOnly)
    {
        this.successfulOnly = successfulOnly;
    }

    public boolean isUseCustomColour()
    {
        return useCustomColour;
    }

    public void setUseCustomColour(boolean useCustomColour)
    {
        this.useCustomColour = useCustomColour;
    }

    public String getCustomColour()
    {
        return customColour;
    }

    public void setCustomColour(String customColour)
    {
        this.customColour = customColour;
    }
}
