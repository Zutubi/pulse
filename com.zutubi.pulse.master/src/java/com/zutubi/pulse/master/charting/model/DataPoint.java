/* Copyright 2017 Zutubi Pty Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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

    @Override
    public String toString()
    {
        return "(" + x + "," + y + ")";
    }
}
