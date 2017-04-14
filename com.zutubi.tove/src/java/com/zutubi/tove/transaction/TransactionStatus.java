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
 * The possible status's of a transaction. 
 */
public enum TransactionStatus
{
    /**
     * The initial state of a transaction.
     */
    INACTIVE,

    /**
     * An active transaction is in progress.
     */
    ACTIVE,

    /**
     * The transaction has begun processing a commit.
     */
    COMMITTING,

    /**
     * The transaction has been successfully committed.
     */
    COMMITTED,

    /**
     * The transaction has been marked for rollback only.  This means that it will
     * not be committed.
     */
    ROLLBACKONLY,

    /**
     * The transaction is in the process of being rolled back.
     */
    ROLLINGBACK,

    /**
     * The transaction has been successfully rolled back. 
     */
    ROLLEDBACK
}
