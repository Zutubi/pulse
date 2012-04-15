package com.zutubi.pulse.master.xwork.actions.agents;

import com.zutubi.i18n.Messages;
import com.zutubi.pulse.master.agent.statistics.AgentStatistics;
import com.zutubi.pulse.master.charting.render.ChartUtils;
import com.zutubi.pulse.master.tove.config.project.reports.ChartColours;
import com.zutubi.pulse.master.xwork.actions.GraphModel;
import com.zutubi.util.time.TimeStamps;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.block.BlockBorder;
import org.jfree.chart.labels.StandardPieSectionLabelGenerator;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.title.LegendTitle;
import org.jfree.data.general.DefaultPieDataset;

import java.awt.*;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

/**
 * JSON data for the agent statistics page.
 */
public class AgentStatisticsModel
{
    private static final Messages I18N = Messages.getInstance(AgentStatisticsModel.class);
    
    private String blurb;
    private RecipeStatisticsModel recipes;
    private UtilisationModel utilisation;
    private GraphModel usageGraph;

    public AgentStatisticsModel(AgentStatistics statistics) throws IOException
    {
        if (statistics.isEmpty())
        {
            blurb = I18N.format("statistics.empty");
        }
        else
        {
            DateFormat format = SimpleDateFormat.getDateInstance();
            String firstDay = format.format(statistics.getFirstDayStamp());
            blurb = I18N.format("statistics.blurb", firstDay);

            recipes = new RecipeStatisticsModel(statistics);
            utilisation = new UtilisationModel(statistics);
            usageGraph = new GraphModel(ChartUtils.renderForWeb(createChart(statistics), 450, 450));
        }
    }
    
    private JFreeChart createChart(AgentStatistics statistics)
    {
        String disabled = I18N.format("disabled");
        String offline = I18N.format("offline");
        String synchronising = I18N.format("synchronising");
        String idle = I18N.format("idle");
        String busy = I18N.format("busy");

        DefaultPieDataset dataset = new DefaultPieDataset();
        dataset.setValue(disabled, statistics.getTotalDisabledTime());
        dataset.setValue(offline, statistics.getTotalOfflineTime());
        dataset.setValue(synchronising, statistics.getTotalSynchronisingTime());
        dataset.setValue(idle, statistics.getTotalIdleTime());
        dataset.setValue(busy, statistics.getTotalBusyTime());

        JFreeChart chart = ChartFactory.createPieChart(I18N.format("utilisation.chart"), dataset, true, false, false);
        chart.setBorderVisible(false);
        chart.setBackgroundPaint(Color.WHITE);
        chart.getTitle().setPaint(Color.DARK_GRAY);
        
        LegendTitle legend = chart.getLegend();
        legend.setFrame(new BlockBorder(Color.LIGHT_GRAY));
        legend.setMargin(0, 4, 0, 4);

        PiePlot plot = (PiePlot) chart.getPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setShadowPaint(null);
        plot.setOutlinePaint(Color.LIGHT_GRAY);
        plot.setLabelGap(0.04);
        StandardPieSectionLabelGenerator labelGenerator = new StandardPieSectionLabelGenerator("{0} = {2}");
        plot.setLegendLabelGenerator(labelGenerator);
        plot.setLabelGenerator(labelGenerator);
        plot.setLabelShadowPaint(null);

        plot.setSectionPaint(disabled, ChartColours.DISABLED_FILL.asColor());
        plot.setSectionPaint(offline, ChartColours.FAIL_FILL.asColor());
        plot.setSectionPaint(synchronising, ChartColours.WARNING_FILL.asColor());
        plot.setSectionPaint(idle, ChartColours.SUCCESS_FILL.asColor());
        plot.setSectionPaint(busy, ChartColours.BUSY_FILL.asColor());

        return chart;
    }
    
    public String getBlurb()
    {
        return blurb;
    }

    public RecipeStatisticsModel getRecipes()
    {
        return recipes;
    }

    public UtilisationModel getUtilisation()
    {
        return utilisation;
    }

    public GraphModel getUsageGraph()
    {
        return usageGraph;
    }

    public static class RecipeStatisticsModel
    {
        private long totalRecipes;
        private String averageRecipesPerDay;
        private String averageBusyTimePerRecipe;

        public RecipeStatisticsModel(AgentStatistics statistics)
        {
            totalRecipes = statistics.getTotalRecipes();
            averageRecipesPerDay = String.format("%.2f", statistics.getRecipesPerDay());
            if (totalRecipes > 0)
            {
                averageBusyTimePerRecipe = TimeStamps.getPrettyElapsed((long) statistics.getBusyTimePerRecipe(), 2);
            }
            else
            {
                averageBusyTimePerRecipe = "n/a";
            }
        }

        public long getTotalRecipes()
        {
            return totalRecipes;
        }

        public String getAverageRecipesPerDay()
        {
            return averageRecipesPerDay;
        }

        public String getAverageBusyTimePerRecipe()
        {
            return averageBusyTimePerRecipe;
        }
    }
    
    public static class UtilisationModel
    {
        private String timeDisabled;
        private String timeOffline;
        private String timeSynchronising;
        private String timeIdle;
        private String timeBusy;

        public UtilisationModel(AgentStatistics statistics)
        {
            timeDisabled = renderTime(statistics.getTotalDisabledTime(), statistics.getPercentDisabledTime());
            timeOffline = renderTime(statistics.getTotalOfflineTime(), statistics.getPercentOfflineTime());
            timeSynchronising = renderTime(statistics.getTotalSynchronisingTime(), statistics.getPercentSynchronisingTime());
            timeIdle = renderTime(statistics.getTotalIdleTime(), statistics.getPercentIdleTime());
            timeBusy = renderTime(statistics.getTotalBusyTime(), statistics.getPercentBusyTime());
        }
        
        public String getTimeDisabled()
        {
            return timeDisabled;
        }

        public String getTimeOffline()
        {
            return timeOffline;
        }

        public String getTimeSynchronising()
        {
            return timeSynchronising;
        }

        public String getTimeIdle()
        {
            return timeIdle;
        }

        public String getTimeBusy()
        {
            return timeBusy;
        }

        private String renderTime(long total, double percent)
        {
            return String.format("%s (%.2f%%)", TimeStamps.getPrettyElapsed(total, 2), percent);
        }
    }
}
