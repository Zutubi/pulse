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

package com.zutubi.tove.type.record.store;

import com.zutubi.tove.type.record.Record;

/**
 *
 *
 */
public interface RecordStore
{
    /**
     * Insert a new record into the record store at the specified path.  This path can later be
     * used to retrieve that record.
     *
     * The record may itself contain children.
     *
     * The parent of the path at which this record is being inserted must exist.
     *
     * @param path uniquely identifies the record.
     * @param record data being inserted.
     *
     */
    void insert(String path, Record record);

    /**
     * Update the record identified by the specified path.
     *
     * This is NOT a deep update.
     *
     * @param path
     * @param record
     */
    void update(String path, Record record);

    Record delete(String path);

    Record select();

    Record exportRecords();

    void importRecords(Record r);
}
