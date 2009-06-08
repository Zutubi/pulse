package com.zutubi.pulse.master.charting.model;

/**
 * A single point of data on a report.  Points are comparable along the domain:
 * i.e. by their x vale.
 */
public class DataPoint implements Comparable
{
    private long x;
    private Number y;

    /**
     * Creates a data point.
     *
     * @param x the point's x (domain) value
     * @param y the point's y (range) value
     */
    public DataPoint(long x, Number y)
    {
        this.x = x;
        this.y = y;
    }

    /**
     * @return this point's x (domain) value
     */
    public long getX()
    {
        return x;
    }

    /**
     * @return this point's y (range) value
     */
    public Number getY()
    {
        return y;
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

        DataPoint dataPoint = (DataPoint) o;

        if (x != dataPoint.x)
        {
            return false;
        }
        if (!y.equals(dataPoint.y))
        {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = (int) (x ^ (x >>> 32));
        result = 31 * result + y.hashCode();
        return result;
    }

    public int compareTo(Object o)
    {
        DataPoint other = (DataPoint) o;
        if (x > other.x)
        {
            return 1;
        }
        else if (x < other.x)
        {
            return -1;
        }
        else
        {
            return 0;
        }
    }
}
