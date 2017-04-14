/* Copyright 2017 Zutubi Pty Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
