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
 * A rollback exception is thrown if an attempt is made to commit
 * a transaction but it fails, forcing a rollback.  After the rollback
 * has been completed, this exception is thrown.
 */
public class RollbackException extends TransactionException
{
    public RollbackException()
    {
    }

    public RollbackException(String message)
    {
        super(message);
    }

    public RollbackException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public RollbackException(Throwable cause)
    {
        super(cause);
    }
}