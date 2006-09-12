package com.zutubi.validation.providers;

import com.zutubi.validation.annotations.Integral;

/**
 * <class-comment/>
 */
public class MockWall
{
    private int height;

    @Integral(max = 20)
    public int getHeight()
    {
        return height;
    }

    public void setHeight(int height)
    {
        this.height = height;
    }
}
