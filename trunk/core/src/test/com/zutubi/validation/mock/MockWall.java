package com.zutubi.validation.mock;

import com.zutubi.validation.annotations.Numeric;
import com.zutubi.validation.annotations.Max;

/**
 * <class-comment/>
 */
public class MockWall
{
    private int height;

    @Numeric(max = 20)
    public int getHeight()
    {
        return height;
    }

    @Max(20) public void setHeight(int height)
    {
        this.height = height;
    }
}
