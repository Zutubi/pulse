package com.zutubi.pulse.master.charting.demo;

import com.zutubi.util.CircularBuffer;
import com.zutubi.util.Constants;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.xy.XYDataset;

import java.util.concurrent.Executors;

public class JvmMemoryData
{
    private static CircularBuffer<Long> used = new CircularBuffer<Long>(100);
    private static CircularBuffer<Long> total = new CircularBuffer<Long>(100);

    private static long startTime;

    public void init()
    {
        startTime = System.currentTimeMillis();
        Executors.newSingleThreadExecutor().submit(new Runnable()
        {
            public void run()
            {
                collectData();
            }
        });
    }

    protected void collectData()
    {
        // init the data.
        for (int i = 0; i < 100; i++)
        {
            used.append(0L);
            total.append(0L);
        }
        while (true)
        {
            used.append(Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory());
            total.append(Runtime.getRuntime().totalMemory());
            try
            {
                Thread.sleep(1000);
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        }
    }

    public static XYDataset getXYDataset()
    {
/*
        // data represents the last 100 seconds.
        long rangeStart = ((System.currentTimeMillis() - startTime) / 1000) - 100;

        DefaultXYDataset dataset = new DefaultXYDataset();
        double[][] data = new double[2][100];
        int i = 0;
        for (Long l : total)
        {
            data[0][i] = rangeStart < 0 ? 0 : rangeStart;
            data[1][i] = l / Constants.MEGABYTE;
            i++;
            rangeStart += 1;
        }
        dataset.addSeries("Total", data);

        rangeStart = ((System.currentTimeMillis() - startTime) / 1000) - 100;
        data = new double[2][100];
        i = 0;
        for (Long l : used)
        {
            data[0][i] = rangeStart < 0 ? 0 : rangeStart;
            data[1][i] = l / Constants.MEGABYTE;
            i++;
            rangeStart += 1;
        }
        dataset.addSeries("Used", data);
        return dataset;
*/
        return null;
    }

    public static CategoryDataset getCategoryDataset()
    {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        int i = 0;
        for (Long l : total)
        {
            Number d = l / Constants.MEGABYTE;
            dataset.addValue(d, "Total", i++);
        }
        i = 0;
        for (Long l : used)
        {
            Number d = l / Constants.MEGABYTE;
            dataset.addValue(d, "Free", i++);
        }
        return dataset;
    }
}
