package com.zutubi.validation.mock;

import com.zutubi.validation.annotations.Numeric;

public class MockWall
{
    private int height;

    @Numeric(max = 20)
    public int getHeight()
    {
        return height;
    }

    public void setHeight(int height)
    {
        this.height = height;
    }
}
