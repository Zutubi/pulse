package com.zutubi.util.math;

import com.zutubi.util.BinaryFunction;

/**
 * A binary function that adds two integers.
 */
public class IntegerAddition implements BinaryFunction<Integer, Integer, Integer>
{
    public Integer process(Integer input1, Integer input2)
    {
        return input1 + input2;
    }
}
