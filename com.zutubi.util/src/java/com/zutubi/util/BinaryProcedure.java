package com.zutubi.util;

/**
 * Generic binary procedure, takes two inputs.
 */
public interface BinaryProcedure<Input1Type, Input2Type>
{
    void process(Input1Type input1, Input2Type input2);
}
