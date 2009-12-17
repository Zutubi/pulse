package com.zutubi.validation.types;

import com.zutubi.validation.annotations.Validate;

/**
 * <class-comment/>
 */
public class TestHouse
{
    private TestDoor door;

    @Validate public TestDoor getDoor()
    {
        return door;
    }

    public void setDoor(TestDoor door)
    {
        this.door = door;
    }
}
