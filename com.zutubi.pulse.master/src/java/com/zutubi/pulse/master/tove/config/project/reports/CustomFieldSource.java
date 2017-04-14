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

package com.zutubi.pulse.master.tove.config.project.reports;

import com.zutubi.pulse.core.model.Result;
import com.zutubi.util.adt.Pair;

import java.util.List;
import java.util.regex.Pattern;

/**
 * An interface to abstract the details of loading custom fields for results.
 */
public interface CustomFieldSource
{
    /**
     * Retrieves the value of a given custom field for the given result, if it
     * exists.
     *
     * @param result the result to get the field value for
     * @param name   the name of the field to retrieve
     * @return the field value, or null if it has no value
     */
    String getFieldValue(Result result, String name);

    /**
     * Retrieves all custom fields with names that match the given pattern in the given result.
     *
     * @param result the result to get the fields from
     * @param namePattern regular expression used to match fields to retrieve
     * @return a list of name, value pairs for all matching custom fields in the result
     */
    List<Pair<String, String>> getAllFieldValues(Result result, Pattern namePattern);
}
