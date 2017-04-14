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

package com.zutubi.tove.type.record;

/**
 * An interface for storing and loading records from permanent storage.
 */
public interface RecordSerialiser
{
    /**
     * Serialise the record using this record serialiser implementation.
     *
     * @param record    the record to be serialised.
     * @param deep      indicates whether or not nested records should also be serialised.
     *
     * @throws RecordSerialiseException on error.
     */
    void serialise(Record record, boolean deep) throws RecordSerialiseException;

    /**
     * Deserialise any serialised records.
     *
     * @return  the deserialised records
     *
     * @throws RecordSerialiseException on error.
     */
    MutableRecord deserialise() throws RecordSerialiseException;
}
