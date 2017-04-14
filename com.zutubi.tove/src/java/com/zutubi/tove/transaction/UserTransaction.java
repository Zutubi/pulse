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

package com.zutubi.tove.transaction;

/**
 * The UserTransaction interface defines the methods to explicitly manage transaction
 * boundaries.
 */
public class UserTransaction
{
    /**
     * The system transaction manager.
     */
    private TransactionManager transactionManager;

    public UserTransaction(TransactionManager transactionManager)
    {
        this.transactionManager = transactionManager;
    }

    public TransactionManager getTransactionManager()
    {
        return transactionManager;
    }

    /**
     * Create a new transaction and associate it with the current thread.
     */
    public void begin()
    {
        this.transactionManager.begin();
    }

    /**
     * Complete the transaction associated with the current thread.
     */
    public void commit()
    {
        this.transactionManager.commit();
    }

    /**
     * Roll back the transaction associated with the current thread.
     */
    public void rollback()
    {
        this.transactionManager.rollback();
    }

    /**
     * Modify the transaction associated with the current thread such that the only possible outcome
     * of the transaction is to roll back the transaction.
     */
    public void setRollbackOnly()
    {
        this.transactionManager.setRollbackOnly();
    }

    /**
     * Obtain the status of the transaction associated with the current thread.
     *
     * @return status
     */
    public TransactionStatus getStatus()
    {
        return this.transactionManager.getStatus();
    }
}
