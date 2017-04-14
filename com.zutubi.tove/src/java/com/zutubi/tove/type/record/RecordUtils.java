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

import com.zutubi.util.BinaryProcedure;

import java.util.Arrays;

/**
 * Utility methods for working with records.
 */
public class RecordUtils
{
    /**
     * Tests if an object value has a type which is valid for a simple record
     * property (i.e. not a nested record).

     * @param value the value to test
     * @return true iff the given value is non-null and is valid as a simple
     *         record property
     */
    public static boolean isSimpleValue(Object value)
    {
        // The given object may not have come from a record at all, so don't
        // just test for it not being a record itself.
        return (value instanceof String) || (value instanceof String[]);
    }

    /**
     * Returns true if the two given simple values are equal.  This handles
     * both nulls and the different types of object which may be present as
     * simple values.  It also treats null and the empty string the same, as
     * web UIs have no way to specify a null string.
     *
     * @param value      the first value
     * @param otherValue the second value
     * @return true if the two values are equal
     */
    public static boolean valuesEqual(Object value, Object otherValue)
    {
        if(value == null)
        {
            return otherValue == null || isEmptyString(otherValue);
        }
        else if(otherValue == null)
        {
            return isEmptyString(value);
        }

        if (value.getClass() != otherValue.getClass())
        {
            return false;
        }

        if (value.getClass().isArray())
        {
            return Arrays.equals((Object[])value, (Object[])otherValue);
        }
        else
        {
            return value.equals(otherValue);
        }
    }

    private static boolean isEmptyString(Object o)
    {
        return (o instanceof String) && ((String) o).length() == 0;
    }

    /**
     * Creates and returns a skeleton record that shares the same structure as
     * the given record.
     * 
     * @param record record to copy the structure from
     * @return a new skeleton of the given record
     */
    public static Record createSkeletonOf(Record record)
    {
        MutableRecord result = new MutableRecordImpl();
        result.setSymbolicName(record.getSymbolicName());
        for (String key : record.nestedKeySet())
        {
            Record child = (Record) record.get(key);
            result.put(key, createSkeletonOf(child));
        }

        return result;
    }

    /**
     * Walks over two record trees in parallel, in depth first order, passing
     * each matching pair of records to the given procedure.  Only paths
     * present in both record trees will be followed (other records are
     * ignored).
     * 
     * @param record1 the first record tree
     * @param record2 the second record tree
     * @param fn callback function used to process pairs of records
     */
    public static void parallelDepthFirstWalk(Record record1, Record record2, BinaryProcedure<Record, Record> fn)
    {
        fn.run(record1, record2);
        for (String key: record1.nestedKeySet())
        {
            Object value2 = record2.get(key);
            if (value2 != null && value2 instanceof Record)
            {
                Record child1 = (Record) record1.get(key);
                parallelDepthFirstWalk(child1, (Record) value2, fn);
            }
        }
    }
}
