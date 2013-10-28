package com.zutubi.pulse.master.charting.render;

import com.zutubi.pulse.master.model.BuildResult;
import com.zutubi.pulse.master.model.Project;
import com.zutubi.pulse.master.model.persistence.BuildResultDao;
import com.zutubi.pulse.master.tove.config.project.reports.CustomFieldSource;
import com.zutubi.pulse.master.tove.config.project.reports.DomainUnit;
import com.zutubi.pulse.master.tove.config.project.reports.ReportConfiguration;
import com.zutubi.pulse.master.tove.config.project.reports.ReportTimeUnit;
import com.zutubi.util.RandomUtils;
import org.jfree.chart.ChartRenderingInfo;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.servlet.ServletUtilities;

import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Utility methods for rendering charts with JFreeChart.
 */
public class ChartUtils
{
    /**
     * Renders a report using JFreeChart, ready for display via a servlet-
     * rendered web page.
     *
     * @param config            configuration describing the report to render
     * @param builds            builds to report over
     * @param customFieldSource source of custom field values if needed for the
     *                          report
     * @return a map containing the location, width, height, imageMap and
     *         imageMapName
     * @throws IOException if there is an error writing the report image file
     */
    public static Map renderForWeb(ReportConfiguration config, List<BuildResult> builds, CustomFieldSource customFieldSource) throws IOException
    {
        Chart chart;
        if (config.getDomainUnits() == DomainUnit.BUILD_IDS)
        {
            chart = new ByBuildChart(config, builds, customFieldSource);
        }
        else
        {
            chart = new ByDayChart(config, builds, customFieldSource);
        }

        return renderForWeb(chart.render(), config.getWidth(), config.getHeight());
    }

    /**
     * Renders a JFreeChart chart, ready for display via a servlet-rendered web
     * page.  The chart is rendered into a PNG image of the given size.
     *
     * @param chart  the chart to render
     * @param width  width of the image to create, in pixels
     * @param height height of the image to create, in pixels
     * @return a map containing the location, width, height, imageMap and
     *         imageMapName
     * @throws IOException if there is an error writing the image file
     */
    public static Map renderForWeb(JFreeChart chart, int width, int height) throws IOException
    {
        Map<String, Object> params = new HashMap<String, Object>();
        ChartRenderingInfo chartRenderingInfo = new ChartRenderingInfo();

        String location = ServletUtilities.saveChartAsPNG(chart, width, height, chartRenderingInfo, null);
        params.put("location", location);
        params.put("width", width);
        params.put("height", height);

        String mapName = "imageMap-" + RandomUtils.insecureRandomString(3);
        params.put("imageMap", ChartUtilities.getImageMap(mapName, chartRenderingInfo));
        params.put("imageMapName", mapName);
        return params;
    }

    /**
     * Retrieves the builds of a given project covered by a given time frame.
     *
     * @param project        project to retrieve the builds of
     * @param timeframe      size of the time frame, must be positive
     * @param timeunit       unit of the time frame
     * @param buildResultDao DAO for querying for builds
     * @return the builds matching the given criteria
     */
    public static List<BuildResult> getBuilds(Project project, int timeframe, ReportTimeUnit timeunit, BuildResultDao buildResultDao)
    {
        if (ReportTimeUnit.BUILDS == timeunit)
        {
            return buildResultDao.findLatestCompleted(project, 0, timeframe);
        }
        else
        {
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DAY_OF_YEAR, -timeframe);
            cal.set(Calendar.HOUR, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);

            return buildResultDao.findSinceByProject(project, cal.getTime());
        }
    }
}
