package com.cinnamonbob.core.model;

public abstract class Result extends Entity
{
    public abstract ResultState getState();

    public boolean inProgress()
    {
        return ResultState.IN_PROGRESS == getState();
    }

    public boolean succeeded()
    {
        return ResultState.SUCCESS == getState();
    }

    public boolean failed()
    {
        return ResultState.FAILURE == getState();
    }

    public boolean errored()
    {
        return ResultState.ERROR == getState();
    }

    public boolean commenced()
    {
        return inProgress() || completed();
    }

    public boolean completed()
    {
        return succeeded() || errored() || failed();
    }
}
