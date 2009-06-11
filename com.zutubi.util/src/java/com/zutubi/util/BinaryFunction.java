package com.zutubi.util;

/**
 * Generic binary function, takes two inputs and transforms them to one output.
 */
public interface BinaryFunction<Input1Type, Input2Type, OutputType>
{
    OutputType process(Input1Type input1, Input2Type input2);
}