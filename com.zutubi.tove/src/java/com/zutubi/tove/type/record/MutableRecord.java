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
 * A record that supports writing to properties.
 */
public interface MutableRecord extends Record, Cloneable
{
    /**
     * Set the symbolic name for the data contained by this record.  The symbolic name defines the type of the
     * records data.
     *
     * @param name symbolic name
     *
     * @see com.zutubi.tove.type.TypeRegistry
     */
    void setSymbolicName(String name);

    /**
     * A meta data to this record.
     *
     * @param key used to identify the meta data
     * @param value of the meta data.
     */
    void putMeta(String key, String value);

    String removeMeta(String key);

    Object put(String key, Object value);

    Object remove(String key);

    void clear();

    /**
     * Updates this record by taking values from the given record.  All meta
     * and simple values from the given record are added to this record, except
     * where overwriteExisting is false and a corresponding value is already
     * defined.  If deep is true, all nested record in the given record are
     * used to recursively update existing counterparts in this record.  If
     * there is no existing counterpart a copy of the nested record is inserted
     * into this record.
     *
     * @param record the record to take values from to update this record
     * @param deep if true, the update recursively processes nested records
     *             where they exist in both this and the given record
     * @param overwriteExisting if true, values from record are unconditionally
     *                          set on this; if false they are only set where
     *                          no corresponding value exists
     */
    void update(Record record, boolean deep, boolean overwriteExisting);

    void setHandle(long handle);

    void setPermanent(boolean permanenet);
}
