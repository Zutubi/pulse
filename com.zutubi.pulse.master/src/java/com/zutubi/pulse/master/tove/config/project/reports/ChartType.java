package com.zutubi.pulse.master.tove.config.project.reports;

/**
 * Types of charts that Pulse can render.
 */
public enum ChartType
{
    /**
     * A histogram (vertical bar chart).  Multiple series are shown as bars
     * placed alongside each other.
     */
    BAR_CHART,
    /**
     * A simple line chart.
     */
    LINE_CHART,
    /**
     * A histogram where multiple series are shown as bars stacked on top of
     * each other.
     */
    STACKED_BAR_CHART,
    /**
     * A chart where values are joined by a line and the area underneath is
     * filled.  Multiple series are shown as areas stacked on top of each
     * other.
     */
    STACKED_AREA_CHART
}
