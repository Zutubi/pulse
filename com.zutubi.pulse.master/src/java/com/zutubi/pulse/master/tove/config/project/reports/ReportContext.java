package com.zutubi.pulse.master.tove.config.project.reports;

import com.zutubi.pulse.master.model.BuildResult;

/**
 * Context used to accumulate the values for a build or stage metric.
 */
public interface ReportContext extends CustomFieldSource
{
    /**
     * Adds a new metric to the context.
     *
     * @param name name of the metric
     * @param build build the value was extracted from
     * @param value the value of the metric in the build
     */
    void addMetricValue(String name, BuildResult build, Number value);
}
