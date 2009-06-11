package com.zutubi.pulse.master.charting.render;

import org.jfree.chart.JFreeChart;

/**
 * Abstract base for classes that can render a JFreeChart chart.
 */
public interface Chart
{
    /**
     * Renders this chart using JFreeChart.
     *
     * @return a rendered JFreeChart chart
     */
    JFreeChart render();
}
