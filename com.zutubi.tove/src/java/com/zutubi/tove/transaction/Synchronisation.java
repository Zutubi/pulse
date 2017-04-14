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
 * A simple callback handler that allows for a notification when a transaction is completed.
 */
public interface Synchronisation
{
    /**
     * Callback that is triggered when the transaction this synchronisation
     * is registered with completed.
     *
     * @param txn    the transaction that has been completed.
     */
    void postCompletion(Transaction txn);
}
