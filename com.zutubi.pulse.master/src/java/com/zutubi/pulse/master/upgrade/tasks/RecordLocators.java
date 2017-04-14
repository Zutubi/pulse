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

package com.zutubi.pulse.master.upgrade.tasks;

import com.google.common.base.Predicate;
import com.zutubi.tove.type.record.Record;

/**
 * Static factory methods for creating {@link RecordLocator} instances.
 */
public class RecordLocators
{
    /**
     * Create a new locator that will combine all the records returned by the
     * given delegate locators.
     *
     * @param delegates delegate locators to combine
     * @return a locator that will return the union of all records returned by
     *         the delegates
     */
    public static RecordLocator newUnion(RecordLocator... delegates)
    {
        return new UnionRecordLocator(delegates);
    }

    /**
     * Create a new locator that finds a single record by a specific path.  If
     * the path does not exist, no records will be returned.
     *
     * @param path path to use to select the single record
     * @return a locator to find a single record by a fixed path
     */
    public static RecordLocator newPath(String path)
    {
        return new PathRecordLocator(path);
    }

    /**
     * Create a new locator that finds records by their path pattern.  Patterns
     * are defined by {@link com.zutubi.tove.type.record.RecordManager#selectAll(String)}.
     *
     * @param pathPatterns path patterns used for location, may contain wildcards
     * @return a locator which will find all records matching the given pattern
     */
    public static RecordLocator newPathPattern(String... pathPatterns)
    {
        return new PathPatternRecordLocator(pathPatterns);
    }

    /**
     * Create a new locator that can filter the output of another locator based
     * on the symbolic names of the records.  Only records with symbolic names
     * that are acceptable will be allowed to pass.
     *
     * @param delegate                delegate locator used to find records to
     *                                filter
     * @param acceptableSymbolicNames the types that this filter will allow to
     *                                pass
     * @return a type-filtering locater
     */
    public static RecordLocator newTypeFilter(RecordLocator delegate, String... acceptableSymbolicNames)
    {
        return new TypeFilterRecordLocator(delegate, acceptableSymbolicNames);
    }

    /**
     * Create a new locator that can filter the output of another locator based
     * on an arbitrary record predicate.  Only records that satisfy the
     * predicate will be allowed to pass.
     *
     * @param delegate  delegate locator used to find records to filter
     * @param predicate defines the records that this filter will allow to pass
     * @return a predicate-filtering locator
     */
    public static RecordLocator newPredicateFilter(RecordLocator delegate, Predicate<Record> predicate)
    {
        return new PredicateFilterRecordLocator(delegate, predicate);
    }
    
    /**
     * Create a new record locator that filters records found by another
     * locator, only passing records that are not inherited.  That is, records
     * that are the first definition of a path in a hierarchy.
     */
    public static RecordLocator newFirstDefinedFilter(RecordLocator delegate, TemplatedScopeDetails scope)
    {
        return new FirstDefinedFilterRecordLocator(delegate, scope);
    }
}
