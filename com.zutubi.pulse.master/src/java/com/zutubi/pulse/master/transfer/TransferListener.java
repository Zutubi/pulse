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

package com.zutubi.pulse.master.transfer;

import java.util.Map;

/**
 * The transfer listener receives callbacks whilst a transfer is in progress.
 *
 * The transfer listener can expect to receive callbacks in the following order.
 * - start
 * - startTable
 * - row
 * - row
 * - endTable
 * - startTable
 * - ...
 * - endTable
 * - end 
 */
public interface TransferListener
{
    /**
     * Called at the very start of the transfer processing.
     */
    void start();

    /**
     * Called when a new table is started.
     *
     * @param table
     */
    void startTable(Table table);

    /**
     * Called for each row within a table.
     * @param row
     */
    void row(Map<String, Object> row);

    /**
     * Called when all of the rows of the current table have been transfered.
     */
    void endTable();

    /**
     * Called at the very end of the transfer processing.
     */
    void end();
}
