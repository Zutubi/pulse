package com.zutubi.util;

/**
 * A point in two-dimensional space, where the (left, top) corner is (0, 0) by convention.
 */
public class Point
{
    private final int x;
    private final int y;

    /**
     * Creates a new point at given coordinates.
     *
     * @param x horizontal position
     * @param y vertical position
     */
    public Point(int x, int y)
    {
        this.x = x;
        this.y = y;
    }

    /**
     * Gives the horizontal position of this point.
     *
     * @return this point's x coordinate
     */
    public int getX()
    {
        return x;
    }

    /**
     * Gives the vertical position of this point.
     *
     * @return this point's y coordinate
     */
    public int getY()
    {
        return y;
    }

    /**
     * Yields a new point that is offset from this one be the given amounts.
     *
     * @param xOffset horizontal offset from this point
     * @param yOffset vertical offset from this point
     * @return the new offset point
     */
    public Point offset(int xOffset, int yOffset)
    {
        return new Point(x + xOffset, y + yOffset);
    }

    /**
     * Returns a new point one unit above this point.
     *
     * @return the point above this one
     */
    public Point up()
    {
        return offset(0, -1);
    }

    /**
     * Returns a new point one unit below this point.
     *
     * @return the point below this one
     */
    public Point down()
    {
        return offset(0, 1);
    }

    /**
     * Returns a new point one unit left of this point.
     *
     * @return the point to the left of this one
     */
    public Point left()
    {
        return offset(-1, 0);
    }

    /**
     * Returns a new point one unit right of this point.
     *
     * @return the point to the right of this one
     */
    public Point right()
    {
        return offset(1, 0);
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

        Point point = (Point) o;

        if (x != point.x)
        {
            return false;
        }

        return y == point.y;
    }

    @Override
    public int hashCode()
    {
        int result = x;
        result = 31 * result + y;
        return result;
    }

    @Override
    public String toString()
    {
        return "(" + x + "," + y + ")";
    }
}
