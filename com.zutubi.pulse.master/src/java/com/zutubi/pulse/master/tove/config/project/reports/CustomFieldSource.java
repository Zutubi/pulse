package com.zutubi.pulse.master.tove.config.project.reports;

import com.zutubi.pulse.core.model.Result;

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
}
