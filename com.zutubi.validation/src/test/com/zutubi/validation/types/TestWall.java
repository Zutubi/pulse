package com.zutubi.validation.types;

import com.zutubi.validation.annotations.Numeric;

public class TestWall
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
