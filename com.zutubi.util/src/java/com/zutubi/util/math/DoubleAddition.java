package com.zutubi.util.math;

import com.zutubi.util.BinaryFunction;

/**
 * A binary function that adds two doubles.
*/
public class DoubleAddition implements BinaryFunction<Double, Double, Double>
{
    public Double process(Double input1, Double input2)
    {
        return input1 + input2;
    }
}