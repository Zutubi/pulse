package com.zutubi.pulse.master.util;

import com.zutubi.util.NullaryFunction;

/**
 * This class provides support for classes that run outside the context of a managed spring
 * transaction to be executed within a transaction.
 */
public class TransactionContext
{
    /**
     * If you need to use this method, chances are the code you are trying to run
     * should be run at a lower level, within one of the managers perhaps.
     * 
     * @param f the function to execute within the transaction.
     * @return the result of the function execution.
     */
    public <T> T executeInsideTransaction(NullaryFunction<T> f)
    {
        return f.process();
    }
}
