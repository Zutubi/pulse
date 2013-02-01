package com.zutubi.pulse.master.model;

import com.google.common.base.Function;

/**
 * Static utilities for working with builds.
 */
public final class BuildResults
{
    // Do not instantiate
    private BuildResults() {}

    /**
     * @return a function the converts a BuildResult to its number
     */
    public static Function<BuildResult, Long> toNumber()
    {
        return ToNumberFunction.INSTANCE;
    }
    
    private enum ToNumberFunction implements Function<BuildResult, Long>
    {
        INSTANCE;

        public Long apply(BuildResult input)
        {
            return input.getNumber();
        }


        @Override
        public String toString()
        {
            return "toNumber";
        }
    }
}
