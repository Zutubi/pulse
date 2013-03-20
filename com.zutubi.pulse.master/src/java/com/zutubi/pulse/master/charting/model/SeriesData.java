package com.zutubi.pulse.master.charting.model;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Holds the data for a single report series: a collection of points.
 */
public class SeriesData
{
    private String name;
    private String customColour;
    private List<DataPoint> points =  new LinkedList<DataPoint>();

    /**
     * Creates a series which will render with a generated colour.
     *
     * @param name name of the series, used e.g. to label it
     */
    public SeriesData(String name)
    {
        this(name, null);
    }

    /**
     * Creates a series.
     *
     * @param name name of the series, used e.g. to label it
     * @param customColour if not null a custom colour to use for the series
     */
    public SeriesData(String name, String customColour)
    {
        this.name = name;
        this.customColour = customColour;
    }

    /**
     * @return the name of this series
     */
    public String getName()
    {
        return name;
    }

    /**
     * @return a custom colour to use for this series,  or null for any generated colour
     */
    public String getCustomColour()
    {
        return customColour;
    }

    /**
     * @return an unmodifiable list of all points in this series
     */
    public List<DataPoint> getPoints()
    {
        return Collections.unmodifiableList(points);
    }

    /**
     * Adds a point to the end of this series.
     *
     * @param point the point to add
     */
    public void addPoint(DataPoint point)
    {
        points.add(point);
    }

    /**
     * Adds all of the given points to this series.
     *
     * @param points the points to add
     */
    public void addPoints(Collection<DataPoint> points)
    {
        for (DataPoint point: points)
        {
            addPoint(point);
        }
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }

        SeriesData that = (SeriesData) o;

        if (!name.equals(that.name))
        {
            return false;
        }
        if (!points.equals(that.points))
        {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = name.hashCode();
        result = 31 * result + points.hashCode();
        return result;
    }

    @Override
    public String toString()
    {
        return name + ": " + points;
    }
}
