package com.zutubi.pulse.master.xwork.actions.agents;

import com.zutubi.i18n.Messages;
import com.zutubi.pulse.master.agent.Agent;
import com.zutubi.pulse.master.agent.statistics.AgentStatistics;
import com.zutubi.pulse.master.charting.render.ChartUtils;
import com.zutubi.pulse.master.tove.config.project.reports.ChartColours;
import com.zutubi.util.TimeStamps;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.block.BlockBorder;
import org.jfree.chart.labels.StandardPieSectionLabelGenerator;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.title.LegendTitle;
import org.jfree.data.general.DefaultPieDataset;

import java.awt.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Map;

/**
 * Action to view statistics for a specified agent.
 */
public class AgentStatisticsAction extends AgentActionBase
{
    private static final Messages I18N = Messages.getInstance(AgentStatisticsAction.class);

    private AgentStatistics statistics;
    private Map usageChart;

    public AgentStatistics getStatistics()
    {
        return statistics;
    }

    public String getBlurb()
    {
        DateFormat format = SimpleDateFormat.getDateInstance();
        String firstDay = format.format(statistics.getFirstDayStamp());
        return I18N.format("statistics.blurb", firstDay);
    }

    public Map getUsageChart()
    {
        return usageChart;
    }

    public String renderValue(double value)
    {
        return String.format("%.2f", value);
    }

    public String renderTime(double time)
    {
        return renderTime((long) time);
    }
    
    public String renderTime(long time)
    {
        return TimeStamps.getPrettyElapsed(time, 2);
    }

    public String renderTime(long total, double percent)
    {
        return String.format("%s (%.2f%%)", TimeStamps.getPrettyElapsed(total, 2), percent);
    }

    @Override
    public String execute() throws Exception
    {
        Agent agent = getRequiredAgent();
        statistics = agentManager.getAgentStatistics(agent);
        usageChart = ChartUtils.renderForWeb(createChart(statistics), 450, 450);
        return SUCCESS;
    }

    private JFreeChart createChart(AgentStatistics statistics)
    {
        DefaultPieDataset dataset = new DefaultPieDataset();
        dataset.setValue(I18N.format("disabled"), statistics.getTotalDisabledTime());
        dataset.setValue(I18N.format("offline"), statistics.getTotalOfflineTime());
        dataset.setValue(I18N.format("synchronising"), statistics.getTotalSynchronisingTime());
        dataset.setValue(I18N.format("idle"), statistics.getTotalIdleTime());
        dataset.setValue(I18N.format("busy"), statistics.getTotalBusyTime());

        JFreeChart chart = ChartFactory.createPieChart(I18N.format("utilisation.chart"), dataset, true, false, false);
        chart.setBorderVisible(false);
        chart.setBackgroundPaint(Color.WHITE);
        chart.getTitle().setPaint(Color.DARK_GRAY);
        
        LegendTitle legend = chart.getLegend();
        legend.setBorder(new BlockBorder(Color.LIGHT_GRAY));
        legend.setMargin(0, 4, 0, 4);

        PiePlot plot = (PiePlot) chart.getPlot();
        plot.setShadowPaint(null);
        plot.setOutlinePaint(Color.LIGHT_GRAY);
        plot.setLabelGap(0.04);
        StandardPieSectionLabelGenerator labelGenerator = new StandardPieSectionLabelGenerator("{0} = {2}");
        plot.setLegendLabelGenerator(labelGenerator);
        plot.setLabelGenerator(labelGenerator);
        plot.setLabelShadowPaint(null);

        plot.setSectionPaint(0, ChartColours.DISABLED_FILL.asColor());
        plot.setSectionPaint(1, ChartColours.FAIL_FILL.asColor());
        plot.setSectionPaint(2, ChartColours.WARNING_FILL.asColor());
        plot.setSectionPaint(3, ChartColours.SUCCESS_FILL.asColor());
        plot.setSectionPaint(4, ChartColours.BUSY_FILL.asColor());

        return chart;
    }
}
