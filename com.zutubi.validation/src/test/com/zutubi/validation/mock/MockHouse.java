package com.zutubi.validation.mock;

import com.zutubi.validation.annotations.Validate;

import java.util.List;

/**
 * <class-comment/>
 */
public class MockHouse
{
    private MockDoor door;

    @Validate public MockDoor getDoor()
    {
        return door;
    }

    public void setDoor(MockDoor door)
    {
        this.door = door;
    }
}
