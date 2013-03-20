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
