package com.zutubi.util.math;

import com.zutubi.util.BinaryFunction;

/**
 * A binary function that adds two longs.
 */
public class LongAddition implements BinaryFunction<Long, Long, Long>
{
    public Long process(Long input1, Long input2)
    {
        return input1 + input2;
    }
}