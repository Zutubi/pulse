package com.zutubi.pulse.charting;

import org.jfree.data.time.TimeTableXYDataset;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.TreeMap;

/**
 */
public class TimeBasedChartData
{
    private static final SimpleDateFormat DATE_FMT = new SimpleDateFormat("d-MMM-yyyy");
    private static final Calendar CALENDAR = Calendar.getInstance();

    /**
     * The default timeframe is 30 days.
     */
    public static final int DEFAULT_TIMEFRAME = 30;

    /**
     * The timeframe of this chart represents the number of days that will make up the domain of this chart.
     *
     * @see TimeBasedChartData#DEFAULT_TIMEFRAME
     */
    private int timeframe = DEFAULT_TIMEFRAME;
    private BuildResultsDataSource source;

    /**
     * Get the timeframe for this chart.
     *
     * @return the timeframe in days.
     */
    public int getTimeframe()
    {
        return timeframe;
    }

    /**
     * Set the timeframe for this chart.
     *
     * @param timeframe in days.
     */
    public void setTimeframe(int timeframe)
    {
        this.timeframe = timeframe;
    }

    public void setSource(BuildResultsDataSource source)
    {
        this.source = source;
    }

    public void populateDataSet(TimeTableXYDataset ds, DailyDataHandler handler)
    {
        Map<String, DailyData> map = aggregateData(source.getLastByDays(getTimeframe()));

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, -timeframe);

        for (int i = 0; i <= timeframe; i++)
        {
            Date date = cal.getTime();
            String key = getAggregateKey(date.getTime());
            handler.handle(date, map.get(key));
            cal.add(Calendar.DAY_OF_YEAR, 1);
        }
    }

    private Map<String, DailyData> aggregateData(BuildResultsResultSet resultSet)
    {
        Map<String, DailyData> map = new TreeMap<String, DailyData>();
        while (resultSet.next())
        {
            String timestamp = getAggregateKey(resultSet.getEndTime());
            if (!map.containsKey(timestamp))
            {
                map.put(timestamp, new DailyData(resultSet.getEndTime()));
            }

            DailyData a = map.get(timestamp);
            a.addBuild(resultSet);
        }
        return map;
    }

    private String getAggregateKey(long l)
    {
        CALENDAR.setTimeInMillis(l);
        return DATE_FMT.format(CALENDAR.getTime());
    }

    public double getLowerBound()
    {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, -timeframe);
        cal.add(Calendar.DAY_OF_YEAR, -3);
        return cal.getTime().getTime();
    }

    public double getUpperBound()
    {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, 3);        
        return cal.getTime().getTime();
    }
}
