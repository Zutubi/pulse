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
    private String field;
    private MetricType fieldType;
    @ControllingCheckbox(dependentFields = {"aggregationFunction"})
    private boolean combineStages;
    private AggregationFunction aggregationFunction;

    public StageMetric getMetric()
    {
        return metric;
    }

    public void setMetric(StageMetric metric)
    {
        this.metric = metric;
    }

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
